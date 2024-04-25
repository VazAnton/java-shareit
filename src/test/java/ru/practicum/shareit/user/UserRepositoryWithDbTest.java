//package ru.practicum.shareit.user;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
//import ru.practicum.shareit.mappers.EntityMapper;
//import ru.practicum.shareit.user.dto.UserDto;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@JdbcTest
//public class UserRepositoryWithDbTest {
//
//    private User testedUser = User.builder().build();
//    private UserDto testedUserDto = UserDto.builder().build();
//    private final UserRepository userRepository;
//    private final EntityMapper entityMapper;
//
//    @Autowired
//    public UserRepositoryWithDbTest(UserRepository userRepository, EntityMapper entityMapper) {
//        this.userRepository = userRepository;
//        this.entityMapper = entityMapper;
//    }
//
//    @Test
//    public void checkAddUser() {
//        UserService userService = new UserServiceImpl(userRepository, entityMapper);
//        testedUserDto = UserDto.builder()
//                .id(0L)
//                .name("user")
//                .email("user@user.com")
//                .build();
//
//        userService.addUser(testedUserDto);
//
//        assertEquals(1, userService.getUsers().size());
//    }
//
//    @Test
//    public void checkGetUserAfterSave() {
//        UserService userService = new UserServiceImpl(userRepository, entityMapper);
//        testedUserDto = UserDto.builder()
//                .id(0L)
//                .name("user")
//                .email("user@user.com")
//                .build();
//
//        User savedUser = userService.addUser(testedUserDto);
//
//        assertThat(savedUser)
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(userService.getUser(savedUser.getId()));
//    }
//
//    @Test
//    public void checkDelete() {
//        UserService userService = new UserServiceImpl(userRepository, entityMapper);
//        testedUserDto = UserDto.builder()
//                .id(0L)
//                .name("user")
//                .email("user@user.com")
//                .build();
//        User savedUser = userService.addUser(testedUserDto);
//        assertEquals(1, userService.getUsers().size());
//
//        userService.deleteUser(savedUser.getId());
//
//        assertEquals(0, userService.getUsers().size());
//    }
//}
