package com.classroomapp.classroombackend.validation;

import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementation of EnumValidator constraint
 */
public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

    private Class<? extends Enum<?>> enumClass;
    private boolean ignoreCase;
    private boolean allowNull;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null values if configured
        if (value == null) {
            return allowNull;
        }

        // Get all enum values
        Enum<?>[] enumValues = enumClass.getEnumConstants();
        if (enumValues == null) {
            return false;
        }

        // Check if value matches any enum constant
        for (Enum<?> enumValue : enumValues) {
            String enumName = enumValue.name();
            if (ignoreCase ? enumName.equalsIgnoreCase(value) : enumName.equals(value)) {
                return true;
            }
        }

        // Build custom error message with valid values
        String validValues = Arrays.stream(enumValues)
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        // Disable default constraint violation
        context.disableDefaultConstraintViolation();
        
        // Add custom message
        context.buildConstraintViolationWithTemplate(
                String.format("Invalid value '%s'. Allowed values: [%s]", value, validValues)
        ).addConstraintViolation();

        return false;
    }
}
