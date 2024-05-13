package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.RequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @InjectMocks
    ItemServiceImpl itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    EntityMapper entityMapper;
    @Mock
    UserServiceImpl userService;
    @Mock
    RequestRepository requestRepository;
    User user1;
    ItemDto itemDto;
    Item item;
    ShortBookingDto shortBookingDto;
    Comment comment;
    Comment comment2;
    Comment comment3;

    @BeforeEach
    public void setup() {
        LocalDateTime now = LocalDateTime.now();
        user1 = new User(1L, "user1", "user@user.com");
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
        comment2 = new Comment(2L, "Add new comment from user1", item, user1,
                now.plusMinutes(5L));
        comment3 = new Comment(2L, "Add new comment from user1", item, user1,
                now.minusDays(5L));
    }

    @Test
    public void checkAddItem() {
        ItemRequestDto itemRequestDto = new ItemRequestDto(
                1L,
                "Хотел бы воспользоваться щёткой для обуви",
                LocalDateTime.now());
        ItemRequest itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться щёткой для обуви", user1,
                itemRequestDto.getCreated());
        when(entityMapper.itemDtoToItem(itemDto))
                .thenReturn(item);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        itemDto.setRequestId(itemRequest.getId());
        when(requestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(item))
                .thenReturn(item);

        assertThat(itemService.addItem(itemDto, user1.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(item);
    }

    @Test
    public void addItemShouldThrowExceptionIfRequestNotExists() {
        when(requestRepository.findById(100L))
                .thenThrow(new EntityNotFoundException("Внимание! Запроса с таким уникальным номером не существует!"));

        assertThrows(EntityNotFoundException.class, () -> requestRepository.findById(100L));
    }

    @Test
    public void updateItemShouldExceptionIfItemNotExists() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);

        assertThrows(EntityNotFoundException.class,
                () -> itemService.updateItem(itemDto, user1.getId(), itemDto.getId()));
    }

    @Test
    public void updateItemShouldExceptionIfUserNotExists() {
        assertThrows(EntityNotFoundException.class,
                () -> itemService.updateItem(itemDto, 100L, itemDto.getId()));
    }

    @Test
    public void checkUpdateItem() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(entityMapper.itemDtoToItem(itemDto))
                .thenReturn(item);
        Item convertedItem = entityMapper.itemDtoToItem(itemDto);
        convertedItem.setOwner(user1);
        when(itemRepository.save(convertedItem))
                .thenReturn(item);

        assertThat(itemService.updateItem(itemDto, user1.getId(), item.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(item);
    }

    @Test
    public void updateItemShouldThrowExceptionIfUserAndOwnerNotEquals() {
        User user2 = new User(2L, "user2", "user@another.com");
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(entityMapper.itemDtoToItem(itemDto))
                .thenReturn(item);
        Item convertedItem = entityMapper.itemDtoToItem(itemDto);
        convertedItem.setOwner(user2);

        assertThrows(EntityNotFoundException.class,
                () -> itemService.updateItem(itemDto, user1.getId(), itemDto.getId()));
    }

    @Test
    public void checkGetItem() {
        LocalDateTime now = LocalDateTime.now();
        Booking bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item,
                user1, Status.WAITING);
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        CommentDto commentDto2 = new CommentDto(2L, "Add new comment from user1", "user",
                now.plusMinutes(5L));
        CommentDto commentDto3 = new CommentDto(2L, "Add new comment from user1", "user",
                now.minusDays(5L));
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(comment2);
        comments.add(comment3);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(bookings);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        itemDto.setLastBooking(entityMapper.bookingToLastBooking(bookingBeforePatch));
        itemDto.setNextBooking(entityMapper.bookingToNextBooking(booking2));
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.commentToCommentDto(comment2))
                .thenReturn(commentDto2);
        when(entityMapper.commentToCommentDto(comment3))
                .thenReturn(commentDto3);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);

        assertThat(itemService.getItem(item.getId(), user1.getId()))
                .isNotNull();
    }

    @Test
    public void checkGetItemIfBookingsIsEmpty() {
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        List<Booking> bookings = new ArrayList<>();
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(bookings);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);

        assertThat(itemService.getItem(item.getId(), user1.getId()))
                .isNotNull();
    }

    @Test
    public void checkGetItemIfBookingsIsNull() {
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(null);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(comments);
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);

        assertThat(itemService.getItem(item.getId(), user1.getId()))
                .isNotNull();
    }

    @Test
    public void checkGetItemsIfFromAndSizeIsNull() {
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        LocalDateTime now = LocalDateTime.now();
        Booking bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.WAITING);
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
        CommentDto commentDto2 = new CommentDto(2L, "Add new comment from user1", "user",
                now.plusMinutes(5L));
        CommentDto commentDto3 = new CommentDto(2L, "Add new comment from user1", "user",
                now.minusDays(5L));
        List<Item> items = new ArrayList<>();
        items.add(item);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(comment2);
        comments.add(comment3);
        List<Item> itemsWithBookings;
        when(itemRepository.findAllByOwnerId(user1.getId()))
                .thenReturn(items);
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(bookingRepository.getAllBookingsInfoByOwner(user1.getId()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(bookings);
        itemsWithBookings = items.stream()
                .filter(item -> !bookings.isEmpty())
                .collect(Collectors.toList());
        when(bookingRepository.findAllByItemInAndStatusOrderByStartAsc(itemsWithBookings, Status.APPROVED))
                .thenReturn(bookings);
        when(commentRepository.findAllByAuthorId(user1.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        itemDto.setLastBooking(entityMapper.bookingToLastBooking(bookingBeforePatch));
        itemDto.setNextBooking(entityMapper.bookingToNextBooking(booking2));
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.commentToCommentDto(comment2))
                .thenReturn(commentDto2);
        when(entityMapper.commentToCommentDto(comment3))
                .thenReturn(commentDto3);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);

        assertThat(itemService.getItems(user1.getId(), null, null))
                .isNotNull();
    }

    @Test
    public void checkGetItemsIfFromAndSizeIsNotNull() {
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        LocalDateTime now = LocalDateTime.now();
        Booking bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.WAITING);
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
        CommentDto commentDto2 = new CommentDto(2L, "Add new comment from user1", "user",
                now.plusMinutes(5L));
        CommentDto commentDto3 = new CommentDto(2L, "Add new comment from user1", "user",
                now.minusDays(5L));
        List<Item> items = new ArrayList<>();
        items.add(item);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(comment2);
        comments.add(comment3);
        Page<Item> pagedItems = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 20);
        when(itemRepository.findAllByOwnerIdOrderByIdDesc(user1.getId(), pageable))
                .thenReturn(pagedItems);
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(bookingRepository.getAllBookingsInfoByOwner(user1.getId()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(bookings);
        when(bookingRepository.findAllByItemInAndStatusOrderByStartAsc(items, Status.APPROVED))
                .thenReturn(bookings);
        when(commentRepository.findAllByAuthorId(user1.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        itemDto.setLastBooking(entityMapper.bookingToLastBooking(bookingBeforePatch));
        itemDto.setNextBooking(entityMapper.bookingToNextBooking(booking2));
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.commentToCommentDto(comment2))
                .thenReturn(commentDto2);
        when(entityMapper.commentToCommentDto(comment3))
                .thenReturn(commentDto3);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);

        assertThat(itemService.getItems(user1.getId(), 0L, 20))
                .isNotNull();
    }

    @Test
    public void checkGetItemsIfFromAndSizeIsNullAndBookingsIsEmpty() {
        LocalDateTime now = LocalDateTime.now();
        Booking bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item,
                user1, Status.WAITING);
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        CommentDto commentDto2 = new CommentDto(2L, "Add new comment from user1", "user",
                now.plusMinutes(5L));
        CommentDto commentDto3 = new CommentDto(2L, "Add new comment from user1", "user",
                now.minusDays(5L));
        List<Item> items = new ArrayList<>();
        items.add(item);
        List<Booking> bookings = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(comment2);
        comments.add(comment3);
        when(itemRepository.findAllByOwnerId(user1.getId()))
                .thenReturn(items);
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(bookingRepository.getAllBookingsInfoByOwner(user1.getId()))
                .thenReturn(bookings);
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(bookings);
        when(bookingRepository.findAllByItemInAndStatusOrderByStartAsc(items, Status.APPROVED))
                .thenReturn(bookings);
        when(commentRepository.findAllByAuthorId(user1.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        itemDto.setLastBooking(entityMapper.bookingToLastBooking(bookingBeforePatch));
        itemDto.setNextBooking(entityMapper.bookingToNextBooking(booking2));
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.commentToCommentDto(comment2))
                .thenReturn(commentDto2);
        when(entityMapper.commentToCommentDto(comment3))
                .thenReturn(commentDto3);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);

        assertThat(itemService.getItems(user1.getId(), null, null))
                .isNotNull();
    }

    @Test
    public void getItemsShouldTrowExceptionIfUserNotExists() {
        List<Item> items = new ArrayList<>();
        items.add(item);
        when(itemRepository.findAllByOwnerId(user1.getId()))
                .thenReturn(items);

        assertThrows(EntityNotFoundException.class, () -> itemService.getItems(user1.getId(), null, null));
    }

    @Test
    public void checkSearchItemsIfTextIsNull() {
        assertThat(itemService.searchItem(null, 1, 20))
                .isEmpty();
    }

    @Test
    public void checkSearchItemsIfFromAndSizeIsNull() {
        when(itemRepository.searchByText(item.getName()))
                .thenReturn(List.of(item));
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);

        assertEquals(1, itemService.searchItem(item.getName(), null, null).size());
    }

    @Test
    public void checkSearchItemsIfFromAndSizeIsNotNull() {
        Page<Item> pagedItems = new PageImpl<>(List.of(item));
        Pageable pageable = PageRequest.of(0, 20);
        when(itemRepository.searchByTextLikePage(item.getName(), pageable)).thenReturn(pagedItems);

        assertNotNull(itemService.searchItem(item.getName(), 0, 20));
    }

    @Test
    public void deleteItemShouldThrowExceptionIfItemNotExists() {
        assertThrows(EntityNotFoundException.class, () -> itemService.deleteItem(100));
    }

    @Test
    public void checkDeleteItem() {
        when(itemRepository.existsById(1L))
                .thenReturn(true);

        itemService.deleteItem(1L);
    }

    @Test
    public void addCommentShouldThrowExceptionIfUserAndItemNotExists() {
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        assertThrows(EntityNotFoundException.class, () -> itemService.addComment(commentDto, 100L, user1.getId()));
    }

    @Test
    public void addCommentShouldThrowValidationException() {
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        LocalDateTime now = LocalDateTime.now();
        Booking bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.WAITING);
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
        ItemDto itemDto2 = new ItemDto(2L, "Кухонный стул",
                "Стул для празднования",
                true);
        Item item2 = new Item(2L, itemDto2.getName(), itemDto2.getDescription(), itemDto2.getAvailable());
        CommentDto commentDto2 = new CommentDto(2L, "Add new comment from user1", "user",
                now.plusMinutes(5L));
        CommentDto commentDto3 = new CommentDto(2L, "Add new comment from user1", "user",
                now.minusDays(5L));
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(comment2);
        comments.add(comment3);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        when(itemRepository.findById(item2.getId()))
                .thenReturn(Optional.of(item2));
        when(bookingRepository.findByItemId(item2.getId()))
                .thenReturn(bookings);
        when(commentRepository.findAllByItemId(item2.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item2))
                .thenReturn(itemDto2);
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.commentToCommentDto(comment2))
                .thenReturn(commentDto2);
        when(entityMapper.commentToCommentDto(comment3))
                .thenReturn(commentDto3);
        when(entityMapper.itemToItemDto(item2))
                .thenReturn(itemDto2);
        when(bookingRepository.findAllByBookerId(user1.getId()))
                .thenReturn(bookings);

        assertThrows(ValidationException.class, () -> itemService.addComment(commentDto, item2.getId(), 1));
    }

    @Test
    public void addCommentShouldThrowValidationExceptionTo() {
        LocalDateTime now = LocalDateTime.now();
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        CommentDto commentDto2 = new CommentDto(2L, "Add new comment from user1", "user",
                now.plusMinutes(5L));
        CommentDto commentDto3 = new CommentDto(2L, "Add new comment from user1", "user",
                now.minusDays(5L));
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        comments.add(comment2);
        comments.add(comment3);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(3L, LocalDateTime.of(2024, 5, 5, 23, 30),
                LocalDateTime.of(2024, 6, 6, 20, 15), item, user1, Status.APPROVED));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(bookings);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.commentToCommentDto(comment2))
                .thenReturn(commentDto2);
        when(entityMapper.commentToCommentDto(comment3))
                .thenReturn(commentDto3);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        when(bookingRepository.findAllByBookerId(user1.getId()))
                .thenReturn(bookings);

        assertThrows(ValidationException.class, () -> itemService.addComment(commentDto, item.getId(), 1));
    }

    @Test
    public void checkAddComment() {
        CommentDto commentDto = new CommentDto(1L, "Add comment from user1", "user",
                comment.getCreated());
        LocalDateTime now = LocalDateTime.now();
        Booking bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.WAITING);
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(3L, LocalDateTime.of(2023, 5, 5, 23, 30),
                LocalDateTime.of(2023, 5, 6, 20, 15), item, user1, Status.APPROVED));
        when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(item.getId()))
                .thenReturn(bookings);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(comments);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        itemDto.setLastBooking(entityMapper.bookingToLastBooking(bookingBeforePatch));
        itemDto.setNextBooking(entityMapper.bookingToNextBooking(booking2));
        when(entityMapper.commentToCommentDto(comment))
                .thenReturn(commentDto);
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        when(bookingRepository.findAllByBookerId(user1.getId()))
                .thenReturn(bookings);
        when(entityMapper.commentDtoToComment(commentDto))
                .thenReturn(comment);
        when(entityMapper.itemDtoToItem(itemDto))
                .thenReturn(item);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(commentRepository.save(comment))
                .thenReturn(comment);

        assertThat(itemService.addComment(commentDto, item.getId(), user1.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(comment);
    }
}
