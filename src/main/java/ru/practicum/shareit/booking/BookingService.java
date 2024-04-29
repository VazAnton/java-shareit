package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.ShortBookingDto;

import java.util.List;

public interface BookingService {

    Booking addBooking(ShortBookingDto shortBookingDto, long userId);

    Booking updateBooking(long id, long userId, String approved);

    Booking getBooking(long bookingId, long userId);

    List<Booking> getBookingsByUser(long userId, String state);

    List<Booking> getBookingsByOwner(long userId, String state);

    void deleteBooking(long id);
}
