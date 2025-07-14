package com.classroomapp.classroombackend.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceRecordDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceResultDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateOrUpdateAttendanceDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MyAttendanceHistoryDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.StudentAttendanceDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.TeachingHistoryDto;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;

public interface AttendanceService {

    /**
     * Creates a new attendance session for a classroom.
     * Only a teacher of the classroom can perform this action.
     *
     * @param createDto DTO containing classroom ID and session duration.
     * @return The created AttendanceSession entity.
     */
    AttendanceSession createSession(CreateAttendanceSessionDto createDto);

    /**
     * Records a student's attendance for a specific session.
     *
     * @param sessionId   The ID of the attendance session.
     * @param studentCode A unique code from the student (e.g., from QR scan), can be extended later.
     * @return The created AttendanceRecord.
     */
    Attendance recordStudentAttendance(Long sessionId, String studentCode);

    /**
     * Closes an open attendance session.
     * Only a teacher of the classroom can perform this action.
     *
     * @param sessionId The ID of the session to close.
     * @return The updated AttendanceSession.
     */
    AttendanceSession closeSession(Long sessionId);

    /**
     * Retrieves the list of all attendance records for a specific session.
     * Only the teacher can view all records.
     *
     * @param sessionId The ID of the session.
     * @return A list of DTOs representing student attendance.
     */
    List<StudentAttendanceDto> getSessionAttendance(Long sessionId);

    /**
     * Calculates and retrieves the overall attendance result for a student in a classroom.
     * Can be accessed by the teacher or the student themselves.
     *
     * @param classroomId The ID of the classroom.
     * @param studentId   The ID of the student.
     * @return A DTO containing the attendance statistics.
     */
    AttendanceResultDto getAttendanceResult(Long classroomId, Long studentId);

    /**
     * Gets the currently active attendance session for a classroom. (New flow)
     * @param classroomId The ID of the classroom.
     * @return The active session DTO, or null if none exists.
     */
    AttendanceSessionDto getActiveSession(Long classroomId);

    /**
     * Marks attendance for a student. (New flow)
     * @param dto The DTO containing the session ID.
     * @param userDetails The security principal of the student.
     */
    void markAttendance(StudentAttendanceDto dto, UserDetails userDetails);

    /**
     * Gets the results of a specific attendance session. (New flow)
     * @param sessionId The ID of the session.
     * @return A list of all students in the class and their attendance status.
     */
    List<AttendanceResultDto> getSessionResults(Long sessionId);

    // --- Old/Existing Methods ---

    void createOrUpdateAttendance(CreateOrUpdateAttendanceDto dto);

    List<AttendanceRecordDto> getAttendanceForLecture(Long lectureId, Long classroomId);

    List<MyAttendanceHistoryDto> getMyAttendanceHistory(Long studentId, Long classroomId);
    
    /**
     * Lấy lịch sử giảng dạy của giáo viên
     * @param teacherId ID của giáo viên
     * @return Danh sách các buổi học đã được ghi nhận giảng dạy
     */
    List<TeachingHistoryDto> getTeachingHistory(Long teacherId);
    
    /**
     * Find attendance records by user ID
     * @param userId The ID of the user
     * @return List of attendance records
     */
    List<AttendanceDto> findByUserId(Long userId);
    
    /**
     * Submit attendance records for a lecture
     * @param submitDto DTO containing attendance data to submit
     */
    void submitAttendance(AttendanceSubmitDto submitDto);
}