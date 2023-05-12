package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserDao implements UserDao {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 0;

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(long userId) {
        return users.get(userId);
    }

    @Override
    public User addUser(User user) {
        user.setId(++idCounter);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(long userId, User user) {
        users.put(userId, user);
        return user;
    }

    @Override
    public void deleteUser(long userId) {
        users.remove(userId);
    }

    @Override
    public boolean emailIsDuplicate(String email) {
        Optional<User> userWithSameEmail = getAllUsers().stream()
                .filter(user -> user.getEmail().equals(email))
                .findAny();
        return userWithSameEmail.isPresent();
    }

    @Override
    public boolean userExists(long userId) {
        return users.containsKey(userId);
    }
}
