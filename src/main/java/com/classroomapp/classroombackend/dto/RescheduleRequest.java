package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    // JSON string cho schedule: { startTime, endTime, days[] }
    private String schedule;
    private Boolean autoAssignRoom = Boolean.TRUE;
    private Long preferRoomId; // optional
    private String propagateMode; // all | fromDate

    // Đổi lịch theo buổi (tùy chọn)
    private java.util.List<Long> selectedLessonIds; // nếu truyền, ưu tiên theo buổi
    private LocalDate targetDate; // ngày đích (khi đổi theo buổi)
    private String targetStartTime; // HH:mm (khi đổi theo buổi)
    private String targetEndTime;   // HH:mm (khi đổi theo buổi)

    // Nâng cao: cập nhật nhiều buổi mỗi buổi một thời gian khác nhau
    private java.util.List<LessonUpdateDto> lessonUpdates;
}


