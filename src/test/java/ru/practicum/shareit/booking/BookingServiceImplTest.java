package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.LastBooking;
import ru.practicum.shareit.booking.dto.NextBooking;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @InjectMocks
    BookingServiceImpl bookingService;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemServiceImpl itemService;
    @Mock
    UserRepository userRepository;
    @Mock
    UserServiceImpl userService;
    @Mock
    EntityMapper entityMapper;
    UserDto userDto;
    User user1;
    User user2;
    ItemRequestDto itemRequestDto;
    ItemRequest itemRequest;
    ItemDto itemDto;
    ItemDto itemDto2;
    Item item;
    Item item2;
    ShortBookingDto shortBookingDto;
    Booking bookingBeforePatch;
    Booking booking2;
    Booking bookingAfterPatch;
    LastBooking lastBooking;
    NextBooking nextBooking;
    CommentDto commentDto;
    CommentDto commentDto2;
    CommentDto commentDto3;
    Comment comment;
    Comment comment2;
    Comment comment3;

    @BeforeEach
    public void setup() {
        LocalDateTime now = LocalDateTime.now();
        userDto = new UserDto(
                1L,
                "user",
                "user@user.com");
        user1 = new User(1L, "user1", "user@user.com");
        user2 = new User(2L, "user2", "user@another.com");
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
        itemDto2 = new ItemDto(2L, "Кухонный стул",
                "Стул для празднования",
                true);
        item = new Item(1L, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        item2 = new Item(2L, itemDto2.getName(), itemDto2.getDescription(), itemDto2.getAvailable());
        shortBookingDto = new ShortBookingDto(
                1L,
                LocalDateTime.of(2024, 5, 25, 20, 30),
                LocalDateTime.of(2024, 5, 25, 22, 50));
        bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.WAITING);
        booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
        bookingAfterPatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.APPROVED);
        commentDto2 = new CommentDto(2L, "Add new comment from user1", "user",
                now.plusMinutes(5L));
        commentDto3 = new CommentDto(2L, "Add new comment from user1", "user",
                now.minusDays(5L));
        comment = new Comment(1L, "Add comment from user1", item, user1, now);
        commentDto = new CommentDto(1L, "Add comment from user1", "user", comment.getCreated());
        comment2 = new Comment(2L, "Add new comment from user1", item, user1,
                now.plusMinutes(5L));
        comment3 = new Comment(2L, "Add new comment from user1", item, user1,
                now.minusDays(5L));
        lastBooking = new LastBooking(1L, user1.getId());
        nextBooking = new NextBooking(1L, user1.getId());
    }

    @Test
    public void addBookingShouldThrowValidationExceptionIfItemIsNotAvailable() {
        when(entityMapper.shortBookingDtoToBooking(shortBookingDto))
                .thenReturn(bookingBeforePatch);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(itemService.findItem(item.getId()))
                .thenReturn(item);
        item.setAvailable(false);
        bookingBeforePatch.setItem(item);

        assertThrows(ValidationException.class, () -> bookingService.addBooking(shortBookingDto, user1.getId()));
    }

    @Test
    public void addBookingShouldThrowValidationExceptionIfStartIsEqualToEnd() {
        when(entityMapper.shortBookingDtoToBooking(shortBookingDto))
                .thenReturn(bookingBeforePatch);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(itemService.findItem(item.getId()))
                .thenReturn(item);
        bookingBeforePatch.setStart(shortBookingDto.getEnd());

        assertThrows(ValidationException.class, () -> bookingService.addBooking(shortBookingDto, user1.getId()));
    }

    @Test
    public void addBookingShouldThrowValidationExceptionIfEndIsBeforeStart() {
        when(entityMapper.shortBookingDtoToBooking(shortBookingDto))
                .thenReturn(bookingBeforePatch);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(itemService.findItem(item.getId()))
                .thenReturn(item);
        bookingBeforePatch.setEnd(shortBookingDto.getStart().minusHours(20));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(shortBookingDto, user1.getId()));
    }

    @Test
    public void addBookingShouldThrowValidationExceptionIfOwnerWantBookItem() {
        when(entityMapper.shortBookingDtoToBooking(shortBookingDto))
                .thenReturn(bookingBeforePatch);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(itemService.findItem(item.getId()))
                .thenReturn(item);
        item.setOwner(user1);
        bookingBeforePatch.setItem(item);

        assertThrows(EntityNotFoundException.class, () -> bookingService.addBooking(shortBookingDto, user1.getId()));
    }

    @Test
    public void checkAddBooking() {
        when(entityMapper.shortBookingDtoToBooking(shortBookingDto))
                .thenReturn(bookingBeforePatch);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(itemService.findItem(item.getId()))
                .thenReturn(item);
        when(bookingRepository.save(bookingBeforePatch))
                .thenReturn(bookingBeforePatch);
        item.setOwner(user2);

        assertThat(bookingService.addBooking(shortBookingDto, user1.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(bookingBeforePatch);
    }

    @Test
    public void getBookingShouldThrowEntityNotFondExceptionIfUserOrBookingNotExists() {
        assertThrows(EntityNotFoundException.class, () -> bookingService.getBooking(100L, 100L));
    }

    @Test
    public void getBookingShouldThrowEntityNotFondExceptionIfBookingNotExists() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(bookingRepository.existsById(bookingBeforePatch.getId()))
                .thenReturn(true);

        assertThrows(EntityNotFoundException.class, () -> bookingService.getBooking(bookingBeforePatch.getId(), user1.getId()));
    }

    @Test
    public void getBookingShouldThrowEntityNotFondExceptionIfUserIsNotOwnerOrBooker() {
        User user3 = new User(3L, "user3", "and_another@one.com");
        when(userRepository.existsById(user3.getId()))
                .thenReturn(true);
        when(bookingRepository.existsById(bookingBeforePatch.getId()))
                .thenReturn(true);
        when(bookingRepository.findById(bookingBeforePatch.getId()))
                .thenReturn(Optional.of(bookingBeforePatch));
        item.setOwner(user2);
        bookingBeforePatch.setItem(item);

        assertThrows(EntityNotFoundException.class, () -> bookingService.getBooking(bookingBeforePatch.getId(),
                user3.getId()));
    }

    @Test
    public void checkGetBooking() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(bookingRepository.existsById(bookingBeforePatch.getId()))
                .thenReturn(true);
        when(bookingRepository.findById(bookingBeforePatch.getId()))
                .thenReturn(Optional.of(bookingBeforePatch));
        item.setOwner(user2);
        bookingBeforePatch.setItem(item);
        when(bookingRepository.getBookingInfo(bookingBeforePatch.getId(), user1.getId()))
                .thenReturn(bookingBeforePatch);

        assertThat(bookingService.getBooking(bookingBeforePatch.getId(), user1.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(bookingBeforePatch);
    }

    @Test
    public void checkDeleteBooking() {
        bookingService.deleteBooking(bookingBeforePatch.getId());
    }

    @Test
    public void updateBookingShouldThrowEntityNotFoundExceptionIfApprovedWrong() {
        when(bookingRepository.findById(bookingBeforePatch.getId()))
                .thenReturn(Optional.of(bookingBeforePatch));
        item.setOwner(user2);

        assertThrows(IllegalArgumentException.class, () -> bookingService.updateBooking(bookingBeforePatch.getId(),
                user2.getId(), "Wrong"));
    }

    @Test
    public void updateBookingShouldThrowIllegalArgumentExceptionIfUserIdIsNotEqualsOwnerId() {
        when(bookingRepository.findById(bookingBeforePatch.getId()))
                .thenReturn(Optional.of(bookingBeforePatch));
        item.setOwner(user2);

        assertThrows(EntityNotFoundException.class, () -> bookingService.updateBooking(bookingBeforePatch.getId(),
                user1.getId(), "APPROVED"));
    }

    @Test
    public void updateBookingShouldThrowValidationExceptionIfStatusIsApprovedYet() {
        when(bookingRepository.findById(bookingAfterPatch.getId()))
                .thenReturn(Optional.of(bookingAfterPatch));
        item.setOwner(user2);
        bookingAfterPatch.setItem(item);

        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingBeforePatch.getId(),
                user2.getId(), "true"));
    }

    @Test
    public void checkApprovedBooking() {
        when(bookingRepository.findById(bookingBeforePatch.getId()))
                .thenReturn(Optional.of(bookingBeforePatch));
        item.setOwner(user2);
        bookingBeforePatch.setItem(item);
        when(bookingRepository.save(bookingBeforePatch))
                .thenReturn(bookingBeforePatch);

        assertThat(bookingService.updateBooking(bookingBeforePatch.getId(), user2.getId(), "true"))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(bookingAfterPatch);
    }

    @Test
    public void checkRejectBooking() {
        when(bookingRepository.findById(bookingBeforePatch.getId()))
                .thenReturn(Optional.of(bookingBeforePatch));
        item.setOwner(user2);
        bookingBeforePatch.setItem(item);
        when(bookingRepository.save(bookingBeforePatch))
                .thenReturn(bookingBeforePatch);
        bookingAfterPatch.setStatus(Status.REJECTED);

        assertThat(bookingService.updateBooking(bookingBeforePatch.getId(), user2.getId(), "false"))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(bookingAfterPatch);
    }

    @Test
    public void getBookingsByUserShouldThrowEntityNotFoundExceptionIfUserNotExists() {
        assertThrows(EntityNotFoundException.class, () -> bookingService.getBookingsByUser(100L, "ALL",
                0, 0));
    }

    @Test
    public void getBookingsByUserShouldThrowIllegalArgumentExceptionIfFromIsNegative() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> bookingService.getBookingsByUser(user1.getId(), "ALL",
                -1, 0));
    }

    @Test
    public void getBookingsByUserShouldThrowUnsupportedStateExceptionIfStateIsWrong() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);

        assertThrows(UnsupportedStateException.class, () -> bookingService.getBookingsByUser(user1.getId(),
                "UNSUPPORTED_STATUS", 1, 10));
    }

    @Test
    public void checkGetBookingsByUserIfStateIsWaiting() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.findAllByBookerIdAndStatusEquals(user1.getId(),
                Status.WAITING, pageable))
                .thenReturn(pagedBookings);

        assertEquals(2, bookingService.getBookingsByUser(user1.getId(), "WAITING", 0, 10).size());
    }

    @Test
    public void checkGetBookingsByUserIfStateIsAll() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0 / 10, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.findAllByBookerId(user1.getId(), pageable))
                .thenReturn(pagedBookings);

        assertEquals(2, bookingService.getBookingsByUser(user1.getId(), "ALL", 0, 10).size());
    }

    @Test
    public void checkGetBookingsByUserIfStateIsRejected() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        bookingAfterPatch.setStatus(Status.REJECTED);
        bookings.add(bookingAfterPatch);
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.findAllByBookerIdAndStatusEquals(user1.getId(), Status.REJECTED, pageable))
                .thenReturn(pagedBookings);

        assertEquals(1, bookingService.getBookingsByUser(user1.getId(), "REJECTED", 0, 10).size());
    }

    @Test
    public void getBookingsByOwnerShouldThrowEntityNotFoundExceptionIfUserNotExists() {
        assertThrows(EntityNotFoundException.class, () -> bookingService.getBookingsByOwner(100L, "ALL",
                0, 0));
    }

    @Test
    public void getBookingsByOwnerShouldThrowUnsupportedStateExceptionIfStateIsWrong() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);

        assertThrows(UnsupportedStateException.class, () -> bookingService.getBookingsByOwner(user1.getId(),
                "UNSUPPORTED_STATUS", 1, 10));
    }

    @Test
    public void checkGetBookingsByOwnerIfStatusWaiting() {
        when(userRepository.existsById(user2.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        item.setOwner(user2);
        bookingBeforePatch.setItem(item);
        booking2.setItem(item);
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.getAllBookingsInfoByOwnerLikePage(user2.getId(), pageable))
                .thenReturn(pagedBookings);

        assertThat(bookingService.getBookingsByOwner(user2.getId(), "WAITING", 0, 10))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(bookings);
    }

    @Test
    public void checkGetBookingsByOwnerIfStatusIsAll() {
        when(userRepository.existsById(user2.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        item.setOwner(user2);
        bookingBeforePatch.setItem(item);
        booking2.setItem(item);
        bookings.add(bookingBeforePatch);
        bookings.add(booking2);
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.getAllBookingsInfoByOwnerLikePage(user2.getId(), pageable))
                .thenReturn(pagedBookings);

        assertEquals(2, bookingService.getBookingsByOwner(user2.getId(), "ALL", 0, 10).size());
    }

    @Test
    public void checkGetBookingsByOwnerIfStateIsRejected() {
        when(userRepository.existsById(user2.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        item.setOwner(user2);
        bookingBeforePatch.setItem(item);
        bookingAfterPatch.setStatus(Status.REJECTED);
        bookings.add(bookingAfterPatch);
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.getAllBookingsInfoByOwnerLikePage(user2.getId(), pageable))
                .thenReturn(pagedBookings);

        assertEquals(1, bookingService.getBookingsByOwner(user2.getId(), "REJECTED", 0, 10).size());
    }

    @Test
    public void checkGetBookingsByOwnerIfStateIsPast() {
        when(userRepository.existsById(user2.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(4L, LocalDateTime.of(2022, 5, 5, 23, 30),
                LocalDateTime.of(2022, 6, 6, 20, 15), item, user1, Status.APPROVED));
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.getAllBookingsInfoByOwnerLikePage(user2.getId(), pageable))
                .thenReturn(pagedBookings);

        assertEquals(1, bookingService.getBookingsByOwner(user2.getId(), "PAST", 0, 10).size());
    }

    @Test
    public void checkGetBookingsByOwnerIfStateIsFuture() {
        when(userRepository.existsById(user2.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(4L, LocalDateTime.of(2025, 5, 5, 23, 30),
                LocalDateTime.of(2025, 6, 6, 20, 15), item, user1, Status.APPROVED));
        bookings.add(new Booking(3L, LocalDateTime.of(2026, 5, 5, 23, 30),
                LocalDateTime.of(2026, 6, 6, 20, 15), item, user1, Status.APPROVED));
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.getAllBookingsInfoByOwnerLikePage(user2.getId(), pageable))
                .thenReturn(pagedBookings);

        assertEquals(2, bookingService.getBookingsByOwner(user2.getId(), "FUTURE", 0, 10).size());
    }

    @Test
    public void checkGetBookingsByOwnerIfStateIsCurrent() {
        when(userRepository.existsById(user2.getId()))
                .thenReturn(true);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(4L, LocalDateTime.of(2023, 5, 5, 23, 30),
                LocalDateTime.of(2025, 6, 6, 20, 15), item, user1, Status.APPROVED));
        bookings.add(new Booking(3L, LocalDateTime.of(2024, 5, 5, 23, 30),
                LocalDateTime.of(2026, 6, 6, 20, 15), item, user1, Status.APPROVED));
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.getAllBookingsInfoByOwnerLikePage(user2.getId(), pageable))
                .thenReturn(pagedBookings);

        assertEquals(2, bookingService.getBookingsByOwner(user2.getId(), "CURRENT", 0, 10).size());
    }
}
