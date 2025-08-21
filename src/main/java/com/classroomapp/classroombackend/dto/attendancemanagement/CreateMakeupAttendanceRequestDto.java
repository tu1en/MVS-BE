package com.classroomapp.classroombackend.dto.attendancemanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new makeup attendance request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMakeupAttendanceRequestDto {
    
    @NotNull(message = "Lecture ID is required")
    private Long lectureId;
    
    @NotNull(message = "Classroom ID is required")
    private Long classroomId;
    
    @NotBlank(message = "Lý do điểm danh bù không được để trống")
    @Size(min = 10, max = 2000, message = "Lý do điểm danh bù phải có từ 10 đến 2000 ký tự")
    private String reason;
}
