package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public List<Item> getItemsByOwnerId(long ownerId, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(String.format("User ID %d is not found", ownerId));
        }

        return itemRepository.findAllByOwnerId(ownerId, pageable);
    }

    @Override
    public Item getItemById(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID %d is not found", itemId)));
    }

    @Override
    public List<Item> searchText(String text, Pageable pageable) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchText(text, pageable);
    }

    @Override
    public Item addItem(Item item) {
        if (!userRepository.existsById(item.getOwnerId())) {
            throw new NotFoundException(String.format("User ID %d is not found", item.getOwnerId()));
        }
        if (item.getRequestId() != null && !itemRequestRepository.existsById(item.getRequestId())) {
            throw new NotFoundException(String.format("Request ID %d is not found", item.getRequestId()));
        }

        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Item item) {
        Item itemToUpdate = itemRepository
                .findById(item.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Item ID %d is not found", item.getId())));

        if (item.getOwnerId() != itemToUpdate.getOwnerId()) {
            throw new ForbiddenException(
                    String.format("User ID %d is not an owner of an item ID %d", item.getOwnerId(), item.getId()));
        }

        if (item.getName() != null) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }

        return itemRepository.save(itemToUpdate);
    }

    @Override
    public Comment addComment(Comment comment) {
        boolean authorHasNotBookedTheItemBefore = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                        comment.getItem().getId(), comment.getAuthor().getId(), BookingStatus.APPROVED, comment.getCreated())
                .isEmpty();

        if (authorHasNotBookedTheItemBefore) {
            throw new ValidationException(String.format("User ID %d has no past bookings of item ID %d",
                    comment.getAuthor().getId(), comment.getItem().getId()));
        }

        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getComments(long itemId) {
        return commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);
    }

    @Override
    public List<Item> getItemsByRequestId(long requestId) {
        return itemRepository.findAllByRequestId(requestId);
    }
}
