package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Item;

public class ItemMapper {

    public static ItemDto itemToItemDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getOwner(), item.getAvailable(),
                item.getRequest());
    }

    public static Item itemDtoToItem(ItemDto itemDto) {
        return new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getOwner(), itemDto.getAvailable(),
                itemDto.getRequest());
    }
}
