package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.Booking;

import java.util.List;

public interface BookingService {
    Booking getBookingById(long bookingId, long userId);

    Booking addBooking(Booking booking);

    Booking updateStatus(long userId, long bookingId, boolean approved);

    List<Booking> getBookingsByBookerId(long bookerId, String stateName);

    List<Booking> getBookingsByOwnerId(long ownerId, String stateName);

    Booking getLastItemBooking(long itemId);

    Booking getNextItemBooking(long itemId);
}
