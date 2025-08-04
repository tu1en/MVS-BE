package com.classroomapp.classroombackend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceResultDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.StudentAttendanceDto;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.service.AttendanceService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceResultDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance-sessions")
@RequiredArgsConstructor
public class AttendanceSessionController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceSessionDto> createSession(@RequestBody CreateAttendanceSessionDto dto) {
        AttendanceSession createdSession = attendanceService.createSession(dto);
        AttendanceSessionDto sessionDto = mapToDto(createdSession);
        return ResponseEntity.ok(sessionDto);
    }

    @GetMapping("/classroom/{classroomId}/active")
    @PreAuthorize("@classroomSecurityService.isMember(#classroomId, principal)")
    public ResponseEntity<AttendanceSessionDto> getActiveSession(@PathVariable Long classroomId) {
        AttendanceSessionDto activeSession = attendanceService.getActiveSession(classroomId);
        if (activeSession == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(activeSession);
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AttendanceResultDto>> getSessionResults(@PathVariable Long sessionId) {
        List<AttendanceResultDto> results = attendanceService.getSessionResults(sessionId);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{sessionId}/mark-attendance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> markAttendance(@PathVariable Long sessionId, Principal principal) {
        StudentAttendanceDto dto = new StudentAttendanceDto();
        dto.setSessionId(sessionId);
        attendanceService.markAttendance(dto, (UserDetails) ((Authentication) principal).getPrincipal());
        return ResponseEntity.ok("Attendance marked successfully");
    }

    @PostMapping("/{sessionId}/close")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceSessionDto> closeSession(@PathVariable Long sessionId) {
        AttendanceSession closedSession = attendanceService.closeSession(sessionId);
        AttendanceSessionDto sessionDto = mapToDto(closedSession);
        return ResponseEntity.ok(sessionDto);
    }

    private AttendanceSessionDto mapToDto(AttendanceSession session) {
        AttendanceSessionDto dto = new AttendanceSessionDto();
        dto.setId(session.getId());
        dto.setClassroomId(session.getClassroom().getId());
        dto.setClassroomName(session.getClassroom().getName());
        dto.setStartTime(session.getCreatedAt());
        dto.setEndTime(session.getExpiresAt());
        dto.setStatus(session.getIsOpen() ? "ACTIVE" : "CLOSED");
        dto.setActive(session.isActive());
        dto.setAutoMarkTeacherAttendance(session.isAutoMarkTeacherAttendance());
        dto.setCreatedAt(session.getCreatedAt());
        
        if (session.getClassroom().getTeacher() != null) {
            dto.setTeacherId(session.getClassroom().getTeacher().getId());
            dto.setTeacherName(session.getClassroom().getTeacher().getFullName());
        }
        
        if (session.getLecture() != null) {
            dto.setTitle(session.getLecture().getTitle());
        } else {
            dto.setTitle("Attendance Session");
        }
        
        return dto;
    }
} 