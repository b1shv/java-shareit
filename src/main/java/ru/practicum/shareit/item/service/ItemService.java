package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getItemsByOwnerId(long ownerId);

    Item getItemById(long itemId);

    List<Item> searchText(String text);

    Item addItem(Item item);

    Item updateItem(Item item);

    Comment addComment(Comment comment);

    List<Comment> getComments(long itemId);
}
