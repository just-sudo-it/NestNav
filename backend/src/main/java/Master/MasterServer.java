package Master;

import Concurrency.PrioritizedTask;
import Concurrency.Priority;
import Concurrency.ThreadPool;
import Concurrency.ThreadSafeList;
import Data.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MasterServer {
    public static int PORT = 8000;
    public static int REDUCER_PORT = 7000;

    private ServerSocket serverSocket;
    private ServerSocket reducerSocket; // Server socket for listening to reducer results
    private Map<Integer, Socket> workerSockets; // Worker ID to socket mapping
    private ThreadPool threadPool;
    private AtomicInteger nextMapId = new AtomicInteger(0);
    private final Map<Integer, Object> lockMap = new HashMap<>();
    private final Map<Integer, List<Accommodation>> resultMap = new HashMap<>();

    public MasterServer(int port, List<Integer> workerPorts, int reducerPort) throws IOException {
        serverSocket = new ServerSocket(port);
        workerSockets = new HashMap<>();
        reducerSocket = new ServerSocket(reducerPort);
        threadPool = new ThreadPool(workerPorts.size(), workerPorts.size() * 2);

        initializeWorkers(workerPorts);
        listenForReducer();
    }

    private void initializeWorkers(List<Integer> workerPorts) {
        for (int workerPort : workerPorts) {
            try {
                System.out.println("Initializing worker on port " + workerPort);
                Socket socket = new Socket("localhost", workerPort);
                workerSockets.put(workerPort, socket);
            } catch (IOException e) {
                System.out.println("Error connecting to worker on port: " + workerPort);
            }
        }
    }

    private void listenForReducer() {
        threadPool.execute(new PrioritizedTask(() -> {
            while (true) {
                try {
                    Socket socket = reducerSocket.accept();
                    handleReducer(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, Priority.HIGH));
    }

    private void handleReducer(Socket socket) {
        try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            System.out.println("Received reducer task");
            NestNavTask task = (NestNavTask) input.readObject();
            int mapId = task.getTaskId();
            List<Accommodation> results = (List<Accommodation>) task.getData();
            synchronized (lockMap.get(mapId)) {
                resultMap.put(mapId, results);
                lockMap.get(mapId).notifyAll();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void acceptClientConnections() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client request executed");
                threadPool.execute(
                        new PrioritizedTask(
                                () -> handleClient(clientSocket),
                                Priority.MEDIUM));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {
            Request request = (Request) input.readObject();
            int mapId = nextMapId.getAndIncrement();
            lockMap.put(mapId, new Object());
            resultMap.put(mapId, new ArrayList<>());

            if (request instanceof BookingRequest) {
                BookingRequest bookingRequest = (BookingRequest) request;
                forwardToWorker(bookingRequest, mapId, clientSocket);
            } else if (request instanceof AddAccommodationRequest) {
                AddAccommodationRequest addAccommodationRequest = (AddAccommodationRequest) request;
                forwardToWorker(addAccommodationRequest, mapId, clientSocket);
            } else if (request instanceof SearchRequest) {
                distributeSearch(request, mapId, output);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void forwardToWorker(BookingRequest request, int mapId, Socket clientSocket) {
        int workerId = hash(request.getRoomName()) % workerSockets.size();
        System.out.println("Forwarding to worker " + workerId);
        try (ObjectOutputStream out = new ObjectOutputStream(workerSockets.get(workerId).getOutputStream())) {
            NestNavTask task = new NestNavTask(mapId, (Serializable) request);
            out.writeObject(task);

            // Read the response from the worker
            try (ObjectInputStream workerInput = new ObjectInputStream(workerSockets.get(workerId).getInputStream())) {
                Response response = (Response) workerInput.readObject();
                try (ObjectOutputStream clientOutput = new ObjectOutputStream(clientSocket.getOutputStream())) {
                    clientOutput.writeObject(response);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void forwardToWorker(AddAccommodationRequest request, int mapId, Socket clientSocket) {
        int workerId = hash(request.getAccommodation().getRoomName()) % workerSockets.size();
        System.out.println("Forwarding to worker " + workerId);
        try (ObjectOutputStream out = new ObjectOutputStream(workerSockets.get(workerId).getOutputStream())) {
            NestNavTask task = new NestNavTask(mapId, (Serializable) request);
            out.writeObject(task);

            // Read the response from the worker
            try (ObjectInputStream workerInput = new ObjectInputStream(workerSockets.get(workerId).getInputStream())) {
                Response response = (Response) workerInput.readObject();
                try (ObjectOutputStream clientOutput = new ObjectOutputStream(clientSocket.getOutputStream())) {
                    clientOutput.writeObject(response);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void distributeSearch(Request request, int mapId, ObjectOutputStream clientOutput) {
        System.out.println("Distributed search for " + request);
        NestNavTask task = new NestNavTask(mapId, (Serializable) request);
        for (Socket socket : workerSockets.values()) {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(task);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Wait for results from the reducer
        synchronized (lockMap.get(mapId)) {
            try {
                while (resultMap.get(mapId).isEmpty()) {
                    lockMap.get(mapId).wait();
                }
                clientOutput.writeObject(new NestNavTask(mapId, (Serializable) resultMap.get(mapId)));
                cleanUp(mapId);
            } catch (InterruptedException | IOException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    private void cleanUp(int mapId) {
        synchronized (lockMap.get(mapId)) {
            resultMap.remove(mapId);
            lockMap.remove(mapId);
        }
    }

    private int hash(String roomName) {
        return roomName.hashCode();
    }

    public static void main(String[] args) {
        try {
            List<Integer> workerPorts = Arrays.asList(8001, 8002, 8003);

            MasterServer master = new MasterServer(
                    MasterServer.PORT,
                    workerPorts,
                    MasterServer.REDUCER_PORT);

            master.acceptClientConnections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}