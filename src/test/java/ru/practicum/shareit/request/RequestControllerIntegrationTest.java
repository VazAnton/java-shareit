package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    RequestService requestService;
    @Autowired
    RequestRepository requestRepository;
    @Autowired
    UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    User user1;
    User user2;
    ItemRequestDto itemRequestDto;
    ItemRequest itemRequest;
    ItemRequest itemRequest2;
    ItemRequestDto itemRequestDtoAfterSave;

    @BeforeEach
    public void setup() {
        user1 = new User(1L, "user1", "user@user.com");

        user2 = new User(2L, "user2", "another_user@user.com");

        itemRequestDto = new ItemRequestDto(
                null,
                "Хотел бы воспользоваться щёткой для обуви",
                LocalDateTime.now());

        itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться щёткой для обуви", user1,
                itemRequestDto.getCreated());

        itemRequest2 = new ItemRequest(2L, "Хотел бы воспользоваться гладильной доской", user2,
                itemRequestDto.getCreated().plusHours(6L));

        itemRequestDtoAfterSave = new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(),
                itemRequest.getCreated(), null);
    }

    public void fullDb() {
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    @Order(0)
    void checkAddRequest() throws Exception {
        fullDb();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(itemRequest.getId()), Long.class));
    }

    @Test
    @Order(1)
    void getItemRequestByIdTest() throws Exception {
        fullDb();
        requestRepository.save(itemRequest);
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockMvc.perform(get("/requests/{requestId}", itemRequest.getId())
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(itemRequest.getId()), Long.class));

    }

    @Test
    @Order(2)
    void checkGetOwnRequests() throws Exception {
        fullDb();
        requestRepository.save(itemRequest);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void checkAllRequests() throws Exception {
        fullDb();
        requestRepository.save(itemRequest2);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", user2.getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
