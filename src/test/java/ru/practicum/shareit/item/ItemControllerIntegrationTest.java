package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.RequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private User user1;
    ItemDto itemDto;
    ItemDto itemFromDb;

    public void fullDb() {
        user1 = new User(1L, "user1", "user@user.com");
        userRepository.save(user1);
    }

    @BeforeEach
    public void setUp() {
        itemDto = new ItemDto(null, "Дрель", "Простая дрель", true);
        itemFromDb = new ItemDto(null, "Дрель", "Простая дрель", true, null,
                null, null, null);
    }

    @Test
    @Order(0)
    public void checkSaveItemInDb() throws Exception {
        fullDb();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(1)
    void updateItemShouldThrowNotFoundExceptionIfItemNotExists() throws Exception {
        fullDb();

        mockMvc.perform(patch("/items/{itemId}", 100L)
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    public void checkDeleteItemIfItemExists() throws Exception {
        Item item = new Item(1L, "Дрель", "Простая дрель", true);
        itemRepository.save(item);

        mockMvc.perform(delete("/items/{id}", item.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void checkGetItemIfItemExists() throws Exception {
        fullDb();
        Item item = new Item(1L, "Дрель", "Простая дрель", true);
        itemRepository.save(item);

        mockMvc.perform(get("/items/{itemId}", itemFromDb.getId())
                        .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    public void checkGetItems() throws Exception {
        setUp();
        fullDb();
        Item item = new Item(1L, "Дрель", "Простая дрель", true);
        itemRepository.save(item);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user1.getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void checkSearchItems() throws Exception {
        String text = "дрель";
        setUp();
        fullDb();
        Item item = new Item(1L, "Дрель", "Простая дрель", true);
        itemRepository.save(item);

        mockMvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }
}
