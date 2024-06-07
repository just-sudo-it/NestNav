package MapReducer;

import Concurrency.PrioritizedTask;
import Concurrency.Priority;
import Concurrency.ThreadPool;
import Data.NestNavTask;
import Data.Response;
import Concurrency.ThreadSafeList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reducer {
    public static final int PORT = 6000;
    private int port;
    private ThreadPool threadPool; // Adding thread pool to manage tasks
    private Map<Integer, ThreadSafeList<Response>> resultMap;

    public Reducer(int port) {
        this.port = port;
        this.resultMap = new HashMap<>();
        // Initializing the thread pool with a reasonable number of threads
        this.threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 2);
    }

    public void start() {
        System.out.println("Reducer started on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket workerSocket = serverSocket.accept();
                threadPool.execute(new PrioritizedTask(() -> handleWorker(workerSocket), Priority.HIGH));
            }
        } catch (IOException e) {
            System.err.println("Error starting Reducer: " + e.getMessage());
        }
    }

    private void handleWorker(Socket workerSocket) {
        try (ObjectInputStream input = new ObjectInputStream(workerSocket.getInputStream())) {
            NestNavTask task = (NestNavTask) input.readObject();
            Integer mapId = task.getTaskId();
            List<Response> results = (List<Response>) task.getData();
            synchronized (resultMap) {
                resultMap.computeIfAbsent(mapId, k -> new ThreadSafeList<>()).addAll(results);
                if (resultMap.get(mapId).size() == expectedMapCount()) {
                    sendAggregatedResults(mapId);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling worker request: " + e.getMessage());
        }
    }

    private int expectedMapCount() {
        // Estimate the expected number of results based on some criteria
        return 3; // Update this value based on system's requirements
    }

    private void sendAggregatedResults(Integer mapId) {
        try (Socket masterSocket = new Socket("localhost", MasterServer.REDUCER_PORT);
             ObjectOutputStream output = new ObjectOutputStream(masterSocket.getOutputStream())) {
            // Assuming ThreadSafeList has responses that can be serialized
            List<Response> finalResults = resultMap.get(mapId).getAll();
            output.writeObject(new NestNavTask(mapId, (Serializable) finalResults));
            System.out.println("Aggregated results for MapID: " + mapId + " have been sent.");
        } catch (IOException e) {
            System.err.println("Error sending aggregated results: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Reducer(PORT).start();
    }
}
