package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Optional;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId) {
        return itemService.updateItem(itemDto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public Optional<ItemDto> getItem(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestParam Integer from,
                                  @RequestParam Integer size) {
        return itemService.getItems(userId, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text,
                                    @RequestParam Integer from,
                                    @RequestParam Integer size) {
        return itemService.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody CommentDto commentDto,
                                 @PathVariable long itemId,
                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.addComment(commentDto, itemId, userId);
    }
}
