package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();
    private UserDto userDtoBeforeSave;
    private User userAfterSave;

    public void setup() {
        userDtoBeforeSave = new UserDto(null, "user1", "user@user.com");
        userAfterSave = new User(1L, userDtoBeforeSave.getName(), userDtoBeforeSave.getEmail());
    }

    @Test
    @Order(0)
    public void checkAddUser() throws Exception {
        setup();

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoBeforeSave))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userAfterSave)));
    }

    @Test
    @Order(1)
    public void updateUserShouldThrowNotFoundException() throws Exception {
        setup();

        mockMvc.perform(patch("/users/{id}", 100L)
                        .content(mapper.writeValueAsString(userDtoBeforeSave))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    public void getUserShouldThrowExceptionIfUserNotExists() throws Exception {
        mockMvc.perform(get("/users/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    public void checkGetUsers() throws Exception {
        setup();
        User user1 = new User(1L, "user1", "user@user.com");
        List<User> users = new ArrayList<>();
        users.add(user1);

        mockMvc.perform(get("/users"))
                .andExpectAll(status().isOk());
    }
}
