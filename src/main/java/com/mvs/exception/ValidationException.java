package com.mvs.exception;

import lombok.Getter;
import org.springframework.validation.FieldError;
import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<FieldError> fieldErrors;

    public ValidationException(List<FieldError> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }
}
