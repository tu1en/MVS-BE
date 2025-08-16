package com.classroomapp.classroombackend.dto.requestmanagement;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRequestDto {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 50, message = "Email không được quá 50 ký tự")
    private String email;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 50, message = "Họ và tên phải có từ 2 đến 50 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 11, message = "Số điện thoại phải có từ 10 đến 11 chữ số")
    private String phoneNumber;

    @NotBlank(message = "Vai trò không được để trống")
    private String requestedRole; // "TEACHER", "STUDENT", or "PARENT"

    private String formResponses; // Dữ liệu JSON từ form

    @Data
    public static class StudentFormData {
        @NotBlank(message = "Email phụ huynh không được để trống")
        @Email(message = "Email phụ huynh không hợp lệ")
        @Size(max = 50, message = "Email phụ huynh không được quá 50 ký tự")
        private String parentEmail;

        @NotBlank(message = "Họ và tên phụ huynh không được để trống")
        @Size(min = 2, max = 50, message = "Họ và tên phụ huynh phải có từ 2 đến 50 ký tự")
        private String parentFullName;

        @NotBlank(message = "Số điện thoại phụ huynh không được để trống")
        @Size(min = 10, max = 11, message = "Số điện thoại phụ huynh phải có từ 10 đến 11 chữ số")
        private String parentPhoneNumber;

        @Size(max = 200, message = "Thông tin thêm không được quá 200 ký tự")
        private String additionalInfo;
    }
} 