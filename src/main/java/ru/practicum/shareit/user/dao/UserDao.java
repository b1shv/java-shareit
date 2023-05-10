package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserDao {
    List<User> getAllUsers();

    User getUserById(long userId);

    User addUser(User user);

    User updateUser(long userId, User user);

    void deleteUser(long userId);

    boolean emailIsDuplicate(String email);

    boolean userExists(long userId);
}
