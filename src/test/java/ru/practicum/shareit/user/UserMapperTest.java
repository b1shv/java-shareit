package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    private final UserMapper userMapper = new UserMapper();

    @Test
    void toDto_returnsDto() {
        User user = User.builder()
                .id(1)
                .name("Ttt")
                .email("ttt@ttt.tt")
                .build();
        UserDto userDto = UserDto.builder()
                .id(1)
                .name("Ttt")
                .email("ttt@ttt.tt")
                .build();

        UserDto dtoFromMapper = userMapper.toDto(user);

        assertThat(dtoFromMapper).isEqualTo(userDto);
    }

    @Test
    void toUser_ReturnsUser() {
        User user = User.builder()
                .id(1)
                .name("Ttt")
                .email("ttt@ttt.tt")
                .build();
        UserDto userDto = UserDto.builder()
                .id(1)
                .name("Ttt")
                .email("ttt@ttt.tt")
                .build();

        User userFromMapper = userMapper.toUser(userDto);

        assertThat(userFromMapper).isEqualTo(user);
    }
}