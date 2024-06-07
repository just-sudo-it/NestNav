package org.manager;

import Data.Accommodation;
import Data.AddAccommodationRequest;
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
                System.out.println("1. Add Accommodation");
                System.out.println("2. List Accommodations");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        addAccommodation(out, in, scanner);
                        break;
                    case 2:
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

    private static void addAccommodation(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("Enter room name, area, capacity, stars, number of reviews, image URL:");
        
        String roomName = scanner.nextLine();
        String area = scanner.nextLine();
        int capacity = scanner.nextInt();
        int stars = scanner.nextInt();
        int reviews = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        String imageUrl = scanner.nextLine();

        List<Date> availableDates = Arrays.asList(new Date(), new Date()); // Sample 
        out.writeObject(new AddAccommodationRequest(new Accommodation(roomName, capacity, area, stars, reviews, imageUrl, availableDates)));
        Response response = (Response) in.readObject();
        System.out.println("Response from server: " + response.getMessage());
    }

    private static void requestAccommodationList(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        //out.writeObject(new RequestAllAccommodations());
        List<Accommodation> accommodations = (List<Accommodation>) in.readObject();
        accommodations.forEach(acc -> {
            System.out.println("Room: " + acc.getRoomName() + ", Area: " + acc.getArea());
        });
    }
}
