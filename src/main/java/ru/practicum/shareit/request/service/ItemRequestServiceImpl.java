package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemRequest> getAllRequests(long userId, int from, int size) {
        return itemRequestRepository.findAllByRequesterIdNot(userId, countPageAndSort(from, size));
    }

    @Override
    public List<ItemRequest> getAllByRequesterId(long requesterId) {
        if (!userRepository.existsById(requesterId)) {
            throw new NotFoundException(String.format("User ID %d is not found", requesterId));
        }
        return itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId);
    }

    @Override
    public ItemRequest getRequestById(long userId, long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User ID %d is not found", userId));
        }
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request ID %d is not found", requestId)));
    }

    @Override
    public ItemRequest addRequest(ItemRequest itemRequest) {
        if (!userRepository.existsById(itemRequest.getRequesterId())) {
            throw new NotFoundException(String.format("User ID %d is not found", itemRequest.getRequesterId()));
        }

        return itemRequestRepository.save(itemRequest);
    }

    private Pageable countPageAndSort(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size, Sort.by("created").descending());
    }
}
