package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    BookingRepository bookingRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

    @Test
    void getBookingById_shouldReturnBooking() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).build();
        Booking booking = Booking.builder().booker(user).item(item).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertEquals(booking, bookingService.getBookingById(1, user.getId()));
        assertEquals(booking, bookingService.getBookingById(1, item.getOwnerId()));
    }

    @Test
    void getBookingById_shouldThrowException_ifBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1, 1));
    }

    @Test
    void getBookingById_shouldThrowException_ifUserIsNotOwnerAndNotBooker() {
        long randomUserId = 143;
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).build();
        Booking booking = Booking.builder().booker(user).item(item).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class, () -> bookingService.getBookingById(1, randomUserId));
    }

    @Test
    void addBooking_shouldCallRepository() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).build();
        Booking booking = Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        bookingService.addBooking(booking);

        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void addBooking_shouldThrowException_ifBookerIdEqualsOwnerId() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(user.getId()).build();
        Booking booking = Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(ForbiddenException.class, () -> bookingService.addBooking(booking));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_shouldThrowException_ifItemNotAvailable() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).available(false).build();
        Booking booking = Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(ValidationException.class, () -> bookingService.addBooking(booking));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void addBooking_shouldThrowException_ifBookingEndTimeEarlierThanStartTime() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).available(true).build();
        Booking booking = Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().minusDays(1))
                .build();

        assertThrows(ValidationException.class, () -> bookingService.addBooking(booking));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateStatus_shouldUpdateStatus_ifStatusWaiting() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(user.getId()).available(true).build();
        Booking bookingToApprove = Booking.builder()
                .id(1L)
                .booker(user)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
        Booking bookingToReject = Booking.builder()
                .id(2L)
                .booker(user)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
        when(bookingRepository.findById(bookingToApprove.getId())).thenReturn(Optional.of(bookingToApprove));
        when(bookingRepository.findById(bookingToReject.getId())).thenReturn(Optional.of(bookingToReject));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(InjectMocks -> InjectMocks.getArgument(0));

        bookingService.updateStatus(user.getId(), bookingToApprove.getId(), true);
        bookingService.updateStatus(user.getId(), bookingToReject.getId(), false);

        assertEquals(BookingStatus.APPROVED, bookingToApprove.getStatus());
        assertEquals(BookingStatus.REJECTED, bookingToReject.getStatus());
    }

    @Test
    void updateStatus_shouldThrowException_ifStatusNotWaiting() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(user.getId()).available(true).build();
        Booking booking = Booking.builder()
                .booker(user)
                .item(item)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.updateStatus(1, 1, false));
    }

    @Test
    void updateStatus_shouldThrowException_ifBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.updateStatus(1, 1, false));
    }

    @Test
    void updateStatus_shouldThrowException_ifUserNotOwner() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).available(true).build();
        Booking booking = Booking.builder()
                .booker(user)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class, () -> bookingService.updateStatus(1, 1, false));
    }

    @Test
    void getBookingsByBookerId_shouldCallBookingRepository() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        bookingService.getBookingsByBookerId(1, "CURRENT", 0, 5);

        verify(bookingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBookingsByBookerId_shouldThrowException_ifUserNotFound() {
        when(userRepository.existsById(anyLong())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByBookerId(1, "CURRENT", 0, 5));
        verify(bookingRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBookingsByBookerId_shouldThrowException_ifUnknownState() {
        String unknownStateName = "UNKNOWN";
        when(userRepository.existsById(anyLong())).thenReturn(true);

        assertThrows(ValidationException.class, () -> bookingService.getBookingsByBookerId(1, unknownStateName, 0, 5));
        verify(bookingRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBookingsByOwnerId_ShouldCallRepository() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        bookingService.getBookingsByOwnerId(1, "CURRENT", 0, 5);

        verify(bookingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getLastItemBooking_shouldCallRepository() {
        bookingService.getLastItemBooking(1);
        verify(bookingRepository, times(1)).findLastItemBooking(1);
    }

    @Test
    void getNextItemBooking_shouldCallRepository() {
        bookingService.getNextItemBooking(1);
        verify(bookingRepository, times(1)).findNextItemBooking(1);
    }
}