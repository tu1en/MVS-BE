package com.classroomapp.classroombackend;

/**
 * Enum cho các loại notification của Schedule
 */
public enum ScheduleNotificationType {
    SCHEDULE_CREATED("Lịch làm việc đã được tạo"),
    SCHEDULE_UPDATED("Lịch làm việc đã được cập nhật"),
    SCHEDULE_PUBLISHED("Lịch làm việc đã được xuất bản"),
    SCHEDULE_ARCHIVED("Lịch làm việc đã được lưu trữ"),
    SCHEDULE_CANCELLED("Lịch làm việc đã được hủy"),
    SCHEDULE_ASSIGNED("Bạn đã được phân công vào lịch làm việc"),
    SCHEDULE_REMINDER("Nhắc nhở về lịch làm việc sắp tới");
    
    private final String description;
    
    ScheduleNotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}