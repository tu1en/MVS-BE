package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Yêu cầu tìm giáo viên khả dụng theo môn và lịch học
 */
@Data
public class AvailableTeachersRequest {
    /**
     * Môn học (khớp với trường subject của `CourseTemplate` hoặc `User.department`)
     */
    private String subject;

    /**
     * Lịch học dạng JSON do FE gửi lên, cùng format với trường `schedule_json` của lớp
     * Ví dụ: {"days":["MON","WED"],"startTime":"18:00","endTime":"20:00"}
     */
    @NotNull
    private String schedule;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}


