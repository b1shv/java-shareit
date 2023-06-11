package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
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

    @Override
    public List<Item> getItemsByOwnerId(long ownerId) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException(String.format("User ID %d is not found", ownerId));
        }

        return itemRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public Item getItemById(long itemId) {
        return itemRepository
                .findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item ID %d is not found", itemId)));
    }

    @Override
    public List<Item> searchText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchText(text.toUpperCase());
    }

    @Override
    public Item addItem(Item item) {
        if (userRepository.findById(item.getOwnerId()).isEmpty()) {
            throw new NotFoundException(String.format("User ID %d is not found", item.getOwnerId()));
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
        if (bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(comment.getItem().getId(),
                comment.getAuthor().getId(), BookingStatus.APPROVED, comment.getCreated()).isEmpty()) {
            throw new ValidationException(String.format("User ID %d has no past bookings of item ID %d",
                    comment.getAuthor().getId(), comment.getItem().getId()));
        }

        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getComments(long itemId) {
        return commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);
    }
}
