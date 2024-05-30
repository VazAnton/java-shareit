package ru.practicum.shareit.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class MyBookingDateValidator implements ConstraintValidator<BookingDateConstraint, LocalDateTime> {

    @Override
    public void initialize(BookingDateConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDateTime startOrEnd, ConstraintValidatorContext constraintValidatorContext) {
        return startOrEnd != null && !startOrEnd.isBefore(LocalDateTime.now());
    }
}

