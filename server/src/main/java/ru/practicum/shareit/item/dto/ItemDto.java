package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.LastBooking;
import ru.practicum.shareit.booking.dto.NextBooking;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private LastBooking lastBooking;
    private NextBooking nextBooking;
    private List<CommentDto> comments;
    private Long requestId;

    public ItemDto(Long id, String name, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
