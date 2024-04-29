package ru.practicum.shareit.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = MyBookingDateValidator.class)
public @interface BookingDateConstraint {

    String message() default "Не верна указана дата начала или конца бронирования!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
