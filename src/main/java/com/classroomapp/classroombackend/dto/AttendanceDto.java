package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

<<<<<<< HEAD
import lombok.AllArgsConstructor;
=======
import com.classroomapp.classroombackend.model.Attendance.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
>>>>>>> master
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
<<<<<<< HEAD
=======
@Builder
>>>>>>> master
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private Long userId;
    private String userName;
<<<<<<< HEAD
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
=======
    private Long sessionId;
    private AttendanceStatus status;
    private LocalDateTime markedAt;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
>>>>>>> master
