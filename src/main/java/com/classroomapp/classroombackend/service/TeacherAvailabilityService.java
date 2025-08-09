package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.AvailableTeacherDto;
import com.classroomapp.classroombackend.dto.AvailableTeachersRequest;

public interface TeacherAvailabilityService {
    List<AvailableTeacherDto> findAvailableTeachers(AvailableTeachersRequest request);
}


