package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Override
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public User getUserById(long userId) {
        return userDao.getUserById(userId);
    }

    @Override
    public User addUser(User user) {
        if (userDao.emailIsDuplicate(user.getEmail())) {
            throw new DuplicateException(String.format("User with email '%s' already exists", user.getEmail()));
        }

        return userDao.addUser(user);
    }

    @Override
    public User updateUser(long userId, User user) {
        if (!userDao.userExists(userId)) {
            throw new NotFoundException(String.format("User with ID %d is not found", userId));
        }

        User userToUpdate = userDao.getUserById(userId);
        if (user.getName() != null) {
            userToUpdate.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().equals(userToUpdate.getEmail())) {
            if (userDao.emailIsDuplicate(user.getEmail())) {
                throw new DuplicateException(String.format("User with email '%s' already exists", user.getEmail()));
            }
            userToUpdate.setEmail(user.getEmail());
        }

        return userDao.updateUser(userId, userToUpdate);
    }

    @Override
    public void deleteUser(long userId) {
        userDao.deleteUser(userId);
    }
}
