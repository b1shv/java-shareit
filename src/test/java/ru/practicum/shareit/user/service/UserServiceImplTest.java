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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    private final User user1 = User.builder().id(1).build();
    private final User user2 = User.builder().id(2).build();

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        List<User> expectedUsers = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(expectedUsers);

        assertEquals(expectedUsers, userService.getAllUsers());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ShouldReturnUser_ifCorrectId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        assertEquals(user1, userService.getUserById(1));
        assertEquals(user2, userService.getUserById(2));
        verify(userRepository, times(2)).findById(Mockito.anyLong());
    }

    @Test
    void getUserById_ShouldThrowException_ifWrongId() {
        when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(1));
        verify(userRepository, times(1)).findById(Mockito.anyLong());
    }

    @Test
    void addUser_ShouldReturnUser() {
        when(userRepository.save(user1)).thenReturn(user1);

        assertEquals(user1, userService.addUser(user1));
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void updateUser_ShouldThrowException_ifWrongId() {
        when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(1, user1));
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