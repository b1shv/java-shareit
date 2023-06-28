package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemMapperTest {
    private final ItemMapper itemMapper = new ItemMapper();
    private Item item;
    private ItemDto itemDto;
    List<CommentDto> comments = List.of(CommentDto.builder().id(100).build());

    @BeforeEach
    void setup() {
        item = Item.builder()
                .id(1)
                .name("Item")
                .ownerId(22)
                .available(true)
                .description("text")
                .requestId(34L)
                .build();
        itemDto = ItemDto.builder()
                .id(1)
                .name("Item")
                .available(true)
                .description("text")
                .requestId(34L)
                .build();
    }

    @Test
    void toDto() {
        ItemDto fromMapper = itemMapper.toDto(item);

        assertEquals(itemDto, fromMapper);
    }

    @Test
    void toDtoWithComments() {
        itemDto.setComments(comments);
        ItemDto fromMapper = itemMapper.toDto(item, comments);

        assertEquals(itemDto, fromMapper);
    }

    @Test
    void toDtoWithCommentsAndBookings() {
        BookingForItemDto lastBooking = BookingForItemDto.builder().id(75).build();
        BookingForItemDto nextBooking = BookingForItemDto.builder().id(88).build();
        itemDto.setComments(comments);
        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);
        ItemDto fromMapper = itemMapper.toDto(item, lastBooking, nextBooking, comments);

        assertEquals(itemDto, fromMapper);
    }

    @Test
    void toItem() {
        Item fromMapper = itemMapper.toItem(itemDto, 22);

        assertEquals(item, fromMapper);
    }
}