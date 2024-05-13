package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShortBookingDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
