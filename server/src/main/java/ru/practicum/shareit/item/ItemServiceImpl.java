package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.RequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final EntityMapper entityMapper;
    private final UserServiceImpl userService;
    private final RequestRepository requestRepository;

    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        Item item = entityMapper.itemDtoToItem(itemDto);
        item.setOwner(userService.findUser(userId));
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = requestRepository.findById(itemDto.getRequestId()).orElseThrow(() ->
                    new EntityNotFoundException("Внимание! Запроса с таким уникальным номером не существует!"));
            item.setRequest(itemRequest);
        }
        log.info("Информация о новой вещи успешно добавлена!");
        ItemDto itemDtoAfterSave = entityMapper.itemToItemDto(itemRepository.save(item));
        if (itemDto.getRequestId() != null) {
            itemDtoAfterSave.setRequestId(itemDto.getRequestId());
        }
        return itemDtoAfterSave;
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId, long itemId) {
        if (userRepository.existsById(userId)) {
            Item itemFromDb = findItem(itemId);
            Item itemLikeDto = entityMapper.itemDtoToItem(itemDto);
            if (itemLikeDto.getName() != null) {
                itemFromDb.setName(itemLikeDto.getName());
            }
            if (itemLikeDto.getDescription() != null) {
                itemFromDb.setDescription(itemLikeDto.getDescription());
            }
            if (itemLikeDto.getAvailable() != null) {
                itemFromDb.setAvailable(itemLikeDto.getAvailable());
            }
            if (itemFromDb.getOwner().getId() != userId) {
                throw new EntityNotFoundException("Внимание! Допущена ошибка при указании уникального номера " +
                        "владельца вещи!");
            }
            log.info("Информация о вещи с номером " + itemId + " успешно добавлена!");
            return entityMapper.itemToItemDto(itemRepository.save(itemFromDb));
        }
        throw new EntityNotFoundException("Внимание! Пользователя или вещи с таким номером не существует!");
    }

    public Item findItem(long id) {
        return itemRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Вещи с таким номером не существует!"));
    }

    private ItemDto setLastAndNextBooking(Item item, List<Booking> bookings) {
        if (bookings == null) {
            return entityMapper.itemToItemDto(item);
        }
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
        ItemDto itemDto = entityMapper.itemToItemDto(item);
        if (lastBooking != null) {
            itemDto.setLastBooking(entityMapper.bookingToLastBooking(lastBooking));
        }
        if (lastBooking != null) {
            itemDto.setNextBooking(entityMapper.bookingToNextBooking(nextBooking));
        }
        return itemDto;
    }

    private ItemDto setComments(ItemDto itemDto, List<Comment> comments) {
        comments.sort(Comparator.comparing(Comment::getCreated));
        itemDto.setComments(comments.stream()
                .map(entityMapper::commentToCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ItemDto> getItem(long id, long userId) {
        Item item = findItem(id);
        log.info("Успешно получена информация о вещи с номером " + id);
        List<Booking> bookings = bookingRepository.findByItemId(id);
        List<Comment> comments = commentRepository.findAllByItemId(id);
        if (bookings == null) {
            return Optional.of(setComments(setLastAndNextBooking(item, null),
                    comments));
        }
        if (!bookings.isEmpty()) {
            ItemDto itemDto = setComments(setLastAndNextBooking(item, bookings),
                    comments);
            for (Booking booking : bookings) {
                if (booking.getBooker().getId() == userId) {
                    itemDto.setLastBooking(null);
                    itemDto.setNextBooking(null);
                }
            }
            return Optional.of(itemDto);
        }
        return Optional.of(entityMapper.itemToItemDto(item));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getItems(long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Item> allItems = itemRepository.findAllByOwnerIdOrderByIdAsc(userId, pageable).getContent();
        List<Item> itemsWithBookings;
        List<Booking> bookings;
        List<ItemDto> result = new ArrayList<>();
        if (userRepository.existsById(userId)) {
            if (!bookingRepository.getAllBookingsInfoByOwner(userId).isEmpty()) {
                itemsWithBookings = allItems.stream()
                        .filter(item -> !bookingRepository.findByItemId(item.getId()).isEmpty())
                        .collect(Collectors.toList());
                bookings = bookingRepository.findAllByItemInAndStatusOrderByStartAsc(itemsWithBookings, Status.APPROVED);
            } else {
                bookings = bookingRepository.findAllByItemInAndStatusOrderByStartAsc(allItems, Status.APPROVED);
            }
            List<Comment> comments = commentRepository.findAllByAuthorId(userId);
            log.info("Успешно получена информация о всех сохранённых вещах!");
            for (Item item : allItems) {
                ItemDto itemDto = setLastAndNextBooking(item, bookings);
                if (bookingRepository.findByItemId(item.getId()).isEmpty()) {
                    itemDto.setLastBooking(null);
                    itemDto.setNextBooking(null);
                }
                setComments(itemDto, comments);
                result.add(itemDto);
            }
            return result;
        }
        throw new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!");
    }

    @Override
    public void deleteItem(long id) {
        if (itemRepository.existsById(id)) {
            log.info("Информация о вещи с номером " + id + " успешно удалена!");
            itemRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("Внимание! Вещи с таким номером не существует!");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItem(String text, Integer from, Integer size) {
        if (text == null || text.isEmpty() || text.isBlank()) {
            log.info("Описание вещи не указано.");
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of(from, size);
        log.info("Успешно получена информация о вещи по её описанию!");
        return itemRepository.searchByTextLikePage(text, pageable)
                .getContent().stream()
                .filter(Item::getAvailable)
                .map(entityMapper::itemToItemDto).collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(CommentDto commentDto, long itemId, long userId) {
        if (userRepository.existsById(userId) && getItem(itemId, userId).isPresent()) {
            List<Booking> bookingsByBookerId = bookingRepository.findAllByBookerId(userId);
            if (bookingsByBookerId.stream().noneMatch(booking -> booking.getItem().getId() ==
                    itemId && booking.getStatus().equals(Status.APPROVED))) {
                throw new ValidationException("Внимание! Только тот, кто бронировал вещь, может оставить о ней " +
                        "отзыв!");
            }
            if (bookingsByBookerId.stream()
                    .filter(booking -> booking.getItem().getId() == itemId)
                    .noneMatch(booking -> booking.getEnd().isBefore(LocalDateTime.now()))) {
                throw new ValidationException("Внимание! Пользователи могут оставлять отзывы только на завершенные " +
                        "заявки!");
            }
            Comment comment = entityMapper.commentDtoToComment(commentDto);
            comment.setItem(entityMapper.itemDtoToItem(getItem(itemId, userId).orElseThrow(() ->
                    new EntityNotFoundException("Внимание! Пользователя или вещи с таким номером не существует!"))));
            comment.setAuthor(userService.findUser(userId));
            comment.setCreated(LocalDateTime.now());
            log.info("Пользователь с номером " + userId + " успешно оставил комментарий к вещи с номером" + itemId + " !");
            return entityMapper.commentToCommentDto(commentRepository.save(comment));
        }
        throw new EntityNotFoundException("Внимание! Пользователя или вещи с таким номером не существует!");
    }
}
