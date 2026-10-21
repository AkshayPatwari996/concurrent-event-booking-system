
package com.eventbooking;

public class BookingTask implements Runnable {

    private final BookingService bookingService;
    private final String customerName;
    private final int seatNumber;

    public BookingTask(BookingService bookingService,
                       String customerName,
                       int seatNumber) {
        this.bookingService = bookingService;
        this.customerName = customerName;
        this.seatNumber = seatNumber;
    }

    @Override
    public void run() {
        try {
            Booking booking = bookingService.bookSeat(
                    customerName, seatNumber);

            System.out.println(
                    "[" + Thread.currentThread().getName()
                            + "] SUCCESS: " + booking);

        } catch (InvalidBookingException e) {
            System.out.println(
                    "[" + Thread.currentThread().getName()
                            + "] REJECTED: " + customerName
                            + " could not book seat " + seatNumber
                            + ". Reason: " + e.getMessage());

        } finally {
            System.out.println(
                    "[" + Thread.currentThread().getName()
                            + "] Booking attempt completed for "
                            + customerName);
        }
    }
}
