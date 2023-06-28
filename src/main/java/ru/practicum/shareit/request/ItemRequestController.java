package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemService itemService;
    private static final String USER_ID = "X-Sharer-User-Id";

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(USER_ID) long userId,
                                       @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                       @Positive @RequestParam(defaultValue = "10") int size) {
        log.debug("GET request: all item requests; from = {}, size = {}, user ID = {}", from, size, userId);
        return itemRequestService.getAllRequests(userId, from, size).stream()
                .map(itemRequest ->
                        itemRequestMapper.toDto(itemRequest, itemService.getItemsByRequestId(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequester(@RequestHeader(USER_ID) long requesterId) {
        log.debug("GET request: all item requests of requester ID {}", requesterId);
        return itemRequestService.getAllByRequesterId(requesterId).stream()
                .map(itemRequest ->
                        itemRequestMapper.toDto(itemRequest, itemService.getItemsByRequestId(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(USER_ID) long userId,
                                  @PathVariable long requestId) {
        log.debug("GET request: single item request ID {}, user ID {}", requestId, userId);
        return itemRequestMapper.toDto(itemRequestService.getRequestById(userId, requestId),
                itemService.getItemsByRequestId(requestId));
    }

    @PostMapping
    public ItemRequestDto add(@RequestHeader(USER_ID) long userId,
                              @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.debug("POST request: new item request from user ID {}", userId);
        return itemRequestMapper.toDto(
                itemRequestService.addRequest(itemRequestMapper.toItemRequest(itemRequestDto, userId)));
    }
}
