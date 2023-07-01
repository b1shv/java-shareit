package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void getBookingById_shouldReturnBooking() {
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).build();
        Booking booking = Booking.builder().booker(user).item(item).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThat(bookingService.getBookingById(1, user.getId())).isEqualTo(booking);
        assertThat(bookingService.getBookingById(1, item.getOwnerId())).isEqualTo(booking);
    }

    @Test
    void getBookingById_shouldThrowException_ifBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1, 1));
    }

    @Test
    void getBookingById_shouldThrowException_ifUserIsNotOwnerAndNotBooker() {
        long randomUserId = 143;
        User user = User.builder().id(1).build();
        Item item = Item.builder().ownerId(2).build();
        Booking booking = Booking.builder().booker(user).item(item).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1, randomUserId))
                .isInstanceOf(ForbiddenException.class);
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

        assertThatThrownBy(() -> bookingService.addBooking(booking)).isInstanceOf(ForbiddenException.class);
        verify(bookingRepository, never()).save(booking);
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

        assertThatThrownBy(() -> bookingService.addBooking(booking)).isInstanceOf(ValidationException.class);
        verify(bookingRepository, never()).save(booking);
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

        assertThatThrownBy(() -> bookingService.addBooking(booking)).isInstanceOf(ValidationException.class);
        verify(bookingRepository, never()).save(booking);
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

        assertThat(bookingToApprove.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingToReject.getStatus()).isEqualTo(BookingStatus.REJECTED);
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
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(1, 1, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateStatus_shouldThrowException_ifBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateStatus(1, 1, false))
                .isInstanceOf(NotFoundException.class);
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
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(1, 1, false))
                .isInstanceOf(ForbiddenException.class);
        ;
    }

    @Test
    void getBookingsByBookerId_shouldCallBookingRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        bookingService.getBookingsByBookerId(1, "CURRENT",
                PageRequest.of(0, 10, Sort.by("start").descending()));

        verify(bookingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBookingsByBookerId_shouldThrowException_ifUserNotFound() {
        when(userRepository.existsById(1L)).thenThrow(new NotFoundException());

        assertThatThrownBy(() -> bookingService.getBookingsByBookerId(
                1, "CURRENT", PageRequest.of(0, 5, Sort.by("start").descending())))
                .isInstanceOf(NotFoundException.class);
        verify(bookingRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBookingsByBookerId_shouldThrowException_ifUnknownState() {
        String unknownStateName = "UNKNOWN";
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.getBookingsByBookerId(
                1, unknownStateName, PageRequest.of(0, 10, Sort.by("start").descending())))
                .isInstanceOf(ValidationException.class);
        verify(bookingRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getBookingsByOwnerId_ShouldCallRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        bookingService.getBookingsByOwnerId(1, "CURRENT",
                PageRequest.of(0, 5, Sort.by("start").descending()));

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