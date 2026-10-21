
package com.eventbooking;

import java.util.ArrayList;
import java.util.List;

public class BookingService {

    private final Event event;
    private final List<Integer> availableSeats = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public BookingService(Event event) {
        this.event = event;

        for (int i = 1; i <= event.getTotalSeats(); i++) {
            availableSeats.add(i);
        }
    }

    public synchronized Booking bookSeat(
            String customerName, int seatNumber)
            throws InvalidBookingException {

        validateSeat(seatNumber);

        if (!availableSeats.contains(seatNumber)) {
            throw new InvalidBookingException(
                    "Seat " + seatNumber + " is already booked.");
        }

        availableSeats.remove(Integer.valueOf(seatNumber));

        Booking booking = new Booking(customerName, seatNumber);
        bookings.add(booking);

        return booking;
    }

    public synchronized List<Integer> getAvailableSeats() {
        return new ArrayList<>(availableSeats);
    }

    public synchronized List<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    private void validateSeat(int seatNumber)
            throws InvalidBookingException {

        if (seatNumber < 1 || seatNumber > event.getTotalSeats()) {
            throw new InvalidBookingException(
                    "Invalid seat number: " + seatNumber);
        }
    }
}
