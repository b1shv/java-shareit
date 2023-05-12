package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserDao userDao;

    @Override
    public List<Item> getItemsByOwnerId(long ownerId) {
        if (!userDao.userExists(ownerId)) {
            throw new NotFoundException(String.format("User with ID %d is not found", ownerId));
        }

        return itemDao.getItemsByOwnerId(ownerId);
    }

    @Override
    public Item getItemById(long itemId) {
        return itemDao.getItemById(itemId);
    }

    @Override
    public List<Item> searchText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemDao.searchText(text);
    }

    @Override
    public Item addItem(Item item) {
        if (!userDao.userExists(item.getOwnerId())) {
            throw new NotFoundException(String.format("User with ID %d is not found", item.getOwnerId()));
        }

        return itemDao.addItem(item);
    }

    @Override
    public Item updateItem(Item item) {
        if (!itemDao.itemExists(item.getId())) {
            throw new NotFoundException(String.format("Item with ID %d is not found", item.getId()));
        }
        if (!itemDao.userIsOwner(item.getOwnerId(), item.getId())) {
            throw new NotFoundException(
                    String.format("User with ID %d doesn't have an item with ID %d", item.getOwnerId(), item.getId()));
        }

        Item itemToUpdate = itemDao.getItemById(item.getId());

        if (item.getName() != null) {
            itemToUpdate.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }

        return itemToUpdate;
    }
}
