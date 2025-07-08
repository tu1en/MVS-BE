package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để hiển thị lịch sử giảng dạy của giáo viên
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeachingHistoryDto {
    private Long lectureId;
    private String lectureTitle;
    private Long classroomId;
    private String classroomName;
    private LocalDate lectureDate;
    private LocalDateTime clockInTime;
} 