package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.Optional;

public interface RequestService {

    ItemRequest addRequest(long userId, ItemRequestDto itemRequestDto);

    Optional<ItemRequestDto> getItemRequest(long userId, long requestId);

    List<ItemRequestDto> getYoursRequest(long userId);

    List<ItemRequestDto> getOthersRequests(long userId, Long from, Integer size);

    void removeRequest(long userId, long requestId);
}
