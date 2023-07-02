package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperTest {
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestMapper itemRequestMapper;

    @Test
    void toItemRequest() {
        ItemRequest itemRequest = ItemRequest.builder()
                .requesterId(3)
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .description("Text")
                .build();
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Text")
                .build();

        ItemRequest fromMapper = itemRequestMapper.toItemRequest(itemRequestDto, 3);
        itemRequest.setCreated(fromMapper.getCreated());

        assertThat(fromMapper).isEqualTo(itemRequest);
    }

    @Test
    void toDtoWithoutItems() {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1)
                .requesterId(3)
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .description("Text")
                .build();
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1)
                .description("Text")
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .build();

        assertThat(itemRequestMapper.toDto(itemRequest)).isEqualTo(itemRequestDto);
    }

    @Test
    void toDtoWithItems() {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1)
                .requesterId(3)
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .description("Text")
                .build();
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1)
                .description("Text")
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .items(Collections.emptyList())
                .build();
        List<Item> items = List.of(Item.builder().build(), Item.builder().build());
        when(itemMapper.toDto(any(Item.class))).thenReturn(ItemDto.builder().build());

        assertThat(itemRequestMapper.toDto(itemRequest, Collections.emptyList())).isEqualTo(itemRequestDto);

        itemRequestDto.setItems(items.stream().map(itemMapper::toDto).collect(Collectors.toList()));

        assertThat(itemRequestMapper.toDto(itemRequest, items)).isEqualTo(itemRequestDto);
    }
}