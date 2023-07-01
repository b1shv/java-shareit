package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.UserRepository;

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

        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        bookingToUpdate.setStatus(status);

        return bookingRepository.save(bookingToUpdate);
    }

    @Override
    public List<Booking> getBookingsByBookerId(long bookerId, String stateName, Pageable pageable) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException(String.format("User ID %d is not found", bookerId));
        }

        try {
            BookingState state = BookingState.valueOf(stateName);
            Specification<Booking> byBookerId = (r, q, cb) -> cb.equal(r.<Long>get("booker").get("id"), bookerId);
            return bookingRepository.findAll(Specification.where(byBookerId).and(state.getSpecification()),
                    pageable).getContent();
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", stateName));
        }
    }

    @Override
    public List<Booking> getBookingsByOwnerId(long ownerId, String stateName, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(String.format("User ID %d is not found", ownerId));
        }

        try {
            BookingState state = BookingState.valueOf(stateName);
            Specification<Booking> byOwnerId = (r, q, cb) -> cb.equal(r.<Long>get("item").get("ownerId"), ownerId);
            return bookingRepository.findAll(Specification.where(byOwnerId).and(state.getSpecification()),
                    pageable).getContent();
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", stateName));
        }
    }

    @Override
    public Booking getLastItemBooking(long itemId) {
        return bookingRepository.findLastItemBooking(itemId);
    }

    @Override
    public Booking getNextItemBooking(long itemId) {
        return bookingRepository.findNextItemBooking(itemId);
    }
}
