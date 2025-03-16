package com.lootopia.lootopia_app.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return email != null && PATTERN.matcher(email).matches();
    }
}
