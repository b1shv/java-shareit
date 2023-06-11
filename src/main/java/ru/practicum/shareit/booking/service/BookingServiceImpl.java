package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public Booking getBookingById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking ID %d is not found", bookingId)));
        if (booking.getBooker().getId() != userId && booking.getItem().getOwnerId() != userId) {
            throw new ForbiddenException(String.format("User ID %d has no access to booking ID %d", userId, bookingId));
        }
        return booking;
    }

    @Override
    public Booking addBooking(Booking booking) {
        if (booking.getBooker().getId() == booking.getItem().getOwnerId()) {
            throw new ForbiddenException(String.format("Item ID %d can't be booked by its owner ID %d",
                    booking.getItem().getId(), booking.getBooker().getId()));
        }
        if (Boolean.FALSE.equals(booking.getItem().getAvailable())) {
            throw new ValidationException(
                    String.format("Item ID %d is not available for booking", booking.getItem().getId()));
        }
        if (booking.getEnd().isBefore(booking.getStart()) || booking.getEnd().isEqual(booking.getStart())) {
            throw new ValidationException("Booking end time must be later than its start time");
        }
        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateStatus(long userId, long bookingId, boolean approved) {
        Booking bookingToUpdate = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking ID %d is not found", bookingId)));

        if (bookingToUpdate.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException(String.format("Status for booking ID %d is already set", bookingId));
        }
        if (bookingToUpdate.getItem().getOwnerId() != userId) {
            throw new ForbiddenException(String.format(
                    "User ID %d is not an owner of an item ID %d", userId, bookingToUpdate.getItem().getId()));
        }

        if (approved) {
            bookingToUpdate.setStatus(BookingStatus.APPROVED);
        } else {
            bookingToUpdate.setStatus(BookingStatus.REJECTED);
        }

        return bookingRepository.save(bookingToUpdate);
    }

    @Override
    public List<Booking> getBookingsByBookerId(long bookerId, String stateName) {
        if (userRepository.findById(bookerId).isEmpty()) {
            throw new NotFoundException(String.format("User ID %d is not found", bookerId));
        }

        try {
            BookingState state = BookingState.valueOf(stateName);
            switch (state) {
                case ALL:
                    return bookingRepository.findByBookerIdOrderByStartDesc(bookerId);
                case WAITING:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
                case REJECTED:
                    return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
                case CURRENT:
                    return bookingRepository.findCurrentByBookerId(bookerId);
                case PAST:
                    return bookingRepository.findPastByBookerId(bookerId);
                case FUTURE:
                    return bookingRepository.findFutureByBookerId(bookerId);
                default:
                    return Collections.emptyList();
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", stateName));
        }
    }

    @Override
    public List<Booking> getBookingsByOwnerId(long ownerId, String stateName) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException(String.format("User ID %d is not found", ownerId));
        }

        try {
            BookingState state = BookingState.valueOf(stateName);
            switch (state) {
                case ALL:
                    return bookingRepository.findByOwnerId(ownerId);
                case WAITING:
                    return bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.WAITING);
                case REJECTED:
                    return bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED);
                case CURRENT:
                    return bookingRepository.findCurrentByOwnerId(ownerId);
                case PAST:
                    return bookingRepository.findPastByOwnerId(ownerId);
                case FUTURE:
                    return bookingRepository.findFutureByOwnerId(ownerId);
                default:
                    return Collections.emptyList();
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", stateName));
        }
    }

    @Override
    public Booking getLastItemBooking(long itemId) {
        return bookingRepository.findLastItemBooking(itemId, BookingStatus.REJECTED.name());
    }

    @Override
    public Booking getNextItemBooking(long itemId) {
        return bookingRepository.findNextItemBooking(itemId, BookingStatus.REJECTED.name());
    }
}
