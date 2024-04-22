package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final EntityMapper entityMapper;

    @Transactional
    @Override
    public Item addItem(ItemDto itemDto, long userId) {
        Item item = entityMapper.itemDtoToItem(itemDto);
        item.setOwner(userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!")));
        log.info("Информация о новой вещи успешно добавлена!");
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item updateItem(ItemDto itemDto, long userId, long itemId) {
        if (userRepository.existsById(userId)) {
            Item itemFromDb = itemRepository.findById(itemId).orElseThrow(() ->
                    new EntityNotFoundException("Внимание! Вещи с таким номером не существует!"));
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
            return itemRepository.save(itemFromDb);
        }
        throw new EntityNotFoundException("Внимание! Пользователя или вещи с таким номером не существует!");
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
        comments.sort((comment1, comment2) -> {
            if (comment1.getCreated().isBefore(comment2.getCreated())) {
                return 1;
            } else if (comment1.getCreated().isAfter(comment2.getCreated())) {
                return -1;
            }
            return 0;
        });
        itemDto.setComments(comments.stream()
                .map(entityMapper::commentToCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ItemDto> getItem(long id, long userId) {
        Item item = itemRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Пользователя или вещи с таким номером не существует!"));
        log.info("Успешно получена информация о вещи с номером " + id);
        List<Booking> bookings = bookingRepository.findByItemId(id);
        List<Comment> comments = commentRepository.findAllByItemId(id);
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
    public List<ItemDto> getItems(long userId) {
        if (userRepository.existsById(userId)) {
            log.info("Успешно получена информация о всех сохранённых вещах!");
            List<Item> items = itemRepository.findAllByOwnerId(userId);
            List<Booking> bookings = bookingRepository.findAllByItemInAndStatusOrderByStartAsc(items, Status.APPROVED);
            List<Comment> comments = commentRepository.findAllByAuthorId(userId);
            return items.stream()
                    .map(item -> setLastAndNextBooking(item, bookings))
                    .map(itemDto -> setComments(itemDto, comments))
                    .collect(Collectors.toList());
//            if (bookingRepository.findAll().isEmpty() /*||
//                    commentRepository.findAllByAuthorId(userId).isEmpty()*/) {
//                return itemRepository.findAllByOwnerId(userId).stream()
//                        .map(entityMapper::itemToItemDto)
//                        .collect(Collectors.toList());
//            }
////            List<ItemDto> ownersItems = new ArrayList<>();
////            for (Item item : itemRepository.findAllByOwnerId(userId)) {
////                ItemDto itemDto;
////                if (item.getOwner().getId() == userId) {
////                    itemDto = setComments(setLastAndNextBooking(item,
////                                    bookingService.getBookingsByOwner(userId, "ALL")),
////                            commentRepository.findAllByAuthorId(userId));
////                    ownersItems.add(itemDto);
////                } else {
////                    itemDto = entityMapper.itemToItemDto(item);
////                    itemDto.setLastBooking(null);
////                    itemDto.setNextBooking(null);
////                    ownersItems.add(itemDto);
////                }
////            }
////            return ownersItems;
//            return itemRepository.findAllByOwnerId(userId).stream()
//                    .map(item -> setLastAndNextBooking(item, bookingService.getBookingsByOwner(userId, "ALL")))
//                    .map(itemDto -> setComments(itemDto, commentRepository.findAllByAuthorId(userId)))
//                    .collect(Collectors.toList());
        }
        throw new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!");
    }

    @Transactional
    @Override
    public void deleteItem(long id) {
        log.info("Информация о вещи с номером " + id + " успешно удалена!");
        itemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItem(String text) {
        if (text != null && (!text.isEmpty() || !text.isBlank())) {
            log.info("Успешно получена информация о вещи по её описанию!");
            return itemRepository.searchByText(text).stream()
                    .filter(Item::getAvailable)
                    .map(entityMapper::itemToItemDto).collect(Collectors.toList());
        }
        log.info("Описание вещи не указано.");
        return new ArrayList<>();
    }

    @Transactional
    @Override
    public Comment addComment(CommentDto commentDto, long itemId, long userId) {
        if (userRepository.existsById(userId) && getItem(itemId, userId).isPresent()) {
            if (bookingRepository.findAllByBookerId(userId).stream().noneMatch(booking -> booking.getItem().getId() == itemId &&
                    booking.getStatus().equals(Status.APPROVED))) {
                throw new ValidationException("Внимание! Только тот, кто бронировал вещь, может оставить о ней " +
                        "отзыв!");
            }
            if (bookingRepository.findAllByBookerId(userId).stream()
                    .filter(booking -> booking.getItem().getId() == itemId)
                    .noneMatch(booking -> booking.getEnd().isBefore(LocalDateTime.now()))) {
                throw new ValidationException("Внимание! Пользователи могут оставлять отзывы только на завершенные " +
                        "заявки!");
            }
            Comment comment = entityMapper.commentDtoToComment(commentDto);
            comment.setItem(entityMapper.itemDtoToItem(getItem(itemId, userId).orElseThrow(() ->
                    new EntityNotFoundException("Внимание! Пользователя или вещи с таким номером не существует!"))));
            comment.setAuthor(userRepository.findById(userId).orElseThrow(() ->
                    new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!")));
            comment.setCreated(LocalDateTime.now());
            log.info("Пользователь с номером " + userId + "успешно оставил комментарий к вещи с номером" + itemId + " !");
            return commentRepository.save(comment);
        }
        throw new EntityNotFoundException("Внимание! Пользователя или вещи с таким номером не существует!");
    }
}
