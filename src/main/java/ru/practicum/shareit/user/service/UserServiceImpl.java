package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        return userDao.getAllUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        return userMapper.toUserDto(userDao.getUserById(userId));
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDao.emailIsDuplicate(userDto.getEmail())) {
            throw new DuplicateException(String.format("User with email '%s' already exists", userDto.getEmail()));
        }

        return userMapper.toUserDto(userDao.addUser(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        if (!userDao.userExists(userId)) {
            throw new NotFoundException(String.format("User with ID %d is not found", userId));
        }

        User user = userDao.getUserById(userId);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userDao.emailIsDuplicate(userDto.getEmail())) {
                throw new DuplicateException(String.format("User with email '%s' already exists", userDto.getEmail()));
            }
            user.setEmail(userDto.getEmail());
        }

        return userMapper.toUserDto(userDao.updateUser(userId, user));
    }

    @Override
    public void deleteUser(long userId) {
        userDao.deleteUser(userId);
    }
}
