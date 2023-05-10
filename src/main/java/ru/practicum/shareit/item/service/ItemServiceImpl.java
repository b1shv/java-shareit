package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final ItemMapper itemMapper;
    private final UserDao userDao;

    @Override
    public List<ItemDto> getItemsByOwnerId(long ownerId) {
        if (!userDao.userExists(ownerId)) {
            throw new NotFoundException(String.format("User with ID %d is not found", ownerId));
        }

        return itemDao.getItemsByOwnerId(ownerId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return itemMapper.toItemDto(itemDao.getItemById(itemId));
    }

    @Override
    public List<ItemDto> searchText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemDao.searchText(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, long ownerId) {
        if (!userDao.userExists(ownerId)) {
            throw new NotFoundException(String.format("User with ID %d is not found", ownerId));
        }

        Item item = itemMapper.toItem(itemDto, ownerId);
        return itemMapper.toItemDto(itemDao.addItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long itemId, long ownerId) {
        if (!itemDao.itemExists(itemId)) {
            throw new NotFoundException(String.format("Item with ID %d is not found", itemId));
        }
        if (!itemDao.userIsOwner(ownerId, itemId)) {
            throw new NotFoundException(
                    String.format("User with ID %d doesn't have an item with ID %d", ownerId, itemId));
        }

        Item item = itemDao.getItemById(itemId);

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(itemDao.updateItem(item));
    }
}
