package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(ItemDto itemDto, long userId);

    ItemDto updateItem(ItemDto itemDto, long userId, long itemId);

    ItemDto getItem(long id);

    List<ItemDto> getItems(long userId);

    boolean deleteItem(long id);

    List<ItemDto> searchItem(String text);
}
