package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.Booking_;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;

import static ru.practicum.shareit.booking.BookingSpecifications.bookerId;
import static ru.practicum.shareit.booking.BookingSpecifications.endInFuture;
import static ru.practicum.shareit.booking.BookingSpecifications.endInPast;
import static ru.practicum.shareit.booking.BookingSpecifications.ownerId;
import static ru.practicum.shareit.booking.BookingSpecifications.startInFuture;
import static ru.practicum.shareit.booking.BookingSpecifications.startInPast;
import static ru.practicum.shareit.booking.BookingSpecifications.status;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, Booking_.START);

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

        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        bookingToUpdate.setStatus(status);

        return bookingRepository.save(bookingToUpdate);
    }

    @Override
    public List<Booking> getBookingsByBookerId(long bookerId, String stateName) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException(String.format("User ID %d is not found", bookerId));
        }

        try {
            BookingState state = BookingState.valueOf(stateName);
            switch (state) {
                case ALL:
                    return bookingRepository.findAll(bookerId(bookerId), SORT_BY_START_DESC);
                case WAITING:
                    return bookingRepository.findAll(
                            bookerId(bookerId).and(status(BookingStatus.WAITING)), SORT_BY_START_DESC);
                case REJECTED:
                    return bookingRepository.findAll(
                            bookerId(bookerId).and(status(BookingStatus.REJECTED)), SORT_BY_START_DESC);
                case CURRENT:
                    return bookingRepository.findAll(
                            bookerId(bookerId).and(startInPast()).and(endInFuture()), SORT_BY_START_DESC);
                case PAST:
                    return bookingRepository.findAll(
                            bookerId(bookerId).and(endInPast()), SORT_BY_START_DESC);
                case FUTURE:
                    return bookingRepository.findAll(
                            bookerId(bookerId).and(startInFuture()), SORT_BY_START_DESC);
                default:
                    return Collections.emptyList();
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", stateName));
        }
    }

    @Override
    public List<Booking> getBookingsByOwnerId(long ownerId, String stateName) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(String.format("User ID %d is not found", ownerId));
        }

        try {
            BookingState state = BookingState.valueOf(stateName);
            switch (state) {
                case ALL:
                    return bookingRepository.findAll(ownerId(ownerId), SORT_BY_START_DESC);
                case WAITING:
                    return bookingRepository.findAll(
                            ownerId(ownerId).and(status(BookingStatus.WAITING)), SORT_BY_START_DESC);
                case REJECTED:
                    return bookingRepository.findAll(
                            ownerId(ownerId).and(status(BookingStatus.REJECTED)), SORT_BY_START_DESC);
                case CURRENT:
                    return bookingRepository.findAll(
                            ownerId(ownerId).and(startInPast()).and(endInFuture()), SORT_BY_START_DESC);
                case PAST:
                    return bookingRepository.findAll(
                            ownerId(ownerId).and(endInPast()), SORT_BY_START_DESC);
                case FUTURE:
                    return bookingRepository.findAll(
                            ownerId(ownerId).and(startInFuture()), SORT_BY_START_DESC);
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
