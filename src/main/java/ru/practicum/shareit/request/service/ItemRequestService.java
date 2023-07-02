package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequest> getAllRequests(long userId, Pageable pageable);

    List<ItemRequest> getAllByRequesterId(long requesterId);

    ItemRequest getRequestById(long userId, long requestId);

    ItemRequest addRequest(ItemRequest itemRequest);
}
