package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    Item addItem(ItemDto itemDto, long userId);

    Item updateItem(ItemDto itemDto, long userId, long itemId);

    Optional<ItemDto> getItem(long id, long userId);

    List<ItemDto> getItems(long userId, Long from, Integer size);

    void deleteItem(long id);

    List<ItemDto> searchItem(String text, Integer from, Integer size);

    Comment addComment(CommentDto commentDto, long itemId, long userId);
}
