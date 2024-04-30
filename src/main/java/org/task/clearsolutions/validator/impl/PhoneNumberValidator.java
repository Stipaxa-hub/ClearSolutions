package org.task.clearsolutions.validator.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.task.clearsolutions.validator.Phone;

public class PhoneNumberValidator implements ConstraintValidator<Phone, String> {
    private static final String REGEX_FOR_PHONE_NUMBER = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
            + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
            + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";
    @Override
    public boolean isValid(String phoneNumber,
                           ConstraintValidatorContext constraintValidatorContext) {
        return phoneNumber != null && Pattern.compile(REGEX_FOR_PHONE_NUMBER).matcher(phoneNumber).matches();
    }
}
