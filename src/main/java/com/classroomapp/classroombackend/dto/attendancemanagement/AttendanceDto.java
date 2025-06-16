package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userFullName;
    private String userPhotoUrl;
    private Long classroomId;
    private String classroomName;
    private LocalDateTime sessionDate;
    private boolean isPresent;
    private String attendanceType;
    private String comment;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String ipAddress;
    private Long markedById;
    private String markedByName;
    private LocalDateTime createdAt;
    private boolean isTeacherRecord;
}
