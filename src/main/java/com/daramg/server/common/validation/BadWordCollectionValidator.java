package com.daramg.server.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public class BadWordCollectionValidator implements ConstraintValidator<NoBadWords, Collection<String>> {

    private BadWordFilter badWordFilter;

    public BadWordCollectionValidator() {
    }

    @Autowired
    public BadWordCollectionValidator(BadWordFilter badWordFilter) {
        this.badWordFilter = badWordFilter;
    }

    @Override
    public boolean isValid(Collection<String> values, ConstraintValidatorContext context) {
        if (badWordFilter == null || values == null || values.isEmpty()) {
            return true;
        }
        return values.stream().noneMatch(badWordFilter::containsBadWord);
    }
}
