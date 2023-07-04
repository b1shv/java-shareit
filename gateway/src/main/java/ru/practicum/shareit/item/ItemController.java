package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(USER_ID) long ownerId,
                                         @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                         @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Get items by owner, userId={}, from={}, size={}", ownerId, from, size);
        return itemClient.getAllItemsByOwner(ownerId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID) long userId,
                                          @PathVariable long itemId) {
        log.info("Get item by ID, userId={}, itemId={}", userId, itemId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchText(@RequestHeader(USER_ID) long userId,
                                             @RequestParam String text,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                             @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Search text, text={}, from={}, size={}", text, from, size);
        return itemClient.searchText(userId, text, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_ID) long userId,
                                      @RequestBody @Valid ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID) long ownerId,
                                         @PathVariable long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Updating item {}, itemId={}, ownerId={}", itemDto, itemId, ownerId);
        return itemClient.updateItem(itemDto, itemId, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID) long authorId,
                                             @PathVariable long itemId,
                                             @RequestBody @Valid CommentDto commentDto) {
        log.info("Creating comment {}, itemId={}, authorId={}");
        return itemClient.addComment(commentDto, itemId, authorId);
    }
}