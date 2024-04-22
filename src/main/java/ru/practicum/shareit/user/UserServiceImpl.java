package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.mappers.EntityMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Transactional
    @Override
    public User addUser(UserDto userDto) {
        log.info("Новый пользователь успешно создан!");
        return userRepository.save(entityMapper.userDtoToUser(userDto));
    }

    @Transactional
    @Override
    public User updateUser(UserDto userDto, long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!"));
        if (userDto.getName() == null) {
            userDto.setName(user.getName());
        }
        if (userDto.getEmail() == null) {
            userDto.setEmail(user.getEmail());
        }
        log.info("Информация о пользователе " + id + " успешно обновлена!");
        userDto.setId(id);
        return userRepository.save(entityMapper.userDtoToUser(userDto));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> getUser(long id) {
        if (userRepository.existsById(id)) {
            log.info("Информация о пользователе " + id + " успешно получена!");
            return userRepository.findById(id);
        }
        throw new EntityNotFoundException("Внимание! Пользователя с таким номером не существует!");
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> getUsers() {
        log.info("Успешно получена информация о всех сохранённых пользователях!");
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteUser(long id) {
        log.info("Информация о пользователе " + id + " успешно удалена!");
        userRepository.deleteById(id);
    }
}
