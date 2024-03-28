package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId) {
        return itemService.updateItem(itemDto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItems(userId);
    }

    @DeleteMapping("/{itemId}")
    public boolean deleteItem(@PathVariable long itemId) {
        return itemService.deleteItem(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam(required = false) String text) {
        return itemService.searchItem(text);
    }
}
