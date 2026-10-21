
package com.eventbooking;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SeatBookingApplication {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println(" CONCURRENT EVENT SEAT BOOKING SYSTEM");
        System.out.println("========================================");

        System.out.print("Enter event name (default: Java Meetup): ");
        String eventName = scanner.nextLine().trim();

        if (eventName.isEmpty()) {
            eventName = "Java Meetup";
        }

        int totalSeats;

        while (true) {
            System.out.print("Enter total number of seats: ");

            try {
                totalSeats = Integer.parseInt(scanner.nextLine().trim());

                if (totalSeats > 0) {
                    break;
                }

                System.out.println("Seats must be greater than zero.");

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }

        Event event = new Event(eventName, totalSeats);
        BookingService service = new BookingService(event);

        System.out.println("\nEvent created: " + event.getEventName());
        System.out.println("Available seats: "
                + service.getAvailableSeats());

        StatusMonitor monitor = new StatusMonitor(5000);
        Thread monitorThread = new Thread(monitor, "status-monitor");
        monitorThread.start();

        ExecutorService executor =
                Executors.newFixedThreadPool(3);

        boolean exit = false;

        try {
            while (!exit) {

                System.out.println("\n========== MENU ==========");
                System.out.println("1. Book a single seat");
                System.out.println("2. Submit concurrent bookings");
                System.out.println("3. View available seats");
                System.out.println("4. View confirmed bookings");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {

                    case "1": {
                        System.out.print("Customer name: ");
                        String name = scanner.nextLine().trim();

                        System.out.print("Seat number: ");
                        int seat = readInteger(scanner);

                        if (name.isEmpty()) {
                            System.out.println(
                                    "Customer name cannot be empty.");
                        } else {
                            executor.submit(
                                    new BookingTask(service, name, seat));
                        }
                        break;
                    }

                    case "2": {
                        System.out.print(
                                "How many booking requests? ");
                        int count = readInteger(scanner);

                        if (count <= 0) {
                            System.out.println(
                                    "Enter a number greater than zero.");
                            break;
                        }

                        for (int i = 1; i <= count; i++) {

                            System.out.println("Request " + i + ":");

                            System.out.print("Customer name: ");
                            String name = scanner.nextLine().trim();

                            System.out.print("Seat number: ");
                            int seat = readInteger(scanner);

                            if (name.isEmpty()) {
                                System.out.println(
                                        "Name cannot be empty. Skipping request.");
                                continue;
                            }

                            executor.submit(
                                    new BookingTask(service, name, seat));
                        }

                        System.out.println(
                                "All requests submitted to the thread pool.");
                        break;
                    }

                    case "3":
                        System.out.println("Available seats: "
                                + service.getAvailableSeats());
                        break;

                    case "4": {
                        List<Booking> bookings =
                                service.getBookings();

                        if (bookings.isEmpty()) {
                            System.out.println("No confirmed bookings.");
                        } else {
                            System.out.println("Confirmed bookings:");
                            bookings.forEach(System.out::println);
                        }
                        break;
                    }

                    case "5":
                        exit = true;
                        break;

                    default:
                        System.out.println(
                                "Invalid option. Choose 1 to 5.");
                }
            }

        } finally {
            System.out.println("\nShutting down booking service...");

            executor.shutdown();

            try {
                if (!executor.awaitTermination(
                        30, TimeUnit.SECONDS)) {

                    executor.shutdownNow();

                    if (!executor.awaitTermination(
                            5, TimeUnit.SECONDS)) {
                        System.out.println(
                                "Some tasks did not finish in time.");
                    }
                }

            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            monitor.stopMonitoring();

            try {
                monitorThread.join();
            } catch (InterruptedException e) {
                monitorThread.interrupt();
                Thread.currentThread().interrupt();
            }

            System.out.println("\n======= BOOKING SUMMARY =======");
            System.out.println("Event: " + event.getEventName());
            System.out.println("Total seats: " + event.getTotalSeats());
            System.out.println("Booked seats: "
                    + service.getBookings().size());
            System.out.println("Remaining seats: "
                    + service.getAvailableSeats().size());
            System.out.println("Confirmed bookings: "
                    + service.getBookings());
            System.out.println("Available seats: "
                    + service.getAvailableSeats());
            System.out.println("================================");

            scanner.close();
        }
    }

    private static int readInteger(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(
                        scanner.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.print(
                        "Enter a valid whole number: ");
            }
        }
    }
}
