package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingJsonTest {

    @Autowired
    JacksonTester<Booking> json;
    LocalDateTime now = LocalDateTime.now();
    Item item = new Item(1L,
            "Кухонный стол",
            "Стол для празднования",
            true);
    User user = new User(1L, "user1", "user@user.com");

    Booking booking = new Booking(1L, now, now.minusHours(5L), item, user, Status.WAITING);

    @Test
    void testBookingToJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        JsonContent<Booking> result = json.write(booking);

        assertThat(result).isEqualToJson(objectMapper.writeValueAsString(booking));
    }

    @Test
    void testJsonBookingToBooking() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        String content = json.write(booking).getJson();
        Booking result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(booking.getId());
    }
}
