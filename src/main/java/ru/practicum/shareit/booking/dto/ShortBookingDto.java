package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.constraints.BookingDateConstraint;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShortBookingDto {
    @NotNull
    private Long itemId;
    @BookingDateConstraint
    private LocalDateTime start;
    @BookingDateConstraint
    private LocalDateTime end;
}
