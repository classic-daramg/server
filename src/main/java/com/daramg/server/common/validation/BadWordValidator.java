package com.daramg.server.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class BadWordValidator implements ConstraintValidator<NoBadWords, String> {

    private BadWordFilter badWordFilter;

    public BadWordValidator() {
    }

    @Autowired
    public BadWordValidator(BadWordFilter badWordFilter) {
        this.badWordFilter = badWordFilter;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (badWordFilter == null || value == null || value.isBlank()) {
            return true;
        }
        return !badWordFilter.containsBadWord(value);
    }
}
