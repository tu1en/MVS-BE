package com.classroomapp.classroombackend.service.impl;

import java.security.Principal;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceSessionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceSessionServiceImpl implements AttendanceSessionService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Override
    public ResponseEntity<AttendanceSessionDto> createSession(CreateAttendanceSessionDto createDto, Principal principal) {
        return null;
    }

    @Override
    public ResponseEntity<List<AttendanceSessionDto>> getSessionsForClassroom(Long classroomId, Principal principal) {
        return null;
    }

    @Override
    public ResponseEntity<AttendanceSessionDto> getSessionDetails(Long sessionId, Principal principal) {
        return null;
    }

    @Override
    public ResponseEntity<?> closeSession(Long sessionId, Principal principal) {
        return null;
    }
} 