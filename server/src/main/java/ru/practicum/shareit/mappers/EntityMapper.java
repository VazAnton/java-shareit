package ru.practicum.shareit.mappers;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.LastBooking;
import ru.practicum.shareit.booking.dto.NextBooking;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

public interface EntityMapper {

    Booking bookingDtoToBooking(BookingDto bookingDto);

    Booking shortBookingDtoToBooking(ShortBookingDto shortBookingDto);

    LastBooking bookingToLastBooking(Booking booking);

    NextBooking bookingToNextBooking(Booking booking);

    BookingDto bookingToBookingDto(Booking booking);

    ItemDto itemToItemDto(Item item);

    Item itemDtoToItem(ItemDto itemDto);

    UserDto userToUserDto(User user);

    User userDtoToUser(UserDto userDto);

    Comment commentDtoToComment(CommentDto commentDto);

    CommentDto commentToCommentDto(Comment comment);
}
