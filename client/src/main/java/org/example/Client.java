package org.example;

import Data.BookingRequest;
import Data.Response;
import Data.SearchRequest;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("User Interface:");
            System.out.println("1. Search Accommodations");
            System.out.println("2. Book Accommodation");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    searchAccommodations(out, in, scanner);
                    break;
                case 2:
                    bookAccommodation(out, in, scanner);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void searchAccommodations(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("Enter area to search:");
        String area = scanner.next();
        out.writeObject(new SearchRequest(area, new Date(), new Date(), 2,2,1));
        Response response = (Response) in.readObject();
        System.out.println("Search results: " + response.getMessage());
    }

    private static void bookAccommodation(ObjectOutputStream out, ObjectInputStream in, Scanner scanner) throws IOException, ClassNotFoundException {
        System.out.println("Enter room name, start date, end date:");
        String roomName = scanner.next();
        Date startDate = new Date();
        Date endDate = new Date();
        out.writeObject(new BookingRequest(roomName, startDate, endDate));
        Response response = (Response) in.readObject();
        System.out.println("Booking response: " + response.getMessage());
    }
}
