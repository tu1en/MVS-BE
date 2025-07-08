package com.classroomapp.classroombackend.controller;

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
import com.classroomapp.classroombackend.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance-sessions")
@RequiredArgsConstructor
public class AttendanceSessionController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceSessionDto> createSession(@RequestBody CreateAttendanceSessionDto dto) {
        // Implementation will be in AttendanceService
        // AttendanceSessionDto createdSession = attendanceService.createSession(dto);
        // return ResponseEntity.ok(createdSession);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/classroom/{classroomId}/active")
    @PreAuthorize("@classroomSecurityService.isMember(#classroomId, principal)")
    public ResponseEntity<AttendanceSessionDto> getActiveSession(@PathVariable Long classroomId) {
        // Implementation will be in AttendanceService
        // AttendanceSessionDto activeSession = attendanceService.getActiveSession(classroomId);
        // return ResponseEntity.ok(activeSession);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AttendanceResultDto>> getSessionResults(@PathVariable Long sessionId) {
        // Implementation will be in AttendanceService
        // List<AttendanceResultDto> results = attendanceService.getSessionResults(sessionId);
        // return ResponseEntity.ok(results);
        throw new UnsupportedOperationException("Not implemented yet");
    }
} 