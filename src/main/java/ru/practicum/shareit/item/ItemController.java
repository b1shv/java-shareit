package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private static final String DEFAULT_FROM = "0";
    private static final String DEFAULT_SIZE = "10";
    private static final String USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(USER_ID) long ownerId,
                                @PositiveOrZero @RequestParam(defaultValue = DEFAULT_FROM) int from,
                                @Positive @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        log.debug("GET request: all items of user {}", ownerId);
        return itemService.getItemsByOwnerId(ownerId, from, size).stream()
                .map(item -> itemMapper.toDto(item,
                        bookingMapper.toDtoForItem(bookingService.getLastItemBooking(item.getId())),
                        bookingMapper.toDtoForItem(bookingService.getNextItemBooking(item.getId())),
                        commentMapper.toDto(itemService.getComments(item.getId())))
                )
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(USER_ID) long userId,
                           @PathVariable long itemId) {
        log.debug("GET request: item ID {}", itemId);
        Item item = itemService.getItemById(itemId);

        if (item.getOwnerId() != userId) {
            return itemMapper.toDto(item, commentMapper.toDto(itemService.getComments(item.getId())));
        }

        return itemMapper.toDto(item,
                bookingMapper.toDtoForItem(bookingService.getLastItemBooking(item.getId())),
                bookingMapper.toDtoForItem(bookingService.getNextItemBooking(item.getId())),
                commentMapper.toDto(itemService.getComments(item.getId())));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @PositiveOrZero @RequestParam(defaultValue = DEFAULT_FROM) int from,
                                @Positive @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        log.debug("GET request: searching for text");
        return itemService.searchText(text, from, size).stream()
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
        log.debug("PATCH request: updating item ID {}", itemId);
        Item item = itemMapper.toItem(itemDto, ownerId);
        item.setId(itemId);
        return itemMapper.toDto(itemService.updateItem(item));
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID) long authorId,
                                 @PathVariable long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        log.debug("POST request: new comment for item ID {}, author: ID {}", itemId, authorId);
        User author = userService.getUserById(authorId);
        Item item = itemService.getItemById(itemId);
        return commentMapper.toDto(itemService.addComment(
                commentMapper.toComment(commentDto, author, item, LocalDateTime.now())));
    }
}
