package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {
    private final UserMapper userMapper = new UserMapper();
    private final User user = User.builder()
            .id(1)
            .name("Ttt")
            .email("ttt@ttt.tt")
            .build();
    private final UserDto userDto = UserDto.builder()
            .id(1)
            .name("Ttt")
            .email("ttt@ttt.tt")
            .build();

    @Test
    void toDto_returnsDto() {
        UserDto dtoFromMapper = userMapper.toDto(user);
        assertEquals(userDto, dtoFromMapper);
    }

    @Test
    void toUser_ReturnsUser() {
        User userFromMapper = userMapper.toUser(userDto);
        assertEquals(user, userFromMapper);
    }
}