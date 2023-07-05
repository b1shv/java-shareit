package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getItemsByOwnerId(long ownerId, Pageable pageable);

    Item getItemById(long itemId);

    List<Item> searchText(String text, Pageable pageable);

    Item addItem(Item item);

    Item updateItem(Item item);

    Comment addComment(Comment comment);

    List<Comment> getComments(long itemId);

    List<Item> getItemsByRequestId(long requestId);
}
