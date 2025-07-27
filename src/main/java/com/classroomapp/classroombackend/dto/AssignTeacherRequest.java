package com.classroomapp.classroombackend.dto;

import com.classroomapp.classroombackend.entity.enumeration.TeacherRole;
import lombok.Data;

@Data
public class AssignTeacherRequest {
    private Long teacherId;   // ID của giáo viên
    private TeacherRole role; // MAIN_INSTRUCTOR hoặc ASSISTANT (mặc định: MAIN_INSTRUCTOR)
    private String notes;   // Ghi chú từ Manager
}