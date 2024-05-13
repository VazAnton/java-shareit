package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.mappers.RequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserServiceImpl userService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final EntityMapper entityMapper;

    @Override
    public ItemRequestDto addRequest(long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = requestMapper.requestDtoToRequest(itemRequestDto);
        itemRequest.setRequester(userService.findUser(userId));
        itemRequest.setCreated(LocalDateTime.now());
        log.info("Запрос на выбранную вещь успешно создан!");
        return requestMapper.requestToRequestDto(requestRepository.save(itemRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ItemRequestDto> getItemRequest(long userId, long requestId) {
        userService.findUser(userId);
        ItemRequest itemRequest = findRequest(requestId);
        setItemsToRequest(List.of(itemRequest));
        log.info("Успешно получена информация о выбранном запросе!");
        return Optional.of(setItemsToRequest(List.of(itemRequest)).get(0));
    }

    private ItemRequest findRequest(long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Запроса с таким уникальным номером не существует!"));
    }

    private List<ItemRequestDto> setItemsToRequest(List<ItemRequest> requests) {
        List<ItemRequestDto> result = requests.stream()
                .map(requestMapper::requestToRequestDto)
                .collect(Collectors.toList());
        List<ItemDto> allItems = itemRepository.findAll().stream()
                .map(entityMapper::itemToItemDto)
                .collect(Collectors.toList());
        for (ItemRequestDto itemRequestDto : result) {
            for (ItemDto itemDto : allItems) {
                String commonPart = itemDto.getName().toLowerCase().substring(0, 4);
                if (itemRequestDto.getDescription().contains(commonPart)) {
                    itemRequestDto.setItems(itemService.searchItem(commonPart, null, null));
                    for (ItemDto requestsItem : itemRequestDto.getItems()) {
                        requestsItem.setRequestId(itemRequestDto.getId());
                    }
                    break;
                } else {
                    itemRequestDto.setItems(new ArrayList<>());
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getYoursRequest(long userId) {
        userService.findUser(userId);
        log.info("Успешно получена информация о Ваших запросах!");
        return setItemsToRequest(requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOthersRequests(long userId, Integer from, Integer size) {
        if ((from < 0 || size < 0) || (from == 0 && size == 0)) {
            throw new IllegalArgumentException("Внимание! Передано неверное значение from или/и size");
        }
        List<ItemRequest> othersRequests = requestRepository.findAll().stream()
                .filter(itemRequest -> itemRequest.getRequester().getId() != userId)
                .collect(Collectors.toList());
        log.info("Успешно получена информация о запросах, созданных другими пользователями!");
        return setItemsToRequest(othersRequests);
    }

    @Override
    public void removeRequest(long userId, long requestId) {
        ItemRequest itemRequest = findRequest(requestId);
        userService.findUser(userId);
        if (itemRequest.getRequester().getId() != userId) {
            throw new ValidationException("Внимание! Только пользователь, создавший запрос, может его удалить!");
        }
        log.info("Успешно удалена информация о выбранном запросе!");
        requestRepository.delete(itemRequest);
    }
}
