package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserRepositoryWithoutBd;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryWithoutDb implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long itemId = 1;
    private final UserRepositoryWithoutBd userRepositoryWithoutBd;

    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        if (itemDto != null && userRepositoryWithoutBd.getUser(userId) != null) {
            itemDto.setOwner(UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(userId)));
            itemDto.setId(itemId);
            items.put(itemId++, ItemMapper.itemDtoToItem(itemDto));
        }
        return itemDto;
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId, long itemId) {
        if (itemDto != null && userRepositoryWithoutBd.getUser(userId) != null) {
            Item chosenItem = ItemMapper.itemDtoToItem(getItem(itemId));
            if (itemDto.getName() == null) {
                chosenItem.setName(items.get(itemId).getName());
            } else {
                chosenItem.setName(itemDto.getName());
            }
            if (itemDto.getDescription() == null) {
                chosenItem.setDescription(items.get(itemId).getDescription());
            } else {
                chosenItem.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() == null) {
                chosenItem.setAvailable(items.get(itemId).getAvailable());
            } else {
                chosenItem.setAvailable(itemDto.getAvailable());
            }
            if (items.get(itemId).getOwner().getId() != userId) {
                throw new ObjectNotFoundException("Внимание! Допущена ошибка при указании уникального номера " +
                        "владельца вещи!");
            }
            items.put(itemId, chosenItem);
        }
        return getItem(itemId);
    }

    @Override
    public ItemDto getItem(long id) {
        if (items.containsKey(id)) {
            return ItemMapper.itemToItemDto(items.get(id));
        }
        throw new ObjectNotFoundException("Внимание! Вещи с такми номером не существует!");
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .map(ItemMapper::itemToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteItem(long id) {
        items.remove(id);
        return true;
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (text != null && (!text.isEmpty() || !text.isBlank())) {
            return items.values().stream()
                    .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) && item.getAvailable()
                            || item.getDescription().toLowerCase().contains(text.toLowerCase()) && item.getAvailable())
                    .map(ItemMapper::itemToItemDto)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
