package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * TODO Sprint add-controllers.
 */
@AllArgsConstructor
@Data
public class Item {
    private Long id;
    private String name;
    private String description;
    private User owner;
    private Boolean available;
    private ItemRequest request;
}
