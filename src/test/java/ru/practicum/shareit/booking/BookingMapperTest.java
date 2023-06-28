package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private BookingMapper bookingMapper;

    private User user;
    private UserDto userDto;
    private Item item;
    private ItemDto itemDto;
    private Booking booking;
    private BookingDto bookingDto;
    private BookingForItemDto bookingForItemDto;

    @BeforeEach
    void setup() {
        user = User.builder().id(12).build();
        userDto = UserDto.builder().id(12).build();
        item = Item.builder().id(3).build();
        itemDto = ItemDto.builder().id(3).build();
        booking = Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.of(2023, 1, 1, 12, 0))
                .end(LocalDateTime.of(2023, 1, 2, 12, 0))
                .status(BookingStatus.WAITING)
                .build();
        bookingDto = BookingDto.builder()
                .start(LocalDateTime.of(2023, 1, 1, 12, 0))
                .end(LocalDateTime.of(2023, 1, 2, 12, 0))
                .build();
        bookingForItemDto = BookingForItemDto.builder()
                .id(23L)
                .bookerId(user.getId())
                .itemId(item.getId())
                .start(LocalDateTime.of(2023, 1, 1, 12, 0))
                .end(LocalDateTime.of(2023, 1, 2, 12, 0))
                .build();
    }

    @Test
    void toBooking() {
        assertEquals(booking, bookingMapper.toBooking(bookingDto, item, user));
    }

    @Test
    void toDto() {
        booking.setId(23L);
        bookingDto.setId(23L);
        booking.setStatus(BookingStatus.APPROVED);
        bookingDto.setStatus(BookingStatus.APPROVED);
        bookingDto.setBooker(userDto);
        bookingDto.setItem(itemDto);
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        assertEquals(bookingDto, bookingMapper.toDto(booking));
    }

    @Test
    void toDtoForItem() {
        booking.setId(23L);

        assertEquals(bookingForItemDto, bookingMapper.toDtoForItem(booking));
        assertDoesNotThrow(() -> bookingMapper.toDtoForItem(null));
    }
}