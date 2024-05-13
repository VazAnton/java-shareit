package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ShortBookingDto;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public Booking addBooking(@RequestBody ShortBookingDto shortBookingDto,
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
                                     @RequestParam String state,
                                     @RequestParam Integer from,
                                     @RequestParam Integer size) {
        return bookingService.getBookingsByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<Booking> getOwnersBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam String state,
                                           @RequestParam Integer from,
                                           @RequestParam Integer size) {
        return bookingService.getBookingsByOwner(userId, state, from, size);
    }

    @DeleteMapping("/{bookingId}")
    public void deleteBooking(@PathVariable long bookingId) {
        bookingService.deleteBooking(bookingId);
    }
}
