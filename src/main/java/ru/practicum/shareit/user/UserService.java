package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User addUser(UserDto userDto);

    User updateUser(UserDto userDto, long id);

    Optional<User> getUser(long id);

    List<User> getUsers();

    void deleteUser(long id);
}
