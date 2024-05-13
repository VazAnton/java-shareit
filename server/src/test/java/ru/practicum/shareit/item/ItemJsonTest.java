package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemJsonTest {

    @Autowired
    JacksonTester<Item> json;
    Item item = new Item(1L,
            "Кухонный стол",
            "Стол для празднования",
            true);

    @Test
    void testBookingToJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        JsonContent<Item> result = json.write(item);

        assertThat(result).isEqualToJson(objectMapper.writeValueAsString(item));
    }

    @Test
    void testJsonBookingToBooking() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        String content = json.write(item).getJson();
        Item result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getDescription()).isEqualTo(item.getDescription());
        assertThat(result.getAvailable()).isEqualTo(item.getAvailable());
    }
}
