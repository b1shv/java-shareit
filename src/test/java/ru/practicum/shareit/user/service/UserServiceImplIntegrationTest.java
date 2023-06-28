package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {
    @Autowired
    private final UserServiceImpl userService;

    @Test
    @DirtiesContext
    void addUser_shouldThrowException_ifEmailDuplicate() {
        User user = User.builder().name("Ttt").email("ttt@ttt.tt").build();
        User userDuplicateEmail = User.builder().name("Rrr").email("ttt@ttt.tt").build();
        userService.addUser(user);

        assertThrows(RuntimeException.class, () -> userService.addUser(userDuplicateEmail));
    }

    @Test
    @DirtiesContext
    void updateUser_shouldChangeEntityFields() {
        User user = userService.addUser(User.builder().name("Ttt").email("ttt@ttt.tt").build());
        User justName = User.builder().name("Fff").build();
        User justEmail = User.builder().email("nnn@nnn.com").build();
        User nameAndEmail = User.builder().name("Uuu").email("uuu@uuu.uu").build();


        userService.updateUser(user.getId(), justName);
        User userUpdatedName = userService.getUserById(user.getId());

        userService.updateUser(user.getId(), justEmail);
        User userUpdatedEmail = userService.getUserById(user.getId());

        userService.updateUser(user.getId(), nameAndEmail);
        User userUpdatedNameAndEmail = userService.getUserById(user.getId());

        assertEquals(justName.getName(), userUpdatedName.getName());
        assertEquals(justEmail.getEmail(), userUpdatedEmail.getEmail());
        assertEquals(nameAndEmail.getName(), userUpdatedNameAndEmail.getName());
        assertEquals(nameAndEmail.getEmail(), userUpdatedNameAndEmail.getEmail());
    }
}