package com.classroomapp.classroombackend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for sending attendance notification data to n8n webhook for Zalo messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloNotificationDto {
    
    /**
     * Type of notification
     */
    private String notificationType; // "ATTENDANCE_REPORT"
    
    /**
     * Classroom information
     */
    private ClassroomInfo classroom;
    
    /**
     * Attendance session information
     */
    private AttendanceSessionInfo session;
    
    /**
     * List of student attendance records with parent contact info
     */
    private List<StudentAttendanceNotification> studentNotifications;
    
    /**
     * Teacher information
     */
    private TeacherInfo teacher;
    
    /**
     * Timestamp when notification was created
     */
    private LocalDateTime timestamp;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomInfo {
        private Long id;
        private String name;
        private String code;
        private String subject;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceSessionInfo {
        private Long sessionId;
        private LocalDate sessionDate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String sessionType;
        private Long lectureId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentAttendanceNotification {
        private StudentInfo student;
        private AttendanceInfo attendance;
        private List<ParentContactInfo> parents;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String name;
        private String studentCode;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceInfo {
        private String status; // PRESENT, ABSENT, LATE, EXCUSED
        private String note;
        private LocalDateTime markedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentContactInfo {
        private Long parentId;
        private String parentName;
        private String phoneNumber; // Will be used as Zalo phone number
        private String email;
        private String relationType; // FATHER, MOTHER, GUARDIAN
        private Boolean isPrimary;
        private Boolean notificationEnabled; // Check if parent wants to receive notifications
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherInfo {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;
    }
}
