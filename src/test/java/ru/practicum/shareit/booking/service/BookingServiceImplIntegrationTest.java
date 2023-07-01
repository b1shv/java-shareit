package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10, Sort.by("start").descending());
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    @BeforeAll
    static void setup(@Autowired UserRepository userRepository,
                      @Autowired ItemRepository itemRepository,
                      @Autowired BookingService bookingService) {
        User user1 = userRepository.save(User.builder().name("User1").email("user1@email.com").build());
        User user2 = userRepository.save(User.builder().name("User2").email("user2@email.com").build());
        Item item1 = itemRepository.save(Item.builder()
                .name("Item1")
                .description("Description")
                .available(true)
                .ownerId(user1.getId())
                .build());
        Item item2 = itemRepository.save(Item.builder()
                .name("Item2")
                .description("Description")
                .available(true)
                .ownerId(user1.getId())
                .build());
        Item item3 = itemRepository.save(Item.builder()
                .name("Item3")
                .description("Description")
                .available(true)
                .ownerId(user2.getId())
                .build());
        bookingService.addBooking(Booking.builder()
                .id(1L)
                .booker(user2)
                .item(item1)
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(9))
                .status(BookingStatus.APPROVED)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(2L)
                .booker(user2)
                .item(item1)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.APPROVED)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(3L)
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.APPROVED)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(4L)
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(7))
                .status(BookingStatus.APPROVED)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(5L)
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(6))
                .end(LocalDateTime.now().plusDays(8))
                .status(BookingStatus.WAITING)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(6L)
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(12))
                .end(LocalDateTime.now().plusDays(13))
                .status(BookingStatus.REJECTED)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(7L)
                .booker(user1)
                .item(item3)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .status(BookingStatus.APPROVED)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(8L)
                .booker(user1)
                .item(item3)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .status(BookingStatus.REJECTED)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(9L)
                .booker(user2)
                .item(item1)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.WAITING)
                .build());
        bookingService.addBooking(Booking.builder()
                .id(10L)
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.APPROVED)
                .build());
    }

    @Test
    void getBookingsByBookerId_shouldReturnAll_ifStateAll() {
        List<Booking> expectedUser1 = List.of(bookingRepository.findById(7L).get(), bookingRepository.findById(8L).get());
        List<Booking> expectedUser2 = Stream.of(
                        bookingRepository.findById(1L).get(),
                        bookingRepository.findById(2L).get(),
                        bookingRepository.findById(3L).get(),
                        bookingRepository.findById(4L).get(),
                        bookingRepository.findById(5L).get(),
                        bookingRepository.findById(6L).get(),
                        bookingRepository.findById(9L).get(),
                        bookingRepository.findById(10L).get())
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actualUser1 = bookingService.getBookingsByBookerId(1, BookingState.ALL.name(), DEFAULT_PAGEABLE);
        List<Booking> actualUser2 = bookingService.getBookingsByBookerId(2, BookingState.ALL.name(), DEFAULT_PAGEABLE);

        assertThat(actualUser1).isEqualTo(expectedUser1);
        assertThat(actualUser2).isEqualTo(expectedUser2);
    }

    @Test
    void getBookingsByBookerId_shouldReturnCurrent_ifStateCurrent() {
        List<Booking> expected = List.of(bookingRepository.findById(3L).get(), bookingRepository.findById(2L).get());
        List<Booking> actual = bookingService.getBookingsByBookerId(2, BookingState.CURRENT.name(), DEFAULT_PAGEABLE);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getBookingsByBookerId_shouldReturnPast_ifStatePast() {
        List<Booking> expected = Stream.of(
                        bookingRepository.findById(1L).get(),
                        bookingRepository.findById(2L).get(),
                        bookingRepository.findById(3L).get(),
                        bookingRepository.findById(4L).get(),
                        bookingRepository.findById(5L).get(),
                        bookingRepository.findById(6L).get(),
                        bookingRepository.findById(9L).get(),
                        bookingRepository.findById(10L).get())
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(2, BookingState.PAST.name(),
                DEFAULT_PAGEABLE);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getBookingsByBookerId_shouldReturnFuture_ifStateFuture() {
        List<Booking> expected = Stream.of(
                        bookingRepository.findById(1L).get(),
                        bookingRepository.findById(2L).get(),
                        bookingRepository.findById(3L).get(),
                        bookingRepository.findById(4L).get(),
                        bookingRepository.findById(5L).get(),
                        bookingRepository.findById(6L).get(),
                        bookingRepository.findById(9L).get(),
                        bookingRepository.findById(10L).get())
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(2, BookingState.FUTURE.name(),
                DEFAULT_PAGEABLE);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getBookingsByBookerId_shouldReturnWaiting_ifStateWaiting() {
        List<Booking> expected = Stream.of(
                        bookingRepository.findById(1L).get(),
                        bookingRepository.findById(2L).get(),
                        bookingRepository.findById(3L).get(),
                        bookingRepository.findById(4L).get(),
                        bookingRepository.findById(5L).get(),
                        bookingRepository.findById(6L).get(),
                        bookingRepository.findById(9L).get(),
                        bookingRepository.findById(10L).get())
                .filter(booking -> booking.getStatus().equals(BookingStatus.WAITING))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(2, BookingState.WAITING.name(),
                DEFAULT_PAGEABLE);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getBookingsByBookerId_shouldReturnRejected_ifStateRejected() {
        List<Booking> expected = Stream.of(
                        bookingRepository.findById(1L).get(),
                        bookingRepository.findById(2L).get(),
                        bookingRepository.findById(3L).get(),
                        bookingRepository.findById(4L).get(),
                        bookingRepository.findById(5L).get(),
                        bookingRepository.findById(6L).get(),
                        bookingRepository.findById(9L).get(),
                        bookingRepository.findById(10L).get())
                .filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(2, BookingState.REJECTED.name(),
                DEFAULT_PAGEABLE);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getBookingsByOwnerId_shouldReturnAll_ifStateAll() {
        List<Booking> expectedUser1 = Stream.of(
                        bookingRepository.findById(1L).get(),
                        bookingRepository.findById(2L).get(),
                        bookingRepository.findById(3L).get(),
                        bookingRepository.findById(4L).get(),
                        bookingRepository.findById(5L).get(),
                        bookingRepository.findById(6L).get(),
                        bookingRepository.findById(9L).get(),
                        bookingRepository.findById(10L).get())
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> expectedUser2 = List.of(bookingRepository.findById(7L).get(), bookingRepository.findById(8L).get());
        List<Booking> actualUser1 = bookingService.getBookingsByOwnerId(1, BookingState.ALL.name(),
                DEFAULT_PAGEABLE);
        List<Booking> actualUser2 = bookingService.getBookingsByOwnerId(2, BookingState.ALL.name(),
                DEFAULT_PAGEABLE);

        assertThat(actualUser1).isEqualTo(expectedUser1);
        assertThat(actualUser2).isEqualTo(expectedUser2);
    }
}