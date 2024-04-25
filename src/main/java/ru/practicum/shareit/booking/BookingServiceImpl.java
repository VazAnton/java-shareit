package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final Comparator<Booking> bookingDateComparator = (booking1, booking2) -> {
        if (booking1.getStart().isBefore(booking2.getStart())) {
            return 1;
        } else if (booking1.getStart().isAfter(booking2.getStart())) {
            return -1;
        }
        return 0;
    };

    @Override
    @Transactional
    public Booking addBooking(ShortBookingDto shortBookingDto, long userId) {
        Booking inMemoryBookingDto = entityMapper.shortBookingDtoToBooking(shortBookingDto);
        inMemoryBookingDto.setBooker(userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!")));
        inMemoryBookingDto.setItem(itemRepository.findById(shortBookingDto.getItemId()).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Вещи с таким номером не существует!")));
        if (!inMemoryBookingDto.getItem().getAvailable()) {
            throw new ValidationException("Внимание! Нельзя забронировать недоступную вещь!");
        }
        if (inMemoryBookingDto.getStart().isEqual(inMemoryBookingDto.getEnd())) {
            throw new ValidationException("Внимание! Дата начала и конца бронирования не могут совпадать!");
        } else if (inMemoryBookingDto.getEnd().isBefore(inMemoryBookingDto.getStart())) {
            throw new ValidationException("Внимание! Дата окончания срока бронирования не может быть " +
                    "раньше её начала!");
        }
        if (inMemoryBookingDto.getItem().getOwner().getId() == userId) {
            throw new EntityNotFoundException("Внимание! Владелец не может забронировать вещь у самого себя!");
        }
        inMemoryBookingDto.setStatus(Status.WAITING);
        log.info("Заявка на бронирование успешно создана!");
        return bookingRepository.save(inMemoryBookingDto);

    }

    @Override
    @Transactional
    public Booking updateBooking(long id, long userId, String approved) {
        Booking bookingFromDb = bookingRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Заявки на бронирование с таким номером не существует!"));
        if (bookingFromDb.getItem().getOwner().getId() != userId) {
            throw new EntityNotFoundException("Внимание! Заявку на бронирование вещи может подтвердить только " +
                    "владелец вещи!");
        }
        if (!Boolean.valueOf(approved).equals(true) && !Boolean.valueOf(approved).equals(false)) {
            throw new IllegalArgumentException("Внимание! Значение параметра approved может быть только true или false!");
        } else {
            if (Boolean.valueOf(approved).equals(true) &&
                    (bookingFromDb.getStatus().equals(Status.REJECTED) || bookingFromDb.getStatus().equals(Status.WAITING))) {
                bookingFromDb.setStatus(Status.APPROVED);
                log.info("Заявка под номером " + id + " успешно одобрена!");
                return bookingRepository.save(bookingFromDb);
            } else if (Boolean.valueOf(approved).equals(false) &&
                    (bookingFromDb.getStatus().equals(Status.APPROVED) || bookingFromDb.getStatus().equals(Status.WAITING))) {
                bookingFromDb.setStatus(Status.REJECTED);
                log.info("Заявка под номером " + id + " успешно одобрена!");
                return bookingRepository.save(bookingFromDb);
            } else {
                throw new ValidationException("Внимание! Нельзя изменить статус заявки на уже имеющийся!");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBooking(long bookingId, long userId) {
        if (!userRepository.existsById(userId) || !bookingRepository.existsById(bookingId)) {
            throw new EntityNotFoundException("Внимание! Пользователя или заявки на бронирование с таким номером " +
                    "не существует!");
        }
        if (bookingRepository.findById(bookingId).get().getBooker().getId() == userId ||
                bookingRepository.findById(bookingId).get().getItem().getOwner().getId() == userId) {
            return bookingRepository.getBookingInfo(bookingId, userId);
        } else {
            throw new EntityNotFoundException("Внимание! Информация о бронировании может быть запрошена либо " +
                    "владельцем вещи, либо пользователем, оставившим заявку на бронирование!");
        }
    }

    private List<Booking> getBookingByBookerAndStatus(long bookerId, Status status) {
        return bookingRepository.findAllByBookerIdAndStatusEquals(bookerId, status);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsByUser(long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!");
        }
        LocalDateTime now = LocalDateTime.now();
        if (State.valueOf(state).equals(State.WAITING)) {
            return getBookingByBookerAndStatus(userId, Status.WAITING).stream()
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.PAST)) {
            return bookingRepository.findAllByBookerIdAndEndIsBefore(userId, now).stream()
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.CURRENT)) {
            return bookingRepository.findAllByBookerIdAndStartIsBeforeOrderByIdAsc(userId, now).stream()
                    .filter(booking -> booking.getEnd().isAfter(now))
                    .sorted(bookingDateComparator)
                    .sorted((booking1, booking2) -> {
                        if (booking1.getId() < booking2.getId()) {
                            return -1;
                        } else if (booking1.getId() > booking2.getId()) {
                            return 1;
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.FUTURE)) {
            return bookingRepository.findAllByBookerId(userId).stream()
                    .filter(booking -> booking.getStart().isAfter(now))
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.REJECTED)) {
            return getBookingByBookerAndStatus(userId, Status.REJECTED).stream()
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.ALL)) {
            return bookingRepository.findAllByBookerId(userId).stream()
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else {
            throw new UnsupportedStateException("Unknown state: " + state);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getBookingsByOwner(long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!");
        }
        LocalDateTime now = LocalDateTime.now();
        if (State.valueOf(state).equals(State.WAITING)) {
            return bookingRepository.getAllBookingsInfoByOwner(userId).stream()
                    .filter(booking -> booking.getStatus().equals(Status.WAITING))
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.PAST)) {
            return bookingRepository.getAllBookingsInfoByOwner(userId).stream()
                    .filter(booking -> booking.getEnd().isBefore(now))
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.CURRENT)) {
            return bookingRepository.getAllBookingsInfoByOwner(userId).stream()
                    .filter(booking -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now))
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.FUTURE)) {
            return bookingRepository.getAllBookingsInfoByOwner(userId).stream()
                    .filter(booking -> booking.getStart().isAfter(now))
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.REJECTED)) {
            return bookingRepository.getAllBookingsInfoByOwner(userId).stream()
                    .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else if (State.valueOf(state).equals(State.ALL)) {
            return bookingRepository.getAllBookingsInfoByOwner(userId).stream()
                    .sorted(bookingDateComparator)
                    .collect(Collectors.toList());
        } else {
            throw new UnsupportedStateException("Unknown state: " + state);
        }
    }

    @Transactional
    @Override
    public void deleteBooking(long id) {
        bookingRepository.deleteById(id);
    }
}
