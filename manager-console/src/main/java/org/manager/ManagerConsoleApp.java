package org.manager;

import Data.Accommodation;
import Data.AddAccommodationRequest;
import Data.Request;
import Data.Response;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ManagerConsoleApp {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\nManager Interface:");
                System.out.println("1. Add Accommodation Manually");
                System.out.println("2. Add Accommodations from JSON");
                System.out.println("3. List Accommodations");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        addAccommodationManually(out, in, scanner);
                        break;
                    case 2:
                        addAccommodationsFromJson(out, in, scanner);
                        break;
                    case 3:
                        requestAccommodationList(out, in);
                        break;
                    case 0:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void addAccommodationManually(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("Enter room name:");
        String roomName = scanner.nextLine();
        System.out.println("Enter area:");
        String area = scanner.nextLine();
        System.out.println("Enter capacity:");
        int capacity = scanner.nextInt();
        System.out.println("Enter stars:");
        int stars = scanner.nextInt();
        System.out.println("Enter number of reviews:");
        int reviews = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.println("Enter image URL:");
        String imageUrl = scanner.nextLine();

        List<Date> availableDates = Arrays.asList(new Date(), new Date()); // Sample dates, modify as needed
        out.writeObject(new AddAccommodationRequest(new Accommodation(roomName, capacity, area, stars, reviews, imageUrl, availableDates)));
        Response response = (Response) in.readObject();
        System.out.println("Response from server: " + response.getMessage());
    }

    private static void addAccommodationsFromJson(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("Enter path to JSON file:");
        String jsonFilePath = scanner.nextLine();
        try {
            List<Accommodation> accommodations = Accommodation.readAccommodationsFromJson(jsonFilePath);
            for (Accommodation accommodation : accommodations) {
                out.writeObject(new AddAccommodationRequest(accommodation));
                Response response = (Response) in.readObject();
                System.out.println("Response from server: " + response.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Failed to read or parse JSON file: " + e.getMessage());
        }
    }

    private static void requestAccommodationList(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Implementing a placeholder request type to list accommodations
        //out.writeObject(new Request("LIST_ACCOMMODATIONS"));
        List<Accommodation> accommodations = (List<Accommodation>) in.readObject();
        accommodations.forEach(acc -> {
            System.out.println("Room: " + acc.getRoomName() + ", Area: " + acc.getArea());
        });
    }
}
