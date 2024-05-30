package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.mappers.RequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    RequestService requestService;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    RequestMapper requestMapper;
    @Autowired
    MockMvc mockMvc;
    User user1 = new User(1L, "user1", "user@user.com");
    ItemRequestDto itemRequestDto = new ItemRequestDto(
            null,
            "Хотел бы воспользоваться щёткой для обуви",
            null);
    ItemRequest itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться щёткой для обуви", user1,
            LocalDateTime.now());
    ItemRequestDto itemRequestDtoAfterSave = new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(),
            itemRequest.getCreated());

    @Test
    void checkAddRequest() throws Exception {
        when(requestService.addRequest(user1.getId(), itemRequestDto))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(requestService, times(1)).addRequest(user1.getId(), itemRequestDto);
    }

    @Test
    void getItemRequestByIdTest() throws Exception {
        when(requestService.getItemRequest(user1.getId(), itemRequest.getId()))
                .thenReturn(Optional.of(itemRequestDtoAfterSave));

        mockMvc.perform(get("/requests/{requestId}", itemRequest.getId())
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(itemRequestDtoAfterSave, requestService.getItemRequest(user1.getId(), itemRequest.getId()).get());
    }

    @Test
    void checkGetOwnRequests() throws Exception {
        when(requestService.getYoursRequest(user1.getId()))
                .thenReturn(List.of(itemRequestDtoAfterSave));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(requestService, times(1)).getYoursRequest(user1.getId());
    }

    @Test
    void checkAllRequests() throws Exception {
        when(requestService.getOthersRequests(user1.getId(), 0, 20))
                .thenReturn(List.of(itemRequestDtoAfterSave));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", user1.getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(requestService, times(1)).getOthersRequests(user1.getId(), 0, 20);
    }
}
