package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(long userId);

    User addUser(User user);

    User updateUser(long userId, User user);

    void deleteUser(long userId);
}
