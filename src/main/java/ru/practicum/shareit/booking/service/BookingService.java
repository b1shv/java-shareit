package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Booking;

import java.util.List;

public interface BookingService {
    Booking getBookingById(long bookingId, long userId);

    Booking addBooking(Booking booking);

    Booking updateStatus(long userId, long bookingId, boolean approved);

    List<Booking> getBookingsByBookerId(long bookerId, String stateName, Pageable pageable);

    List<Booking> getBookingsByOwnerId(long ownerId, String stateName, Pageable pageable);

    Booking getLastItemBooking(long itemId);

    Booking getNextItemBooking(long itemId);
}
