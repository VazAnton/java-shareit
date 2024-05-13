package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;
    @Mock
    EntityMapper entityMapper;
    UserDto userDto;
    User user1;

    @BeforeEach
    public void setup() {
        userDto = new UserDto(
                1L,
                "user",
                "user@user.com");
        user1 = new User(1L, "user", "user@user.com");
    }

    @Test
    public void checkAddUser() {
        User userDtoToUser = new User(userDto.getId(), userDto.getName(), userDto.getEmail());
        when(entityMapper.userDtoToUser(userDto))
                .thenReturn(userDtoToUser);
        when(userRepository.save(entityMapper.userDtoToUser(userDto)))
                .thenReturn(user1);
        User result = userService.addUser(userDto);

        assertThat(result)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user1);
    }

    @Test
    public void checkUpdateUser() {
        User userDtoToUser = new User(userDto.getId(), userDto.getName(), userDto.getEmail());
        when(entityMapper.userDtoToUser(userDto))
                .thenReturn(userDtoToUser);
        when(userRepository.save(entityMapper.userDtoToUser(userDto)))
                .thenReturn(user1);
        when(userRepository.findById(userDto.getId()))
                .thenReturn(Optional.of(user1));
        when(userService.updateUser((userDto), userDto.getId()))
                .thenReturn(User.builder()
                        .id(user1.getId())
                        .name(userDto.getName())
                        .email(user1.getEmail())
                        .build());

        assertThat(userService.updateUser((userDto), userDto.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user1);
    }

    @Test
    public void updateUserShouldThrowExceptionIfUserNotExists() {
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(userDto, 100L));
    }

    @Test
    public void checkUpdateUserIfNameAndEmailIsNull() {
        User userDtoToUser = new User(userDto.getId(), userDto.getName(), userDto.getEmail());
        UserDto userDtoWithoutNameAndEmail = new UserDto(userDto.getId(), null, null);
        when(entityMapper.userDtoToUser(userDtoWithoutNameAndEmail))
                .thenReturn(userDtoToUser);
        when(userRepository.save(entityMapper.userDtoToUser(userDtoWithoutNameAndEmail)))
                .thenReturn(user1);
        when(userRepository.findById(userDto.getId()))
                .thenReturn(Optional.of(user1));
        when(userService.updateUser((userDtoWithoutNameAndEmail), userDto.getId()))
                .thenReturn(User.builder()
                        .id(user1.getId())
                        .name(userDto.getName())
                        .email(user1.getEmail())
                        .build());

        assertThat(userService.updateUser((userDto), userDto.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user1);
    }

    @Test
    public void checkGetUser() {
        when(userRepository.existsById(user1.getId()))
                .thenReturn(true);
        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(userService.getUser(user1.getId()))
                .thenReturn(Optional.of(user1));

        assertThat(userService.getUser(user1.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(user1));
    }

    @Test
    public void checkGetUserIfUserNotExists() {

        assertThrows(EntityNotFoundException.class, () -> userService.getUser(100L));

    }

    @Test
    public void checkGetUsers() {
        User user2 = new User(2L, "user2", "user@another.com");
        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));
        when(userService.getUsers())
                .thenReturn(List.of(user1, user2));

        assertThat(userService.getUsers())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(user1, user2));
    }

    @Test
    public void checkDeleteUser() {
        userService.deleteUser(user1.getId());
    }
}
