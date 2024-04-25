package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.constraints.BookingDateConstraint;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BookingDto {
    private Long id;
    @BookingDateConstraint
    private LocalDateTime start;
    @BookingDateConstraint
    private LocalDateTime end;
    @Valid
    private Item item;
    @Valid
    private User booker;
    private State state;

    public BookingDto(Long id, LocalDateTime start, LocalDateTime end, Item item, User booker) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.item = item;
        this.booker = booker;
    }
}
