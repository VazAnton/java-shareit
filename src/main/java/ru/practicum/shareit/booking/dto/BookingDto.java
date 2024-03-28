package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
public class BookingDto {
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
