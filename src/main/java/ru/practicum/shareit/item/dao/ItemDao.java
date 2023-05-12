package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemDao {
    List<Item> getItemsByOwnerId(long ownerId);

    Item getItemById(long itemId);

    List<Item> searchText(String text);

    Item addItem(Item item);

    Item updateItem(Item item);

    boolean itemExists(long itemId);

    boolean userIsOwner(long userId, long itemId);
}
