package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, long requesterId) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requesterId(requesterId)
                .created(LocalDateTime.now())
                .build();
    }

    public ItemRequestDto toDto(ItemRequest itemRequest, List<Item> items) {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(Collections.emptyList())
                .build();

        if (!items.isEmpty()) {
            itemRequestDto.setItems(items.stream().map(itemMapper::toDto).collect(Collectors.toList()));
        }
        return itemRequestDto;
    }

    public ItemRequestDto toDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }
}
