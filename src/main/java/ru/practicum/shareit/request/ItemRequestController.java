package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mappers.RequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final RequestMapper requestMapper;
    private final RequestService requestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return requestMapper.requestToRequestDto(requestService.addRequest(userId, itemRequestDto));
    }

    @GetMapping("/{requestId}")
    public Optional<ItemRequestDto> getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @PathVariable long requestId) {
        return requestService.getItemRequest(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.getYoursRequest(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(required = false) Long from,
                                               @RequestParam(required = false) Integer size) {
        return requestService.getOthersRequests(userId, from, size);
    }
}
