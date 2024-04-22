//package ru.practicum.shareit.item;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.practicum.shareit.exception.EntityNotFoundException;
//import ru.practicum.shareit.item.dto.ItemDto;
//import ru.practicum.shareit.request.ItemRequest;
//import ru.practicum.shareit.user.User;
//import ru.practicum.shareit.user.UserRepositoryWithoutBd;
//import ru.practicum.shareit.user.dto.UserDto;
//import ru.practicum.shareit.user.dto.UserMapper;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class ItemRepositoryWithoutBdTest {
//
//    UserRepositoryWithoutBd userRepositoryWithoutBd;
//    ItemRepositoryWithoutDb itemRepositoryWithoutDb;
//
//    @BeforeEach
//    public void setup() {
//        userRepositoryWithoutBd = new UserRepositoryWithoutBd();
//        itemRepositoryWithoutDb = new ItemRepositoryWithoutDb(userRepositoryWithoutBd);
//    }
//
//    @Test
//    public void checkAddItemIfUserExists() {
//        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
//        userRepositoryWithoutBd.addUser(testedUserDto);
//        User owner = UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(1));
//        ItemDto testedItemDto = new ItemDto(0L, "Дрель", "Простая дрель", owner, true,
//                new ItemRequest());
//
//        itemRepositoryWithoutDb.addItem(testedItemDto, 1L);
//
//        assertNotNull(itemRepositoryWithoutDb.getItem(1L));
//        assertEquals(1, itemRepositoryWithoutDb.getItems(1L).size());
//    }
//
//    @Test
//    public void addItemShouldThrowNotFoundExceptionIfUserNotExists() {
//        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
//        userRepositoryWithoutBd.addUser(testedUserDto);
//        User owner = UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(1));
//        ItemDto testedItemDto = new ItemDto(0L, "Дрель", "Простая дрель", owner, true,
//                new ItemRequest());
//
//        assertThrows(EntityNotFoundException.class, () -> itemRepositoryWithoutDb.addItem(testedItemDto, 2L));
//    }
//
//    @Test
//    public void getItemShouldThrowNotFoundExceptionIfItemNotExists() {
//        assertThrows(EntityNotFoundException.class, () -> itemRepositoryWithoutDb.getItem(1L));
//    }
//
//    @Test
//    public void checkUpdateItemIfUserExists() {
//        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
//        userRepositoryWithoutBd.addUser(testedUserDto);
//        User owner = UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(1));
//        ItemDto testedItemDto = new ItemDto(0L, "Дрель", "Простая дрель", owner, true,
//                new ItemRequest());
//        itemRepositoryWithoutDb.addItem(testedItemDto, 1L);
//        ItemDto newtestedItemDto = new ItemDto(1L, "Дрель +", "Аккумуляторная дрель", owner,
//                false, new ItemRequest());
//
//        itemRepositoryWithoutDb.updateItem(newtestedItemDto, 1L, newtestedItemDto.getId());
//
//        assertEquals(newtestedItemDto, itemRepositoryWithoutDb.getItem(1L));
//    }
//
//    @Test
//    public void checkDeleteItem() {
//        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
//        userRepositoryWithoutBd.addUser(testedUserDto);
//        User owner = UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(1));
//        ItemDto testedItemDto = new ItemDto(0L, "Дрель", "Простая дрель", owner, true,
//                new ItemRequest());
//        itemRepositoryWithoutDb.addItem(testedItemDto, 1L);
//        assertEquals(1, itemRepositoryWithoutDb.getItems(1L).size());
//
//        itemRepositoryWithoutDb.deleteItem(1L);
//
//        assertTrue(itemRepositoryWithoutDb.getItems(1L).isEmpty());
//    }
//
//    @Test
//    public void checkSearchItemByTextIfTextIsNull() {
//        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
//        userRepositoryWithoutBd.addUser(testedUserDto);
//        User owner = UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(1));
//        ItemDto testedItemDto = new ItemDto(0L, "Дрель", "Простая дрель", owner, true,
//                new ItemRequest());
//        itemRepositoryWithoutDb.addItem(testedItemDto, 1L);
//
//        assertTrue(itemRepositoryWithoutDb.searchItem(null).isEmpty());
//    }
//
//    @Test
//    public void checkSearchItemByTextIfTextIsEmpty() {
//        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
//        userRepositoryWithoutBd.addUser(testedUserDto);
//        User owner = UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(1));
//        ItemDto testedItemDto = new ItemDto(0L, "Дрель", "Простая дрель", owner, true,
//                new ItemRequest());
//        itemRepositoryWithoutDb.addItem(testedItemDto, 1L);
//
//        assertTrue(itemRepositoryWithoutDb.searchItem("").isEmpty());
//    }
//
//    @Test
//    public void checkSearchItemByTextIfTextIsFine() {
//        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
//        userRepositoryWithoutBd.addUser(testedUserDto);
//        User owner = UserMapper.userDtoToUser(userRepositoryWithoutBd.getUser(1));
//        ItemDto testedItemDto = new ItemDto(0L, "Дрель", "Простая дрель", owner, true,
//                new ItemRequest());
//        itemRepositoryWithoutDb.addItem(testedItemDto, 1L);
//
//        assertEquals(itemRepositoryWithoutDb.searchItem("ДрЕлЬ"), itemRepositoryWithoutDb.getItems(1L));
//    }
//}