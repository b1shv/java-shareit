package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserDto> getAll() {
        log.debug("GET request: all users");
        return userService.getAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable long userId) {
        log.debug("GET request: user with ID {}", userId);
        return userMapper.toDto(userService.getUserById(userId));
    }

    @PostMapping
    public UserDto add(@RequestBody UserDto userDto) {
        log.debug("POST request: new user");
        return userMapper.toDto(userService.addUser(userMapper.toUser(userDto)));
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId, @RequestBody UserDto userDto) {
        log.debug("PATCH request: updating user {}", userId);
        return userMapper.toDto(userService.updateUser(userId, userMapper.toUser(userDto)));
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        log.debug("DELETE request: user {}", userId);
        userService.deleteUser(userId);
    }
}
