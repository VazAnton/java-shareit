
package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
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
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Item item3;
    private Booking booking;
    private ShortBookingDto shortBookingDto;
    private Booking bookingOutput;
    private final Long userId1 = 1L;
    private final Long userId2 = 2L;
    private final Long bookingId1 = 1L;
    private final Long invalidId = 999L;
    private final int from = 0;
    private final int size = 2;
    private final LocalDateTime startTime = LocalDateTime.now().plusHours(1);
    private final LocalDateTime endTime = LocalDateTime.now().plusHours(2);

    public void init() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }

    void setUpBooking() {
        mapper.registerModule(new JavaTimeModule());
        user1 = User.builder()
                .email("ruru@yandex.ru")
                .name("RuRu")
                .build();

        user2 = User.builder()
                .email("comcom@gmail.com")
                .name("ComCom")
                .build();

        item1 = new Item(1L, "Дрель", "Простая дрель", true);
        user1.setId(1L);
        item1.setOwner(user2);

        item2 = Item.builder()
                .name("Отвертка")
                .description("Крестовая отвертка")
                .available(true)
                .build();
        user2.setId(2L);
        item2.setOwner(user1);

        item3 = Item.builder()
                .name("Стул")
                .description("Пластиковый стул")
                .available(false)
                .build();
        item3.setOwner(user1);

        booking = Booking.builder()
                .start(startTime)
                .end(endTime)
                .status(Status.APPROVED)
                .booker(user1)
                .item(item2)
                .build();

        shortBookingDto = new ShortBookingDto(
                1L,
                startTime,
                endTime);

        bookingOutput = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item1, user1,
                Status.WAITING);
    }

    @Test
    @SneakyThrows
    public void checkAddBooking() {
        setUpBooking();
        init();

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(shortBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void checkGetBookingById() {
        setUpBooking();
        init();
        bookingRepository.save(bookingOutput);

        mvc.perform(get("/bookings/{bookingId}", bookingId1)
                        .header("X-Sharer-User-Id", userId1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(bookingOutput.getId()), Long.class))
                .andReturn();
    }

    @Test
    @SneakyThrows
    public void updateBookingShouldThrowEntityNotFoundExceptionIfUserNotExists() {
        setUpBooking();

        mvc.perform(patch("/bookings/{bookingId}", bookingId1)
                        .header("X-Sharer-User-Id", invalidId)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void updateBookingShouldThrowEntityNotFoundExceptionIfBookingNotExists() {
        setUpBooking();

        mvc.perform(patch("/bookings/{bookingId}", invalidId)
                        .header("X-Sharer-User-Id", userId1)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void updateBookingShouldThrowEntityNotFoundExceptionIfOwnerIdIsWrong() {
        setUpBooking();

        mvc.perform(patch("/bookings/{bookingId}", bookingId1)
                        .header("X-Sharer-User-Id", userId1)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void checkGetAllBookingsByUserIfStateAll() {
        setUpBooking();
        init();
        bookingRepository.save(bookingOutput);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId1)
                        .param("state", "ALL")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsByUserShouldThrowUnsupportedStatusExceptionIfStateIsWrong() {
        setUpBooking();
        init();

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId1)
                        .param("state", "Unknown")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @SneakyThrows
    public void getBookingByIdShouldThrowEntityNotFoundExceptionIfBookingNotExists() {
        setUpBooking();

        mvc.perform(get("/bookings/{bookingId}", invalidId)
                        .header("X-Sharer-User-Id", userId1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @SneakyThrows
    public void checkGetAllBookingsByOwnerIfStateAll() {
        setUpBooking();
        init();
        bookingRepository.save(bookingOutput);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId2)
                        .param("state", "ALL")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void checkGetAllBookingsByOwnerIfStateWaiting() {
        setUpBooking();
        init();
        bookingRepository.save(bookingOutput);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId2)
                        .param("state", "WAITING")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsByOwnerShouldThrowUnsupportedStatusExceptionIfStateIsWrong() {
        setUpBooking();
        init();

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId2)
                        .param("state", "Unknown")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(23)
    @SneakyThrows
    public void checkGetAllBookingsByOwnerIfUserNotExists() {
        setUpBooking();

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", invalidId)
                        .param("state", "ALL")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
