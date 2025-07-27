package com.classroomapp.classroombackend.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Enum validation annotation for validating enum values
 * Usage: @EnumValidator(enumClass = Priority.class, message = "Invalid priority")
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidatorImpl.class)
@Documented
public @interface EnumValidator {
    
    /**
     * The enum class to validate against
     */
    Class<? extends Enum<?>> enumClass();
    
    /**
     * Error message
     */
    String message() default "Invalid enum value";
    
    /**
     * Whether to ignore case when validating
     */
    boolean ignoreCase() default true;
    
    /**
     * Whether null values are allowed
     */
    boolean allowNull() default true;
    
    /**
     * Validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload
     */
    Class<? extends Payload>[] payload() default {};
}
