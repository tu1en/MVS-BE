package com.classroomapp.classroombackend.service;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;

public interface AttendanceSessionService {
    ResponseEntity<AttendanceSessionDto> createSession(CreateAttendanceSessionDto createDto, Principal principal);
    ResponseEntity<List<AttendanceSessionDto>> getSessionsForClassroom(Long classroomId, Principal principal);
    ResponseEntity<AttendanceSessionDto> getSessionDetails(Long sessionId, Principal principal);
    ResponseEntity<?> closeSession(Long sessionId, Principal principal);
} 