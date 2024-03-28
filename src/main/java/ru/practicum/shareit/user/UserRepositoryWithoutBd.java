package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateDataException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryWithoutBd implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long userId = 1;

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto != null) {
            for (User savedUser : users.values()) {
                if (userDto.getEmail().equals(savedUser.getEmail())) {
                    throw new DuplicateDataException("Внимание! Пользователь с таким адресом электронной почты уже " +
                            "существует!");
                }
            }
            userDto.setId(userId);
            users.put(userId++, UserMapper.userDtoToUser(userDto));
        }
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto userDto, long id) {
        if (getUser(id) != null) {
            User user = UserMapper.userDtoToUser(userDto);
            user.setId(id);
            if (userDto.getName() == null) {
                user.setName(users.get(id).getName());
            } else {
                user.setName(userDto.getName());
            }
            if (userDto.getEmail() == null) {
                user.setEmail(users.get(id).getEmail());
            } else {
                List<User> anotherUsers = new ArrayList<>(users.values());
                anotherUsers.remove(users.get(id));
                for (User savedUser : anotherUsers) {
                    if (userDto.getEmail().equals(savedUser.getEmail())) {
                        throw new DuplicateDataException("Внимание! Пользователь с таким адресом электронной почты уже " +
                                "существует!");
                    }
                }
                user.setEmail(userDto.getEmail());
            }
            users.put(id, user);
            return getUser(id);
        }
        return userDto;
    }

    @Override
    public UserDto getUser(long id) {
        return UserMapper.userToUserDto(Optional.ofNullable(users.get(id)).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!")));
    }

    @Override
    public List<UserDto> getUsers() {
        return users.values().stream()
                .map(UserMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteUser(long id) {
        users.remove(id);
        return true;
    }
}
