package com.classroomapp.classroombackend;

import com.classroomapp.classroombackend.dto.RegisterDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RegisterDto (create user DTO).
 *
 * This test uses concrete inputs (like the TopCVCalculation tests) instead of reflection,
 * so it will actually run against the real DTO in your codebase.
 */
public class CreateUserDtoTest {

    /**
     * Build a Jakarta Validator for bean validation testing.
     */
    private Validator buildValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    @Test
    @DisplayName("Valid RegisterDto passes validation")
    void testValidRegisterDto() {
        // Arrange: a fully valid DTO instance
        RegisterDto dto = new RegisterDto();
        dto.setUsername("valid_user");            // >=3 and <=50 chars
        dto.setPassword("secret1");               // >=6 chars
        dto.setEmail("user@example.com");         // valid email format
        dto.setFullName("Nguyen Van A");          // not blank
        dto.setRoleId(2);                          // not null

        // Act
        Validator validator = buildValidator();
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);

        // Assert: no violations for valid input
        assertTrue(violations.isEmpty(), "Valid RegisterDto should have no validation errors");
    }

    @Test
    @DisplayName("Blank and null fields fail validation")
    void testBlankAndNullFields() {
        // Arrange: set blank strings and null roleId to trigger @NotBlank/@NotNull
        RegisterDto dto = new RegisterDto();
        dto.setUsername("");           // @NotBlank
        dto.setPassword("");           // @NotBlank and @Size(min=6)
        dto.setEmail("");              // @NotBlank and @Email
        dto.setFullName("");           // @NotBlank
        dto.setRoleId(null);            // @NotNull

        // Act
        Validator validator = buildValidator();
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);

        // Assert: expect at least one violation per invalid property
        assertFalse(violations.isEmpty(), "Blank/null fields must trigger validation errors");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("roleId")));
    }

    @Test
    @DisplayName("Invalid email and short password fail validation")
    void testInvalidEmailAndShortPassword() {
        // Arrange: invalid email and password shorter than 6
        RegisterDto dto = new RegisterDto();
        dto.setUsername("valid_user");
        dto.setPassword("123");                   // too short
        dto.setEmail("invalid-email");            // not an email
        dto.setFullName("Valid Name");
        dto.setRoleId(1);

        // Act
        Validator validator = buildValidator();
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);

        // Assert: expect violations on email and password
        assertFalse(violations.isEmpty(), "Invalid email and short password should fail validation");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Username length boundaries are enforced")
    void testUsernameLengthBoundaries() {
        Validator validator = buildValidator();

        // Case 1: too short (2 chars) -> should fail
        RegisterDto tooShort = new RegisterDto("ab", "secret1", "a@b.com", "Name", 1);
        Set<ConstraintViolation<RegisterDto>> v1 = validator.validate(tooShort);
        assertFalse(v1.isEmpty(), "Username with length 2 should fail validation");

        // Case 2: lower boundary (3 chars) -> should pass
        RegisterDto lowerOk = new RegisterDto("abc", "secret1", "a@b.com", "Name", 1);
        Set<ConstraintViolation<RegisterDto>> v2 = validator.validate(lowerOk);
        assertTrue(v2.isEmpty(), "Username with length 3 should be valid");

        // Case 3: upper boundary (50 chars) -> should pass
        String fifty = "x".repeat(50);
        RegisterDto upperOk = new RegisterDto(fifty, "secret1", "a@b.com", "Name", 1);
        Set<ConstraintViolation<RegisterDto>> v3 = validator.validate(upperOk);
        assertTrue(v3.isEmpty(), "Username with length 50 should be valid");

        // Case 4: too long (51 chars) -> should fail
        String fiftyOne = "x".repeat(51);
        RegisterDto tooLong = new RegisterDto(fiftyOne, "secret1", "a@b.com", "Name", 1);
        Set<ConstraintViolation<RegisterDto>> v4 = validator.validate(tooLong);
        assertFalse(v4.isEmpty(), "Username with length 51 should fail validation");
    }

    @Test
    @DisplayName("Lombok-generated toString is non-null")
    void testToStringNonNull() {
        // Arrange
        RegisterDto dto = new RegisterDto("user123", "secret1", "user@example.com", "Nguyen Van A", 2);

        // Act
        String str = String.valueOf(dto);

        // Assert
        assertNotNull(str);
        assertTrue(str.contains("user123"), "toString should include field values if Lombok @Data is applied");
    }
}
