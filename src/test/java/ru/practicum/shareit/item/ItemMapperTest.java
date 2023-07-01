package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {
    private final ItemMapper itemMapper = new ItemMapper();

    @Test
    void toDto() {
        ItemDto itemDto = itemDto();
        Item item = item();

        ItemDto fromMapper = itemMapper.toDto(item);

        assertThat(fromMapper).isEqualTo(itemDto);
    }

    @Test
    void toDtoWithComments() {
        ItemDto itemDto = itemDto();
        Item item = item();
        List<CommentDto> comments = List.of(CommentDto.builder().id(100).build());

        itemDto.setComments(comments);
        ItemDto fromMapper = itemMapper.toDto(item, comments);

        assertThat(fromMapper).isEqualTo(itemDto);
    }

    @Test
    void toDtoWithCommentsAndBookings() {
        ItemDto itemDto = itemDto();
        Item item = item();
        List<CommentDto> comments = List.of(CommentDto.builder().id(100).build());
        BookingForItemDto lastBooking = BookingForItemDto.builder().id(75).build();
        BookingForItemDto nextBooking = BookingForItemDto.builder().id(88).build();
        itemDto.setComments(comments);
        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);

        ItemDto fromMapper = itemMapper.toDto(item, lastBooking, nextBooking, comments);

        assertThat(fromMapper).isEqualTo(itemDto);
    }

    @Test
    void toItem() {
        ItemDto itemDto = itemDto();
        Item item = item();

        Item fromMapper = itemMapper.toItem(itemDto, 22);

        assertThat(fromMapper).isEqualTo(item);
    }

    private Item item() {
        return Item.builder()
                .id(1)
                .name("Item")
                .ownerId(22)
                .available(true)
                .description("text")
                .requestId(34L)
                .build();
    }

    private ItemDto itemDto() {
        return ItemDto.builder()
                .id(1)
                .name("Item")
                .available(true)
                .description("text")
                .requestId(34L)
                .build();
    }
}