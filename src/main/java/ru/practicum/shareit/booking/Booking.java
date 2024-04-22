package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {
    private Long id;
    @PastOrPresent
    private LocalDate start;
    @FutureOrPresent
    private LocalDate end;
    @Valid
    private Item item;
    @Valid
    private User booker;
    private Status status;
}
