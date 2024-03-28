package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.DuplicateDataException;
import ru.practicum.shareit.exception.ObjectNotFoundException;

import java.util.Map;

@RestControllerAdvice("ru.practicum.shareit")
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> duplicateHandler(final DuplicateDataException de) {
        return Map.of("Ошибка при передаче данных", de.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFoundObjectHandler(final ObjectNotFoundException e) {
        return Map.of("Объект не найден.", e.getMessage());
    }
}
