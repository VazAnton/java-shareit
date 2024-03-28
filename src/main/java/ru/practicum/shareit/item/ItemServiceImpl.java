package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        log.info("Информация о новой вещи успешно добавлена!");
        return itemRepository.addItem(itemDto, userId);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId, long itemId) {
        log.info("Информация о вещи с номером " + itemId + " успешно добавлена!");
        return itemRepository.updateItem(itemDto, userId, itemId);
    }

    @Override
    public ItemDto getItem(long id) {
        log.info("Успешно получена информация о вещи с номером " + id);
        return itemRepository.getItem(id);
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        log.info("Успешно получена информация о всех сохранённых вещах!");
        return itemRepository.getItems(userId);
    }

    @Override
    public boolean deleteItem(long id) {
        log.info("Информация о вещи с номером " + id + " успешно удалена!");
        return itemRepository.deleteItem(id);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        log.info("Успешно получена информация о вещи по её описанию!");
        return itemRepository.searchItem(text);
    }
}
