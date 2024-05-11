package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;

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
    User user1;
    User user2;
    ItemDto itemDto;
    Item item;
    ShortBookingDto shortBookingDto;
    Booking bookingBeforePatch;

    @BeforeEach
    public void setup() {
        LocalDateTime now = LocalDateTime.now();
        user1 = new User(1L, "user1", "user@user.com");
        user2 = new User(2L, "user2", "user@another.com");
        itemDto = new ItemDto(
                1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        item = new Item(1L, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        shortBookingDto = new ShortBookingDto(
                1L,
                LocalDateTime.of(2024, 5, 25, 20, 30),
                LocalDateTime.of(2024, 5, 25, 22, 50));
        bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.WAITING);
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
        Booking bookingAfterPatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.APPROVED);
        when(bookingRepository.findById(bookingAfterPatch.getId()))
                .thenReturn(Optional.of(bookingAfterPatch));
        item.setOwner(user2);
        bookingAfterPatch.setItem(item);

        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingBeforePatch.getId(),
                user2.getId(), "true"));
    }

    @Test
    public void checkApprovedBooking() {
        Booking bookingAfterPatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.APPROVED);
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
        Booking bookingAfterPatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.APPROVED);
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
        LocalDateTime now = LocalDateTime.now();
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
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
        LocalDateTime now = LocalDateTime.now();
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
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
        Booking bookingAfterPatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.APPROVED);
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
        LocalDateTime now = LocalDateTime.now();
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
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
        LocalDateTime now = LocalDateTime.now();
        Booking booking2 = new Booking(2L, now.plusDays(5L), now.plusDays(6L), item, user1, Status.WAITING);
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
        Booking bookingAfterPatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
                Status.APPROVED);
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
