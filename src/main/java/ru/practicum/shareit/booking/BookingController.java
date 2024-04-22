package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ShortBookingDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public Booking addBooking(@Valid @RequestBody ShortBookingDto shortBookingDto,
                              @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.addBooking(shortBookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public Booking updateBooking(@PathVariable long bookingId,
                                 @RequestHeader("X-Sharer-User-Id") long userId,
                                 @RequestParam String approved) {
        return bookingService.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking getBooking(@PathVariable long bookingId, @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<Booking> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByUser(userId, state);
    }

    @GetMapping("/owner")
    public List<Booking> getOwnersBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByOwner(userId, state);
    }

    @DeleteMapping("/{bookingId}")
    public void deleteBooking(@PathVariable long bookingId) {
        bookingService.deleteBooking(bookingId);
    }
}
