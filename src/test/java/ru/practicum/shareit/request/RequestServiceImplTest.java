package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.mappers.RequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {

    @InjectMocks
    RequestServiceImpl requestService;
    @Mock
    RequestRepository requestRepository;
    @Mock
    RequestMapper requestMapper;
    @Mock
    UserServiceImpl userService;
    @Mock
    ItemService itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    EntityMapper entityMapper;
    User user1;
    ItemRequestDto itemRequestDto;
    ItemRequest itemRequest;
    ItemDto itemDto;

    @BeforeEach
    public void setup() {
        user1 = new User(1L, "user1", "user@user.com");
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
    }

    @Test
    public void checkAddRequest() {
        when(requestMapper.requestDtoToRequest(itemRequestDto))
                .thenReturn(itemRequest);
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(requestRepository.save(itemRequest))
                .thenReturn(itemRequest);

        assertThat(requestService.addRequest(user1.getId(), itemRequestDto))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(itemRequest);
    }

    @Test
    public void checkGetRequest() {
        Item item = new Item(1L, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(requestMapper.requestToRequestDto(itemRequest))
                .thenReturn(itemRequestDto);
        when(requestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAll())
                .thenReturn(List.of(item));
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        when(itemService.searchItem("стол", null, null))
                .thenReturn(List.of(itemDto));
        itemRequestDto.setItems(itemService.searchItem("стол", null, null));

        assertThat(requestService.getItemRequest(user1.getId(), itemRequest.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(itemRequestDto));
    }

    @Test
    public void checkGetRequestIfRequestNotExists() {
        userService.findUser(user1.getId());

        assertThrows(EntityNotFoundException.class, () -> requestService.getItemRequest(user1.getId(), 100L));
    }

    @Test
    public void checkGetYoursRequest() {
        Item item = new Item(1L, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);
        when(requestMapper.requestToRequestDto(itemRequest))
                .thenReturn(itemRequestDto);
        when(requestRepository.findAllByRequesterIdOrderByCreatedDesc(user1.getId()))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findAll())
                .thenReturn(List.of(item));
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        when(itemService.searchItem(itemDto.getName(), 1, 10))
                .thenReturn(List.of(itemDto));
        itemRequestDto.setItems(itemService.searchItem(itemDto.getName(), 1, 10));

        assertThat(requestService.getYoursRequest(user1.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(itemRequestDto));
    }

    @Test
    public void checkGetOtherRequest() {
        Item item = new Item(1L, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        when(requestRepository.findAll())
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findAll())
                .thenReturn(List.of(item));
        when(entityMapper.itemToItemDto(item))
                .thenReturn(itemDto);
        when(itemService.searchItem(itemDto.getName(), 1, 10))
                .thenReturn(List.of(itemDto));
        itemRequestDto.setItems(itemService.searchItem(itemDto.getName(), 1, 10));

        assertThat(requestService.getOthersRequests(user1.getId(), 0L, 20))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of());
    }

    @Test
    public void getOtherRequestShouldThrowExceptionIfFromAndAndSizeIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> requestService.getOthersRequests(user1.getId(), -1L, -20));
    }

    @Test
    public void getOtherRequestReturnEmptyListIfFromAndAndSizeIsNull() {
        assertTrue(requestService.getOthersRequests(user1.getId(), null, null).isEmpty());
    }

    @Test
    public void checkDeleteItemRequest() {
        when(requestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(userService.findUser(user1.getId()))
                .thenReturn(user1);

        requestService.removeRequest(user1.getId(), itemRequest.getId());
    }

    @Test
    public void checkDeleteItemRequestIfUserISnotRequester() {
        User user2 = new User(2L, "user1", "user@user.com");
        itemRequest = new ItemRequest(1L, "Хотел бы воспользоваться щёткой для обуви", user1,
                itemRequestDto.getCreated());
        when(requestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(userService.findUser(user2.getId()))
                .thenReturn(user2);

        assertThrows(ValidationException.class, () -> requestService.removeRequest(user2.getId(), itemRequest.getId()));
    }
}
