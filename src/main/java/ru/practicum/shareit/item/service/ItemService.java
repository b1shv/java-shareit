package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getItemsByOwnerId(long ownerId, int from, int size);

    Item getItemById(long itemId);

    List<Item> searchText(String text, int from, int size);

    Item addItem(Item item);

    Item updateItem(Item item);

    Comment addComment(Comment comment);

    List<Comment> getComments(long itemId);

    List<Item> getItemsByRequestId(long requestId);
}
