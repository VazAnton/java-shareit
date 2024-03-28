package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Новый пользователь успешно создан!");
        return userRepository.addUser(userDto);
    }

    @Override
    public UserDto updateUser(UserDto userDto, long id) {
        log.info("Информация о пользователе " + id + " успешно обновлена!");
        return userRepository.updateUser(userDto, id);
    }

    @Override
    public UserDto getUser(long id) {
        log.info("Информация о пользователе " + id + " успешно получена!");
        return userRepository.getUser(id);
    }

    @Override
    public List<UserDto> getUsers() {
        log.info("Успешно получена информация о всех сохранённых пользователях!");
        return userRepository.getUsers();
    }

    @Override
    public boolean deleteUser(long id) {
        log.info("Информация о пользователе " + id + " успешно удалена!");
        return userRepository.deleteUser(id);
    }
}
