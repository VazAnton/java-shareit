package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, long id);

    UserDto getUser(long id);

    List<UserDto> getUsers();

    boolean deleteUser(long id);
}
