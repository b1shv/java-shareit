package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private static final String USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(USER_ID) long ownerId) {
        log.debug("GET request: all items of user {}", ownerId);
        return itemService.getItemsByOwnerId(ownerId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable long itemId) {
        log.debug("GET request: item with ID {}", itemId);
        return itemMapper.toDto(itemService.getItemById(itemId));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.debug("GET request: searching for text");
        return itemService.searchText(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID) long ownerId,
                       @Valid @RequestBody ItemDto itemDto) {
        log.debug("POST request: new item, owner: {}", ownerId);
        return itemMapper.toDto(itemService.addItem(itemMapper.toItem(itemDto, ownerId)));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID) long ownerId,
                          @PathVariable long itemId,
                          @RequestBody ItemDto itemDto) {
        log.debug("PATCH request: updating item {}", itemId);
        Item item = itemMapper.toItem(itemDto, ownerId);
        item.setId(itemId);
        return itemMapper.toDto(itemService.updateItem(item));
    }
}
