package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserService userService;
    @Autowired
    MockMvc mockMvc;
    User user1 = new User(1L, "user1", "user@user.com");
    User user2 = new User(2L, "user2", "user@another.com");

    @Test
    void checkAddUser() throws Exception {
        UserDto userDto = new UserDto(
                null,
                "user",
                "user@user.com");
        when(userService.addUser(userDto))
                .thenReturn(user1);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user1.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));

        verify(userService, times(1)).addUser(userDto);
    }

    @Test
    void checkUpdateUser() throws Exception {
        UserDto userDto = new UserDto(
                1L,
                "new_user",
                "user@user.com");
        when(userService.updateUser((userDto), userDto.getId()))
                .thenReturn(User.builder()
                        .id(user1.getId())
                        .name(userDto.getName())
                        .email(user1.getEmail())
                        .build());

        mockMvc.perform(patch("/users/{id}", 1L)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));

        verify(userService, times(1)).updateUser(userDto, userDto.getId());
    }

    @Test
    void checkGetUser() throws Exception {
        when(userService.getUser(user1.getId()))
                .thenReturn(Optional.of(user1));

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user1.getName())))
                .andExpect(jsonPath("$.email", is(user1.getEmail())));

        verify(userService, times(1)).getUser(user1.getId());
    }

    @Test
    void checkGetUsers() throws Exception {
        when(userService.getUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpectAll(status().isOk()).andReturn();

        verify(userService, times(1)).getUsers();
    }

    @Test
    void checkDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        verify(userService, times(1)).deleteUser(1L);
    }
}
