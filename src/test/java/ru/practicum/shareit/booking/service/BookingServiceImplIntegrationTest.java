package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {
    @Autowired
    private final BookingService bookingService;

    @Autowired
    private final BookingRepository bookingRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ItemRepository itemRepository;

    private static User user1;
    private static User user2;
    private static Item item1;
    private static Item item2;
    private static Item item3;
    private static Booking booking1;
    private static Booking booking2;
    private static Booking booking3;
    private static Booking booking4;
    private static Booking booking5;
    private static Booking booking6;
    private static Booking booking7;
    private static Booking booking8;
    private static Booking booking9;
    private static Booking booking10;

    @BeforeAll
    static void setup(@Autowired UserRepository userRepository,
                      @Autowired ItemRepository itemRepository,
                      @Autowired BookingService bookingService) {
        user1 = userRepository.save(User.builder().name("User1").email("user1@email.com").build());
        user2 = userRepository.save(User.builder().name("User2").email("user2@email.com").build());
        item1 = itemRepository.save(Item.builder()
                .name("Item1")
                .description("Description")
                .available(true)
                .ownerId(user1.getId())
                .build());
        item2 = itemRepository.save(Item.builder()
                .name("Item2")
                .description("Description")
                .available(true)
                .ownerId(user1.getId())
                .build());
        item3 = itemRepository.save(Item.builder()
                .name("Item3")
                .description("Description")
                .available(true)
                .ownerId(user2.getId())
                .build());
        booking1 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item1)
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(9))
                .status(BookingStatus.APPROVED)
                .build());
        booking2 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item1)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.APPROVED)
                .build());
        booking3 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.APPROVED)
                .build());
        booking4 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(7))
                .status(BookingStatus.APPROVED)
                .build());
        booking5 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(6))
                .end(LocalDateTime.now().plusDays(8))
                .status(BookingStatus.WAITING)
                .build());
        booking6 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(12))
                .end(LocalDateTime.now().plusDays(13))
                .status(BookingStatus.REJECTED)
                .build());
        booking7 = bookingService.addBooking(Booking.builder()
                .booker(user1)
                .item(item3)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .status(BookingStatus.APPROVED)
                .build());
        booking8 = bookingService.addBooking(Booking.builder()
                .booker(user1)
                .item(item3)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .status(BookingStatus.REJECTED)
                .build());
        booking9 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item1)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.WAITING)
                .build());
        booking10 = bookingService.addBooking(Booking.builder()
                .booker(user2)
                .item(item2)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.APPROVED)
                .build());
    }

    @Test
    void getBookingsByBookerId_shouldReturnAll_ifStateAll() {
        List<Booking> expectedUser1 = List.of(booking7, booking8);
        List<Booking> expectedUser2 = Stream.of(
                        booking1, booking2, booking3, booking4, booking5, booking6, booking9, booking10)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actualUser1 = bookingService.getBookingsByBookerId(user1.getId(), BookingState.ALL.name(), 0, 10);
        List<Booking> actualUser2 = bookingService.getBookingsByBookerId(user2.getId(), BookingState.ALL.name(), 0, 10);

        assertEquals(expectedUser1, actualUser1);
        assertEquals(expectedUser2, actualUser2);
    }

    @Test
    void getBookingsByBookerId_shouldReturnCurrent_ifStateCurrent() {
        List<Booking> expected = List.of(booking3, booking2);
        List<Booking> actual = bookingService.getBookingsByBookerId(user2.getId(), BookingState.CURRENT.name(), 0, 10);

        assertEquals(expected, actual);
    }

    @Test
    void getBookingsByBookerId_shouldReturnPast_ifStatePast() {
        List<Booking> expected = Stream.of(
                        booking1, booking2, booking3, booking4, booking5, booking6, booking9, booking10)
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(user2.getId(), BookingState.PAST.name(), 0, 10);

        assertEquals(expected, actual);
    }

    @Test
    void getBookingsByBookerId_shouldReturnFuture_ifStateFuture() {
        List<Booking> expected = Stream.of(
                        booking1, booking2, booking3, booking4, booking5, booking6, booking9, booking10)
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(user2.getId(), BookingState.FUTURE.name(), 0, 10);

        assertEquals(expected, actual);
    }

    @Test
    void getBookingsByBookerId_shouldReturnWaiting_ifStateWaiting() {
        List<Booking> expected = Stream.of(
                        booking1, booking2, booking3, booking4, booking5, booking6, booking9, booking10)
                .filter(booking -> booking.getStatus().equals(BookingStatus.WAITING))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(user2.getId(), BookingState.WAITING.name(), 0, 10);

        assertEquals(expected, actual);
    }

    @Test
    void getBookingsByBookerId_shouldReturnRejected_ifStateRejected() {
        List<Booking> expected = Stream.of(
                        booking1, booking2, booking3, booking4, booking5, booking6, booking9, booking10)
                .filter(booking -> booking.getStatus().equals(BookingStatus.REJECTED))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> actual = bookingService.getBookingsByBookerId(user2.getId(), BookingState.REJECTED.name(), 0, 10);

        assertEquals(expected, actual);
    }

    @Test
    void getBookingsByOwnerId_shouldReturnAll_ifStateAll() {
        List<Booking> expectedUser1 = Stream.of(
                        booking1, booking2, booking3, booking4, booking5, booking6, booking9, booking10)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        List<Booking> expectedUser2 = List.of(booking7, booking8);
        List<Booking> actualUser1 = bookingService.getBookingsByOwnerId(user1.getId(), BookingState.ALL.name(), 0, 10);
        List<Booking> actualUser2 = bookingService.getBookingsByOwnerId(user2.getId(), BookingState.ALL.name(), 0, 10);

        assertEquals(expectedUser1, actualUser1);
        assertEquals(expectedUser2, actualUser2);
    }

    @AfterAll
    static void shutdown(@Autowired UserRepository userRepository,
                         @Autowired ItemRepository itemRepository,
                         @Autowired BookingRepository bookingRepository) {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}