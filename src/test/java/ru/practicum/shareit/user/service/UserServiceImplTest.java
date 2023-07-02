package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        User user1 = User.builder().id(1).build();
        User user2 = User.builder().id(2).build();
        List<User> expectedUsers = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(expectedUsers);

        assertThat(userService.getAllUsers()).isEqualTo(expectedUsers);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ShouldReturnUser_ifCorrectId() {
        User user1 = User.builder().id(1).build();
        User user2 = User.builder().id(2).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        assertThat(userService.getUserById(1)).isEqualTo(user1);
        assertThat(userService.getUserById(2)).isEqualTo(user2);
        verify(userRepository, times(2)).findById(Mockito.anyLong());
    }

    @Test
    void getUserById_ShouldThrowException_ifWrongId() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1)).isInstanceOf(NotFoundException.class);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void addUser_ShouldReturnUser() {
        User user1 = User.builder().id(1).build();
        when(userRepository.save(user1)).thenReturn(user1);

        assertThat(userService.addUser(user1)).isEqualTo(user1);
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void updateUser_ShouldThrowException_ifWrongId() {
        User user1 = User.builder().id(1).build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1, user1)).isInstanceOf(NotFoundException.class);
        verify(userRepository, times(1)).findById(Mockito.anyLong());
        verify(userRepository, never()).save(Mockito.any());
    }

    @Test
    void updateUser_ShouldUpdateUserFields() {
        User user = User.builder().id(1).name("old name").email("old@email.com").build();
        String newName = "new name";
        String newEmail = "new@email.com";
        String newNewName = "new new name";
        String newNewEmail = "newnew@email.com";
        User justName = User.builder().name(newName).build();
        User justEmail = User.builder().email(newEmail).build();
        User nameAndEmail = User.builder().name(newNewName).email(newNewEmail).build();
        InOrder inOrder = Mockito.inOrder(userRepository);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.updateUser(1, justName);
        inOrder.verify(userRepository).save(argThat(arg -> arg.getName().equals(newName)));

        userService.updateUser(1, justEmail);
        inOrder.verify(userRepository).save(argThat(arg -> arg.getEmail().equals(newEmail)));

        userService.updateUser(1, nameAndEmail);
        inOrder.verify(userRepository).save(argThat(arg -> arg.getName().equals(newNewName)
                && arg.getEmail().equals(newNewEmail)));
    }

    @Test
    void deleteUser_ShouldReferRepository() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}