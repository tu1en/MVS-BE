package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.classroomapp.classroombackend.validation.MinAge;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
    private Long id;
    private Long userId;
    private String contractId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String contractType; // "TEACHER", "ACCOUNTANT"
    private String position;
    private String department;
    private Double salary;
    private String workingHours;
    private String status; // "ACTIVE", "EXPIRED", "TERMINATED"
    private String contractTerms;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String offer; // Thông tin offer từ Quản Lý Offer
    
    // --- OFFER MANAGEMENT FIELDS ---
    private String comments; // Changed from evaluation to comments (Nhận xét từ Quản lý Offer)
    private Long grossSalary; // Lương GROSS từ Quản lý Offer
    private Long netSalary; // Lương NET từ Quản lý Offer  
    @Min(20000)
    @Max(9999999)
    private Long hourlySalary; // Lương theo giờ từ Quản lý Offer

    // --- CUSTOM FIELDS FOR VIETNAMESE CONTRACT ---
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @MinAge(18)
    private LocalDate birthDate;
    private String citizenId; // Số CCCD
    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 3, max = 255, message = "Địa chỉ phải từ 3 đến 255 ký tự")
    @Pattern(
        regexp = "^(?=.{3,255}$)(?!.*\\b(?:test|xxx)\\b)(?=.*[A-Za-zÀ-ỹ]{3,})[a-zA-Z0-9À-ỹ\\s,\\.\\-/]+$",
        message = "Địa chỉ không hợp lệ: chỉ cho phép chữ, số, khoảng trắng, dấu phẩy, dấu chấm, gạch ngang và \"/\"; phải có ít nhất 1 từ ≥ 3 ký tự; không chứa từ cấm."
    )
    private String address;
    private String qualification;
    private String subject;
    private String classLevel; // Changed from educationLevel to classLevel (Lớp học)
    
    // --- NEW WORKING SCHEDULE FIELDS ---
    private String workSchedule; // Thời gian làm việc (combined schedule description)
    private String workShifts; // Ca làm việc (morning, afternoon, evening)
    private String workDays; // Ngày trong tuần (Monday, Tuesday, etc.)

    // --- CONTRACT DURATION FIELDS ---
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // Contract start date (persistent field)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // Contract expiry date (persistent field)
    
    // --- COMPUTED FIELD ---
    // Ngày bắt đầu hợp đồng (tự động tính theo buổi dạy đầu tiên của giáo viên)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;
}
