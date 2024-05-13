package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @MockBean
    ItemService itemService;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    EntityMapper entityMapper;
    @Autowired
    MockMvc mockMvc;
    ItemDto itemDto = new ItemDto(
            1L,
            "Кухонный стол",
            "Стол для празднования",
            true);
    Item item = new Item(1L, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
    ItemDto itemDtoAfterSave = new ItemDto(
            item.getId(),
            itemDto.getName(),
            itemDto.getDescription(),
            itemDto.getAvailable());
    User user1 = new User(1L, "user1", "user@user.com");

    @Test
    void checkAddItem() throws Exception {
        when(itemService.addItem(itemDto, user1.getId()))
                .thenReturn(itemDto);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        itemDto.setRequestId(1L);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, times(1)).addItem(itemDto, user1.getId());
    }

    @Test
    void checkUpdateItem() throws Exception {
        when(itemService.updateItem(itemDto, user1.getId(), item.getId()))
                .thenReturn(itemDto);

        mockMvc.perform(patch("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, times(1)).updateItem(itemDto, user1.getId(), item.getId());
    }

    @Test
    void checkDeleteItem() throws Exception {
        mockMvc.perform(delete("/items/{id}", 1L))
                .andExpect(status().isOk()).andReturn();

        verify(itemService, times(1)).deleteItem(1L);
    }

    @Test
    void checkGetItem() throws Exception {
        when(itemService.getItem(item.getId(), user1.getId()))
                .thenReturn(Optional.of(itemDtoAfterSave));

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, times(1)).getItem(item.getId(), user1.getId());
    }

    @Test
    void checkGetItems() throws Exception {
        when(itemService.getItems(user1.getId(), 0, 20))
                .thenReturn(List.of(itemDtoAfterSave));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user1.getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, times(1)).getItems(user1.getId(), 0, 20);
    }

    @Test
    public void checkSearchItems() throws Exception {
        String text = "стол";
        when(itemService.searchItem(text, 0, 20))
                .thenReturn(List.of(itemDtoAfterSave));

        mockMvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, times(1)).searchItem(text, 0, 20);
    }

    @Test
    void checkAddComment() throws Exception {
        CommentDto commentDto = new CommentDto(
                1L,
                "Add comment from user1",
                null,
                LocalDateTime.now());
        Comment comment = new Comment(1L, commentDto.getText(), item, user1, commentDto.getCreated());
        when(itemService.addComment(commentDto, item.getId(), user1.getId()))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, times(1)).addComment(commentDto, item.getId(), user1.getId());
    }
}
