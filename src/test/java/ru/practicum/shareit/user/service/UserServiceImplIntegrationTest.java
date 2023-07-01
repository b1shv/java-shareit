package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {
    private final UserServiceImpl userService;

    @Test
    void addUser_shouldThrowException_ifEmailDuplicate() {
        User user = User.builder().name("Ttt").email("ttt@ttt.tt").build();
        User userDuplicateEmail = User.builder().name("Rrr").email("ttt@ttt.tt").build();
        userService.addUser(user);

        assertThatThrownBy(() -> userService.addUser(userDuplicateEmail)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void updateUser_shouldChangeEntityFields() {
        User user = userService.addUser(User.builder().name("Ttt").email("ttt@ttt.tt").build());
        User justName = User.builder().name("Fff").build();
        User justEmail = User.builder().email("nnn@nnn.com").build();
        User nameAndEmail = User.builder().name("Uuu").email("uuu@uuu.uu").build();


        userService.updateUser(user.getId(), justName);
        User userUpdatedName = userService.getUserById(user.getId());
        assertThat(userUpdatedName.getName()).isEqualTo(justName.getName());

        userService.updateUser(user.getId(), justEmail);
        User userUpdatedEmail = userService.getUserById(user.getId());
        assertThat(userUpdatedEmail.getEmail()).isEqualTo(justEmail.getEmail());

        userService.updateUser(user.getId(), nameAndEmail);
        User userUpdatedNameAndEmail = userService.getUserById(user.getId());
        assertThat(userUpdatedNameAndEmail.getName()).isEqualTo(nameAndEmail.getName());
        assertThat(userUpdatedNameAndEmail.getEmail()).isEqualTo(nameAndEmail.getEmail());
    }
}