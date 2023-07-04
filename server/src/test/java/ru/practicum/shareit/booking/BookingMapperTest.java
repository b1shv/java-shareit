package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private BookingMapper bookingMapper;

    @Test
    void toBooking() {
        User booker = user(11);
        Item item = item(22);
        Booking booking = booking(booker, item);
        BookingDto bookingDto = bookingDto();

        assertThat(bookingMapper.toBooking(bookingDto, item, booker)).isEqualTo(booking);
    }

    @Test
    void toDto() {

        User booker = user(1);
        Item item = item(2);
        Booking booking = booking(booker, item);
        UserDto userDto = userDto(1);
        ItemDto itemDto = itemDto(2);
        BookingDto bookingDto = bookingDto();

        booking.setId(23L);
        bookingDto.setId(23L);
        booking.setStatus(BookingStatus.APPROVED);
        bookingDto.setStatus(BookingStatus.APPROVED);
        bookingDto.setBooker(userDto);
        bookingDto.setItem(itemDto);
        when(userMapper.toDto(booker)).thenReturn(userDto);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        assertThat(bookingMapper.toDto(booking)).isEqualTo(bookingDto);
    }

    @Test
    void toDtoForItem() {
        User booker = user(1);
        Item item = item(2);
        Booking booking = booking(booker, item);
        booking.setId(23L);
        BookingForItemDto bookingForItemDto = bookingForItemDto(23L, booker, item);

        assertThat(bookingMapper.toDtoForItem(booking)).isEqualTo(bookingForItemDto);
    }

    private User user(long id) {
        return User.builder().id(id).build();
    }

    private UserDto userDto(long id) {
        return UserDto.builder().id(id).build();
    }

    private Item item(long id) {
        return Item.builder().id(id).build();
    }

    private ItemDto itemDto(long id) {
        return ItemDto.builder().id(id).build();
    }

    private Booking booking(User booker, Item item) {
        return Booking.builder()
                .booker(booker)
                .item(item)
                .start(LocalDateTime.of(2023, 1, 1, 12, 0))
                .end(LocalDateTime.of(2023, 1, 2, 12, 0))
                .status(BookingStatus.WAITING)
                .build();
    }

    private BookingDto bookingDto() {
        return BookingDto.builder()
                .start(LocalDateTime.of(2023, 1, 1, 12, 0))
                .end(LocalDateTime.of(2023, 1, 2, 12, 0))
                .build();
    }

    private BookingForItemDto bookingForItemDto(long id, User booker, Item item) {
        return BookingForItemDto.builder()
                .id(id)
                .bookerId(booker.getId())
                .itemId(item.getId())
                .start(LocalDateTime.of(2023, 1, 1, 12, 0))
                .end(LocalDateTime.of(2023, 1, 2, 12, 0))
                .build();
    }
}