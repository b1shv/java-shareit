package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext
class BookingRepositoryIntegrationTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void setup(@Autowired BookingRepository bookingRepository,
                      @Autowired ItemRepository itemRepository,
                      @Autowired UserRepository userRepository) {
        User owner = userRepository.save(User.builder().id(1).name("User 1").email("user1@email.com").build());
        User booker = userRepository.save(User.builder().id(2).name("User 2").email("user2@email.com").build());
        Item item1 = itemRepository.save(Item.builder()
                .id(1)
                .name("Item 1")
                .available(true)
                .description("Description")
                .ownerId(owner.getId())
                .build());
        Item item2 = itemRepository.save(Item.builder()
                .id(2)
                .name("Item 2")
                .available(true)
                .description("Description")
                .ownerId(owner.getId())
                .build());
        bookingRepository.save(Booking.builder()
                .id(1L)
                .item(item1)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(20))
                .end(LocalDateTime.now().minusDays(15))
                .build());
        bookingRepository.save(Booking.builder()
                .id(2L)
                .item(item1)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build());
        bookingRepository.save(Booking.builder()
                .id(3L)
                .item(item1)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(12))
                .build());
        bookingRepository.save(Booking.builder()
                .id(4L)
                .item(item1)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
        bookingRepository.save(Booking.builder()
                .id(5L)
                .item(item1)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .build());
        bookingRepository.save(Booking.builder()
                .id(6L)
                .item(item1)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(7))
                .build());
        bookingRepository.save(Booking.builder()
                .id(7L)
                .item(item2)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build());
    }

    @Test
    void findLastItemBooking() {
        assertThat(bookingRepository.findLastItemBooking(1)).isEqualTo(bookingRepository.findById(5L).get());
        assertThat(bookingRepository.findLastItemBooking(2)).isNull();
    }

    @Test
    void findNextItemBooking() {
        assertThat(bookingRepository.findNextItemBooking(1)).isEqualTo(bookingRepository.findById(6L).get());
        assertThat(bookingRepository.findNextItemBooking(2)).isEqualTo(bookingRepository.findById(7L).get());
    }

    @Test
    void findAllByItemIdAndBookerIdAndStatusAndEndBefore() {
        List<Booking> expected = List.of(bookingRepository.findById(1L).get(), bookingRepository.findById(5L).get());
        List<Booking> actual = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                1, 2, BookingStatus.APPROVED, LocalDateTime.now());

        assertThat(actual).isEqualTo(expected);
    }
}