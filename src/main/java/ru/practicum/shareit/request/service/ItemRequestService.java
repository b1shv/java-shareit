package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequest> getAllRequests(long userId, int from, int size);

    List<ItemRequest> getAllByRequesterId(long requesterId);

    ItemRequest getRequestById(long userId, long requestId);

    ItemRequest addRequest(ItemRequest itemRequest);
}
