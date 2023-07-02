package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerIntegrationTest {
    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getById_shouldReturnOk_ifUserFound() throws Exception {
        int id = 7;
        when(userMapper.toDto(any())).thenReturn(UserDto.builder().id(id).build());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)));

        verify(userService, times(1)).getUserById(id);
    }

    @Test
    void getById_shouldReturnNotFound_ifUserNotFound() throws Exception {
        when(userMapper.toDto(any())).thenThrow(new NotFoundException());

        mockMvc.perform(get("/users/{id}", 3))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(3);
    }

    @Test
    void add_shouldReturnOk_ifValidationPassed() throws Exception {
        UserDto userDto = UserDto.builder().id(1).name("Ggg").email("ggg@ggg.gg").build();
        User user = User.builder().id(1).name("Ggg").email("ggg@ggg.gg").build();

        when(userService.addUser(user)).thenReturn(user);
        when(userMapper.toUser(userDto)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).addUser(any(User.class));
    }

    @Test
    void add_shouldReturnBadRequest_ifValidationFailed() throws Exception {
        UserDto userDto = UserDto.builder().id(1).name("Ggg").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        userDto.setEmail("1111");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void update_shouldReturnOk_ifUserFound() throws Exception {
        UserDto userDtoRequest = UserDto.builder().id(1).name("PPP").build();
        UserDto userDtoResponse = UserDto.builder().id(1).name("FFF").build();
        when(userMapper.toDto(any())).thenReturn(userDtoResponse);

        mockMvc.perform(patch("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("FFF")));

        verify(userService, times(1)).updateUser(anyLong(), any());
    }

    @Test
    void update_shouldReturnNotFound_ifUserNotFound() throws Exception {
        when(userMapper.toDto(any())).thenThrow(new NotFoundException());

        mockMvc.perform(patch("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserDto.builder().build())))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(anyLong(), any());
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }
}