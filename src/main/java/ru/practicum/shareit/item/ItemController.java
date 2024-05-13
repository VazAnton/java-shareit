package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;

import javax.validation.Valid;
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
    private final EntityMapper entityMapper;

    @PostMapping
    public ItemDto addItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long userId) {
        ItemDto itemDtoAfterSave = entityMapper.itemToItemDto(itemService.addItem(itemDto, userId));
        if (itemDto.getRequestId() != null) {
            itemDtoAfterSave.setRequestId(itemDto.getRequestId());
        }
        return itemDtoAfterSave;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId) {
        return entityMapper.itemToItemDto(itemService.updateItem(itemDto, userId, itemId));
    }

    @GetMapping("/{itemId}")
    public Optional<ItemDto> getItem(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestParam(required = false) Long from,
                                  @RequestParam(required = false) Integer size) {
        return itemService.getItems(userId, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam(required = false) String text,
                                    @RequestParam(required = false) Integer from,
                                    @RequestParam(required = false) Integer size) {
        return itemService.searchItem(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody @Valid CommentDto commentDto,
                                 @PathVariable long itemId,
                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return entityMapper.commentToCommentDto(itemService.addComment(commentDto, itemId, userId));
    }
}
