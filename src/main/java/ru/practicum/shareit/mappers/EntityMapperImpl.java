package ru.practicum.shareit.mappers;

import org.springframework.stereotype.Component;
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

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapperImpl implements EntityMapper {

    @Override
    public Booking bookingDtoToBooking(BookingDto bookingDto) {
        if (bookingDto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(bookingDto.getItem());
        booking.setBooker(bookingDto.getBooker());
        return booking;
    }

    @Override
    public Booking shortBookingDtoToBooking(ShortBookingDto shortBookingDto) {
        if (shortBookingDto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setStart(shortBookingDto.getStart());
        booking.setEnd(shortBookingDto.getEnd());
        return booking;
    }

    @Override
    public BookingDto bookingToBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItem(booking.getItem());
        bookingDto.setBooker(booking.getBooker());
        return bookingDto;
    }

    @Override
    public LastBooking bookingToLastBooking(Booking booking) {
        if (booking == null) {
            return null;
        }
        LastBooking lastBooking = new LastBooking();
        lastBooking.setId(booking.getId());
        lastBooking.setBookerId(booking.getBooker().getId());
        return lastBooking;
    }

    @Override
    public NextBooking bookingToNextBooking(Booking booking) {
        if (booking == null) {
            return null;
        }
        NextBooking nextBooking = new NextBooking();
        nextBooking.setId(booking.getId());
        nextBooking.setBookerId(booking.getBooker().getId());
        return nextBooking;
    }

    @Override
    public ItemDto itemToItemDto(Item item) {
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
    }

    @Override
    public Item itemDtoToItem(ItemDto itemDto) {
        return new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(),
                itemDto.getAvailable());
    }

    @Override
    public UserDto userToUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    @Override
    public User userDtoToUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    @Override
    public Comment commentDtoToComment(CommentDto commentDto) {
        if (commentDto == null) {
            return null;
        }
        return new Comment(commentDto.getText());
    }

    @Override
    public CommentDto commentToCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }

    @Override
    public List<ItemDto> itemsToDtoList(List<Item> items) {
        return items.stream().map(this::itemToItemDto).collect(Collectors.toList());
    }

    @Override
    public List<Item> itemsDtoToItemsList(List<ItemDto> itemsDto) {
        return itemsDto.stream().map(this::itemDtoToItem).collect(Collectors.toList());
    }
}
