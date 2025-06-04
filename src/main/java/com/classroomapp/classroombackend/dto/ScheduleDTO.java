package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Long id;
    private String classId;
    private String className;
    private String subject;
    private Integer day;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long teacherId;
    private String teacherName;
    private Set<Long> studentIds;
    private String materialsUrl;
    private String meetUrl;
    private String room;
}
