package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemServiceImpl itemService;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final EntityMapper entityMapper;
    private final Comparator<Booking> bookingDateComparator = (booking1, booking2) -> {
        if (booking1.getStart().isBefore(booking2.getStart())) {
            return 1;
        } else if (booking1.getStart().isAfter(booking2.getStart())) {
            return -1;
        }
        return 0;
    };

    private void validateAddBooking(Booking inMemoryBookingDto, long userId) {
        if (!inMemoryBookingDto.getItem().getAvailable()) {
            throw new ValidationException("Внимание! Нельзя забронировать недоступную вещь!");
        }
        if (inMemoryBookingDto.getStart().isEqual(inMemoryBookingDto.getEnd())) {
            throw new ValidationException("Внимание! Дата начала и конца бронирования не могут совпадать!");
        }
        if (inMemoryBookingDto.getEnd().isBefore(inMemoryBookingDto.getStart())) {
            throw new ValidationException("Внимание! Дата окончания срока бронирования не может быть " +
                    "раньше её начала!");
        }
        if (inMemoryBookingDto.getItem().getOwner().getId() == userId) {
            throw new EntityNotFoundException("Внимание! Владелец не может забронировать вещь у самого себя!");
        }
    }

    @Override
    public Booking addBooking(ShortBookingDto shortBookingDto, long userId) {
        Booking inMemoryBookingDto = entityMapper.shortBookingDtoToBooking(shortBookingDto);
        inMemoryBookingDto.setBooker(userService.findUser(userId));
        inMemoryBookingDto.setItem(itemService.findItem(shortBookingDto.getItemId()));
        validateAddBooking(inMemoryBookingDto, userId);
        inMemoryBookingDto.setStatus(Status.WAITING);
        log.info("Заявка на бронирование успешно создана!");
        return bookingRepository.save(inMemoryBookingDto);

    }

    @Override
    public Booking updateBooking(long id, long userId, String approved) {
        Booking bookingFromDb = findBookingById(id);
        if (bookingFromDb.getItem().getOwner().getId() != userId) {
            throw new EntityNotFoundException("Внимание! Заявку на бронирование вещи может подтвердить только " +
                    "владелец вещи!");
        }
        if (!approved.equals(String.valueOf(true)) && !approved.equals(String.valueOf(false))) {
            throw new IllegalArgumentException("Внимание! Значение параметра approved может быть только true или false!");
        } else {
            if (approved.equals(String.valueOf(true)) && (bookingFromDb.getStatus().equals(Status.REJECTED)
                    || bookingFromDb.getStatus().equals(Status.WAITING))) {
                bookingFromDb.setStatus(Status.APPROVED);
                log.info("Заявка под номером " + id + " успешно одобрена!");
                return bookingRepository.save(bookingFromDb);
            } else if (approved.equals(String.valueOf(false)) && (bookingFromDb.getStatus().equals(Status.APPROVED)
                    || bookingFromDb.getStatus().equals(Status.WAITING))) {
                bookingFromDb.setStatus(Status.REJECTED);
                log.info("Заявка под номером " + id + " успешно одобрена!");
                return bookingRepository.save(bookingFromDb);
            } else {
                throw new ValidationException("Внимание! Нельзя изменить статус заявки на уже имеющийся!");
            }
        }
    }

    private Booking findBookingById(long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Заявки на бронирование с таким номером не существует!"));
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBooking(long bookingId, long userId) {
        if (!userRepository.existsById(userId) || !bookingRepository.existsById(bookingId)) {
            throw new EntityNotFoundException("Внимание! Пользователя или заявки на бронирование с таким номером " +
                    "не существует!");
        }
        Booking booking = findBookingById(bookingId);
        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            return bookingRepository.getBookingInfo(bookingId, userId);
        } else {
            throw new EntityNotFoundException("Внимание! Информация о бронировании может быть запрошена либо " +
                    "владельцем вещи, либо пользователем, оставившим заявку на бронирование!");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsByUser(long userId, String state, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!");
        }
        if (from < 0) {
            throw new IllegalArgumentException("Низя!");
        }
        LocalDateTime now = LocalDateTime.now();
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(from, size, sortByStart);
        Pageable pageable1 = PageRequest.of(from / size, size, sortByStart);
        switch (state) {
            case "WAITING":
                return bookingRepository.findAllByBookerIdAndStatusEquals(userId,
                                Status.WAITING, pageable).getContent().stream()
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findAllByBookerIdAndEndIsBefore(userId, now, pageable)
                        .getContent().stream()
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findAllByBookerIdAndStartIsBeforeOrderByIdAsc(userId, now, pageable)
                        .getContent().stream()
                        .filter(booking -> booking.getEnd().isAfter(now))
                        .sorted(bookingDateComparator)
                        .sorted(Comparator.comparingLong(Booking::getId))
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findAllByBookerIdAndStartIsAfter(userId, now, pageable)
                        .getContent().stream()
                        .filter(booking -> booking.getStart().isAfter(now))
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findAllByBookerIdAndStatusEquals(userId,
                                Status.REJECTED, pageable).getContent().stream()
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "ALL":
                return bookingRepository.findAllByBookerId(userId, pageable1).getContent();
            default:
                throw new UnsupportedStateException("Unknown state: " + state);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsByOwner(long userId, String state, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!");
        }
        LocalDateTime now = LocalDateTime.now();
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(from, size, sortByStart);
        switch (state) {
            case "WAITING":
                return bookingRepository.getAllBookingsInfoByOwnerLikePage(userId, pageable).getContent().stream()
                        .filter(booking -> booking.getStatus().equals(Status.WAITING))
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.getAllBookingsInfoByOwnerLikePage(userId, pageable).getContent().stream()
                        .filter(booking -> booking.getEnd().isBefore(now))
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.getAllBookingsInfoByOwnerLikePage(userId, pageable).getContent().stream()
                        .filter(booking -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now))
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.getAllBookingsInfoByOwnerLikePage(userId, pageable).getContent().stream()
                        .filter(booking -> booking.getStart().isAfter(now))
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.getAllBookingsInfoByOwnerLikePage(userId, pageable).getContent().stream()
                        .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            case "ALL":
                return bookingRepository.getAllBookingsInfoByOwnerLikePage(userId, pageable).getContent().stream()
                        .sorted(bookingDateComparator)
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedStateException("Unknown state: " + state);
        }
    }

    @Override
    public void deleteBooking(long id) {
        bookingRepository.deleteById(id);
    }
}
