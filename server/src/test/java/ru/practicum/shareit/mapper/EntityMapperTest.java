package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.LastBooking;
import ru.practicum.shareit.booking.dto.NextBooking;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.mappers.EntityMapperImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class EntityMapperTest {

    @InjectMocks
    EntityMapperImpl entityMapper;
    @Mock
    EntityMapper entityMapperMock;
    UserDto userDto;
    User user1;
    ItemRequestDto itemRequestDto;
    ItemRequest itemRequest;
    ItemDto itemDto;
    Item item;
    ShortBookingDto shortBookingDto;
    CommentDto commentDto;
    Comment comment;

    @BeforeEach
    public void setup() {
        LocalDateTime now = LocalDateTime.now();
        userDto = new UserDto(
                1L,
                "user",
                "user@user.com");
        user1 = new User(1L, "user1", "user@user.com");
        itemRequestDto = new ItemRequestDto(
                1L,
                "Хотел бы воспользоваться щёткой для обуви",
                LocalDateTime.now());
        itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться щёткой для обуви", user1,
                itemRequestDto.getCreated());
        itemDto = new ItemDto(
                1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        item = new Item(1L, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        shortBookingDto = new ShortBookingDto(
                0L,
                LocalDateTime.of(2024, 5, 25, 20, 30),
                LocalDateTime.of(2024, 5, 25, 22, 50));
        comment = new Comment(1L, "Add comment from user1", item, user1, now);
        commentDto = new CommentDto(1L, "Add comment from user1", "user", comment.getCreated());
    }

    @Test
    public void checkUserToUserDto() {
        UserDto newUserDto = entityMapper.userToUserDto(user1);

        assertThat(newUserDto).isNotNull();
        assertThat(newUserDto.getId()).isEqualTo(user1.getId());
        assertThat(newUserDto.getName()).isEqualTo(user1.getName());
        assertThat(newUserDto.getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    public void checkUserDtoToUser() {
        User newUser = entityMapper.userDtoToUser(userDto);

        assertThat(newUser).isNotNull();
        assertThat(newUser.getId()).isEqualTo(userDto.getId());
        assertThat(newUser.getName()).isEqualTo(userDto.getName());
        assertThat(newUser.getEmail()).isEqualTo(userDto.getEmail());
    }

    @Test
    public void checkItemToItemDtoIfItemIsNotNull() {
        ItemDto newItemDto = entityMapper.itemToItemDto(item);

        assertThat(newItemDto).isNotNull();
        assertThat(newItemDto.getId()).isEqualTo(item.getId());
        assertThat(newItemDto.getName()).isEqualTo(item.getName());
        assertThat(newItemDto.getDescription()).isEqualTo(item.getDescription());
        assertThat(newItemDto.getAvailable()).isEqualTo(item.getAvailable());
    }

    @Test
    public void checkItemToItemDtoIfItemIsNull() {
        assertNull(entityMapper.itemToItemDto(null));
    }

    @Test
    public void checkItemDtoToItem() {
        Item newItem = entityMapper.itemDtoToItem(itemDto);

        assertThat(newItem).isNotNull();
        assertThat(newItem.getId()).isEqualTo(itemDto.getId());
        assertThat(newItem.getName()).isEqualTo(itemDto.getName());
        assertThat(newItem.getDescription()).isEqualTo(itemDto.getDescription());
        assertThat(newItem.getAvailable()).isEqualTo(itemDto.getAvailable());
    }

    @Test
    public void checkCommentDtoToComment() {
        Comment newComment = entityMapper.commentDtoToComment(commentDto);

        assertThat(newComment).isNotNull();
        assertThat(newComment.getText()).isEqualTo(commentDto.getText());
    }

    @Test
    public void checkCommentDtoToCommentIfCommentDtoIsNull() {
        assertNull(entityMapper.commentDtoToComment(null));
    }

    @Test
    public void checkCommentToCommentDto() {
        CommentDto newCommentDto = entityMapper.commentToCommentDto(comment);

        assertThat(newCommentDto).isNotNull();
        assertThat(newCommentDto.getId()).isEqualTo(comment.getId());
        assertThat(newCommentDto.getText()).isEqualTo(comment.getText());
        assertThat(newCommentDto.getAuthorName()).isEqualTo(comment.getAuthor().getName());
        assertThat(newCommentDto.getCreated()).isEqualTo(comment.getCreated());
    }

    @Test
    public void checkCommentToCommentDtoIfCommentIsNull() {
        assertNull(entityMapper.commentToCommentDto(null));
    }

    @Test
    public void checkShortBookingDtoToBookingIfShortBookingDtoIsNull() {
        assertNull(entityMapper.shortBookingDtoToBooking(null));
    }

    @Test
    public void checkBookingToBookingDtoIfBookingIsNull() {
        assertNull(entityMapper.bookingToBookingDto(null));
    }

    @Test
    public void checkBookingToLastBookingIfBookingIsNull() {
        assertNull(entityMapper.bookingToLastBooking(null));
    }

    @Test
    public void checkBookingToNextBookingIfBookingIsNull() {
        assertNull(entityMapper.bookingToNextBooking(null));
    }

    @Test
    public void checkBookingDtoToBookingIfBookingDtoIsNull() {
        assertNull(entityMapper.bookingDtoToBooking(null));
    }

    @Test
    public void checkShortBookingDtoToBooking() {
        Booking newBooking = entityMapper.shortBookingDtoToBooking(shortBookingDto);

        assertThat(newBooking).isNotNull();
        assertThat(newBooking.getStart()).isEqualTo(shortBookingDto.getStart());
        assertThat(newBooking.getEnd()).isEqualTo(shortBookingDto.getEnd());
    }

    @Test
    public void checkBookingDtoToBooking() {
        BookingDto bookingDto = new BookingDto(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1);
        Booking newBooking = entityMapper.bookingDtoToBooking(bookingDto);

        assertThat(newBooking).isNotNull();
        assertThat(newBooking.getId()).isEqualTo(bookingDto.getId());
        assertThat(newBooking.getStart()).isEqualTo(bookingDto.getStart());
        assertThat(newBooking.getItem()).isEqualTo(bookingDto.getItem());
        assertThat(newBooking.getBooker()).isEqualTo(bookingDto.getBooker());
    }

    @Test
    public void checkBookingToBookingDto() {
        Booking booking = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1);
        BookingDto bookingDto = entityMapper.bookingToBookingDto(booking);

        assertThat(bookingDto).isNotNull();
        assertThat(bookingDto.getId()).isEqualTo(booking.getId());
        assertThat(bookingDto.getStart()).isEqualTo(booking.getStart());
        assertThat(bookingDto.getItem()).isEqualTo(booking.getItem());
        assertThat(bookingDto.getBooker()).isEqualTo(booking.getBooker());
    }

    @Test
    public void checkBookingToLastBooking() {
        Booking booking = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1);
        LastBooking lastBooking1 = entityMapper.bookingToLastBooking(booking);

        assertThat(lastBooking1).isNotNull();
        assertThat(lastBooking1.getId()).isEqualTo(booking.getId());
        assertThat(lastBooking1.getBookerId()).isEqualTo(booking.getBooker().getId());
    }

    @Test
    public void checkBookingToNextBooking() {
        Booking booking = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1);
        NextBooking nextBooking1 = entityMapper.bookingToNextBooking(booking);

        assertThat(nextBooking1).isNotNull();
        assertThat(nextBooking1.getId()).isEqualTo(booking.getId());
        assertThat(nextBooking1.getBookerId()).isEqualTo(booking.getBooker().getId());
    }
}
