package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.mappers.RequestMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class RequestMapperTest {

    @InjectMocks
    RequestMapper itemRequestMapper;
    User user1;
    ItemRequestDto itemRequestDto;
    ItemRequest itemRequest;

    @BeforeEach
    public void setup() {
        user1 = new User(1L, "user1", "user@user.com");
        itemRequestDto = new ItemRequestDto(
                1L,
                "Хотел бы воспользоваться щёткой для обуви",
                LocalDateTime.now());
        itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться щёткой для обуви", user1,
                itemRequestDto.getCreated());
    }

    @Test
    public void checkRequestDtoToRequest() {
        ItemRequestDto newRequestDto = itemRequestMapper.requestToRequestDto(itemRequest);

        assertThat(newRequestDto).isNotNull();
        assertThat(newRequestDto.getId()).isEqualTo(itemRequest.getId());
        assertThat(newRequestDto.getDescription()).isEqualTo(itemRequest.getDescription());
    }

    @Test
    public void checkRequestDtoToRequestIfRequestIsNull() {
        assertNull(itemRequestMapper.requestToRequestDto(null));
    }

    @Test
    public void checkRequestToRequestDto() {
        ItemRequest newRequest = itemRequestMapper.requestDtoToRequest(itemRequestDto);

        assertThat(newRequest).isNotNull();
        assertThat(newRequest.getDescription()).isEqualTo(itemRequestDto.getDescription());
    }

    @Test
    public void checkRequestToRequestDtoIfRequestDtoIsNull() {
        assertNull(itemRequestMapper.requestDtoToRequest(null));
    }
}
