package Worker;

import Concurrency.PrioritizedTask;
import Concurrency.Priority;
import Concurrency.ThreadPool;
import Concurrency.ThreadSafeList;
import Data.*;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.stream.Collectors;

public class WorkerNode {
    private int port;
    private ThreadPool threadPool;
    private ThreadSafeList<Accommodation> accommodations;

    public WorkerNode(int port) {
        this.port = port;
        this.threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors() / 2, Runtime.getRuntime().availableProcessors());
        this.accommodations = new ThreadSafeList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Worker Node started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new PrioritizedTask(() -> handleClient(clientSocket), Priority.HIGH));
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())) {
            System.out.println("Worker Node handling client request :" + clientSocket.getInetAddress().toString());
            NestNavTask task = (NestNavTask) input.readObject();
            Response response = handleRequest(task);

            if (task.getData() instanceof SearchRequest) {
                forwardToReducer(task);
            } else {
                // Return response directly to the master
                try (ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {
                    output.writeObject(response);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
    }

    private Response handleRequest(NestNavTask task) {
        Serializable data = task.getData();
        if (data instanceof AddAccommodationRequest) {
            AddAccommodationRequest addRequest = (AddAccommodationRequest) data;
            accommodations.add(addRequest.getAccommodation());
            return new Response(true, "Accommodation added successfully", null);
        } else if (data instanceof SearchRequest) {
            SearchRequest searchRequest = (SearchRequest) data;
            return handleSearchRequest(searchRequest, task.getTaskId());
        } else if (data instanceof BookingRequest) {
            BookingRequest bookingRequest = (BookingRequest) data;
            return handleBookingRequest(bookingRequest);
        }
        return new Response(false, "Invalid request type", null);
    }

    private Response handleSearchRequest(SearchRequest request, Integer taskId) {
        List<Accommodation> filteredAccommodations = accommodations.stream()
                .filter(acc -> acc.getArea().equals(request.getArea())
                        && acc.isAvailable(request.getStartDate(), request.getEndDate())
                        && acc.getNoOfPersons() >= request.getMinCapacity())
                .collect(Collectors.toList());

        return new Response(true, "Search successful", filteredAccommodations);
    }

    private void forwardToReducer(NestNavTask task) {
        try (Socket reducerSocket = new Socket("localhost", Reducer.REDUCER_PORT);
             ObjectOutputStream output = new ObjectOutputStream(reducerSocket.getOutputStream())) {
            output.writeObject(task);
        } catch (IOException e) {
            System.err.println("Error forwarding to reducer: " + e.getMessage());
        }
    }

    private Response handleBookingRequest(BookingRequest request) {
        return accommodations.stream()
                .filter(x -> x.getRoomName().equals(request.getRoomName()))
                .findFirst()
                .map(accommodation -> {
                    if (accommodation.book(request.getStartDate(), request.getEndDate())) {
                        return new Response(true, "Booking successful", null);
                    } else {
                        return new Response(false, "Booking failed", null);
                    }
                })
                .orElse(new Response(false, "Accommodation not found", null));
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java WorkerNode <port number>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        new WorkerNode(port).start();
    }
}