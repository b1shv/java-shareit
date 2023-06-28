package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item1;
    private Item item2;
    private Booking booking1;
    private Booking booking2;
    private Booking booking3;
    private Booking booking4;
    private Booking booking5;
    private Booking booking6;
    private Booking booking7;

    @BeforeEach
    void setup() {
        owner = userRepository.save(User.builder().name("User 1").email("user1@email.com").build());
        booker = userRepository.save(User.builder().name("User 2").email("user2@email.com").build());
        item1 = itemRepository.save(Item.builder()
                .name("Item 1")
                .available(true)
                .description("Description")
                .ownerId(owner.getId())
                .build());
        item2 = itemRepository.save(Item.builder()
                .name("Item 2")
                .available(true)
                .description("Description")
                .ownerId(owner.getId())
                .build());
        booking1 = bookingRepository.save(Booking.builder()
                .item(item1)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(20))
                .end(LocalDateTime.now().minusDays(15))
                .build());
        booking2 = bookingRepository.save(Booking.builder()
                .item(item1)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build());
        booking3 = bookingRepository.save(Booking.builder()
                .item(item1)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(12))
                .build());
        booking4 = bookingRepository.save(Booking.builder()
                .item(item1)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
        booking5 = bookingRepository.save(Booking.builder()
                .item(item1)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .build());
        booking6 = bookingRepository.save(Booking.builder()
                .item(item1)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(7))
                .build());
        booking7 = bookingRepository.save(Booking.builder()
                .item(item2)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
    }

    @Test
    void findLastItemBooking() {
        assertEquals(booking5, bookingRepository.findLastItemBooking(item1.getId()));
        assertNull(bookingRepository.findLastItemBooking(item2.getId()));
    }

    @Test
    void findNextItemBooking() {
        assertEquals(booking6, bookingRepository.findNextItemBooking(item1.getId()));
        assertEquals(booking7, bookingRepository.findNextItemBooking(item2.getId()));
    }

    @Test
    void findAllByItemIdAndBookerIdAndStatusAndEndBefore() {
        List<Booking> expected = List.of(booking1, booking5);
        List<Booking> actual = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                item1.getId(), booker.getId(), BookingStatus.APPROVED, LocalDateTime.now());

        assertEquals(expected, actual);
    }
}