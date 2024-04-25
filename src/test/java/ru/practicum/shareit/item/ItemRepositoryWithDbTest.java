//package ru.practicum.shareit.item;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
//import ru.practicum.shareit.booking.BookingRepository;
//import ru.practicum.shareit.item.dto.ItemDto;
//import ru.practicum.shareit.mappers.EntityMapper;
//import ru.practicum.shareit.mappers.EntityMapperImpl;
//import ru.practicum.shareit.user.User;
//import ru.practicum.shareit.user.UserRepository;
//import ru.practicum.shareit.user.UserService;
//import ru.practicum.shareit.user.UserServiceImpl;
//import ru.practicum.shareit.user.dto.UserDto;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.assertj.core.api.Assertions.assertThat;
//
//@JdbcTest
//public class ItemRepositoryWithDbTest {
//
//    Item testedItem = Item.builder().build();
//    ItemDto testedItemDto = ItemDto.builder().build();
//    UserDto testedUserDto = UserDto.builder().build();
//    private UserRepository userRepository;
//    private ItemRepository itemRepository;
//    private BookingRepository bookingRepository;
//    private CommentRepository commentRepository;
//    private EntityMapper entityMapper;
//
//    @Autowired
//    public ItemRepositoryWithDbTest(UserRepository userRepository, ItemRepository itemRepository,
//                                    BookingRepository bookingRepository, CommentRepository commentRepository,
//                                    EntityMapper entityMapper) {
//        this.userRepository = userRepository;
//        this.itemRepository = itemRepository;
//        this.bookingRepository = bookingRepository;
//        this.commentRepository = commentRepository;
//        this.entityMapper = entityMapper;
//    }
//
//    @Test
//    public void checkAddItemIfUserExists() {
//        UserService userService = new UserServiceImpl(userRepository, entityMapper);
//        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository,
//                commentRepository, entityMapper);
//        testedUserDto = UserDto.builder()
//                .id(0L)
//                .name("user")
//                .email("user@user.com")
//                .build();
//        User savedUser = userService.addUser(testedUserDto);
//        testedItemDto = ItemDto.builder()
//                .id(0L)
//                .name("Дрель")
//                .description("Простая дрель")
//                .available(true)
//                .build();
//
//        Item savedItem = itemService.addItem(testedItemDto, savedUser.getId());
//
//        assertEquals(1, userService.getUsers().size());
//        assertThat(savedItem)
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(itemService.getItem(savedItem.getId(), savedUser.getId()));
//    }
//}
