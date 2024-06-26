package org.task.clearsolutions.validator;

import jakarta.persistence.Table;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.task.clearsolutions.validator.impl.EmailValidator;

@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Email {
    String message() default "Invalid format email";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
