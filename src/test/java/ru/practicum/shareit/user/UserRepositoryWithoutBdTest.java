package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.DuplicateDataException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryWithoutBdTest {

    private UserRepositoryWithoutBd userRepositoryWithoutBd;

    @BeforeEach
    public void setUp() {
        userRepositoryWithoutBd = new UserRepositoryWithoutBd();
    }

    @Test
    public void checkAddUser() {
        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");

        userRepositoryWithoutBd.addUser(testedUserDto);

        assertNotNull(userRepositoryWithoutBd.getUser(1));
        assertEquals(1, userRepositoryWithoutBd.getUsers().size());
    }

    @Test
    public void checkUpdateUser() {
        UserDto testedUserDto = new UserDto(0L, "user", "user@user.com");
        userRepositoryWithoutBd.addUser(testedUserDto);
        UserDto newTestedUser = new UserDto(1L, "update",
                "update@user.com");

        assertEquals(userRepositoryWithoutBd.updateUser(newTestedUser, 1L), newTestedUser);
    }

    @Test
    public void updateUserShouldThrowExceptionIfEmailAlreadyExists() {
        UserDto testedUserDto1 = new UserDto(0L, "user", "user@user.com");
        userRepositoryWithoutBd.addUser(testedUserDto1);
        UserDto newTestedUser1 = new UserDto(1L, "update", "user@user.com");
        userRepositoryWithoutBd.updateUser(newTestedUser1, 1L);
        UserDto testedUserDto2 = new UserDto(0L, "user", "another@user.com");
        userRepositoryWithoutBd.addUser(testedUserDto2);
        UserDto newTestedUser2 = new UserDto(1L, "updateName", "user@user.com");

        assertThrows(DuplicateDataException.class, () -> userRepositoryWithoutBd.updateUser(newTestedUser2, 2L));
    }

    @Test
    public void getUserShouldTrowExceptionIfUserIdNotExists() {
        assertThrows(EntityNotFoundException.class, () -> userRepositoryWithoutBd.getUser(9));
    }

    @Test
    public void checkGetUsersIfUsersNotExists() {
        assertEquals(0, userRepositoryWithoutBd.getUsers().size());
    }

    @Test
    public void checkGetUsersIfUserIsNull() {
        userRepositoryWithoutBd.addUser(null);

        assertTrue(userRepositoryWithoutBd.getUsers().isEmpty());
    }

    @Test
    public void checkDeleteUser() {
        UserDto testedUserDto1 = new UserDto(0L, "user", "user@user.com");
        userRepositoryWithoutBd.addUser(testedUserDto1);
        assertEquals(1, userRepositoryWithoutBd.getUsers().size());

        userRepositoryWithoutBd.deleteUser(1L);

        assertTrue(userRepositoryWithoutBd.getUsers().isEmpty());
    }
}
