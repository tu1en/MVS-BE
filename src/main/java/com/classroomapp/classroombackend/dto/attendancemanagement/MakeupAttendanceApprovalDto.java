package com.classroomapp.classroombackend.dto.attendancemanagement;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for acknowledging a makeup attendance request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeupAttendanceApprovalDto {

    private boolean acknowledged;

    @Size(max = 1000, message = "Manager notes must not exceed 1000 characters")
    private String managerNotes;
}
