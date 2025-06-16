package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.AttendanceDto;
import com.classroomapp.classroombackend.dto.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance.AttendanceStatus;

public interface AttendanceService {
    
    // Attendance Session Management
    AttendanceSessionDto createAttendanceSession(Long classroomId, Long teacherId, String title, 
                                               String description, LocalDateTime startTime, 
                                               LocalDateTime endTime, Double latitude, 
                                               Double longitude, Double locationRadius);
    
    AttendanceSessionDto getAttendanceSessionById(Long sessionId);
    
    List<AttendanceSessionDto> getAttendanceSessionsByClassroom(Long classroomId);
    
    List<AttendanceSessionDto> getAttendanceSessionsByTeacher(Long teacherId);
    
    AttendanceSessionDto startAttendanceSession(Long sessionId);
    
    AttendanceSessionDto endAttendanceSession(Long sessionId);
    
    void deleteAttendanceSession(Long sessionId);
    
    // Attendance Marking
    AttendanceDto markAttendance(Long sessionId, Long userId, Double latitude, Double longitude);
    
    AttendanceDto updateAttendanceStatus(Long attendanceId, AttendanceStatus status);
    
    // Special attendance methods used by controller
    ApiResponse PerformCheckInLogic(String username, LocationDataDto locationData, String clientIpAddress);
    
    List<AttendanceDto> GetStudentsForAttendance(Long sessionId, Long teacherId);
    
    ApiResponse MarkStudentAttendance(AttendanceDto attendanceDto, Long teacherId);
    
    // Attendance Retrieval
    List<AttendanceDto> getAttendanceBySession(Long sessionId);
    
    List<AttendanceDto> getAttendanceByUser(Long userId);
    
    List<AttendanceDto> getAttendanceByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<AttendanceDto> getAttendanceByClassroomAndDateRange(Long classroomId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Attendance Reports and Analytics
    Map<AttendanceStatus, Long> getAttendanceSummaryBySession(Long sessionId);
    
    Map<Long, Double> getStudentAttendancePercentageByClassroom(Long classroomId);
    
    List<AttendanceDto> getAbsentStudents(Long sessionId);
    
    List<AttendanceDto> getLateStudents(Long sessionId);
    
    // Background Tasks
    void processScheduledSessions();
    
    void processActiveSessions();
    
    // Validation and Utility Methods
    boolean isUserInClassroom(Long userId, Long classroomId);
    
    boolean isWithinLocationRadius(Double userLat, Double userLon, Double sessionLat, Double sessionLon, Double radius);
    
    boolean canMarkAttendance(Long sessionId, Long userId);
}
