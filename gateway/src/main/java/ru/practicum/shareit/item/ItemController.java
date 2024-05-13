package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemId) {
        return itemClient.updateItem(itemDto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                           @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemClient.getItems(userId, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable long itemId) {
        itemClient.deleteItem(itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam(required = false) String text,
                                             @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                             @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemClient.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestBody @Valid CommentDto commentDto,
                                             @PathVariable long itemId,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.addComment(commentDto, itemId, userId);
    }
}
