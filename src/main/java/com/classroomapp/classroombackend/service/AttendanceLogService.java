package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceLogService {

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    public List<AttendanceLog> getTeacherAttendanceStatus(LocalDate date, String shift) {
        if (shift != null && !shift.isEmpty()) {
            return attendanceLogRepository.findByRoleAndDate("TEACHER", date);
        }
        return attendanceLogRepository.findByRoleAndDate("TEACHER", date);
    }

    public List<AttendanceLog> getDailyAttendanceByShift(LocalDate date, String shift) {
        return attendanceLogRepository.findByDateAndShift(date, shift);
    }

    public List<AttendanceLog> getAllStaffAttendanceLogs(LocalDate date) {
        return attendanceLogRepository.findByDate(date);
    }

    public List<AttendanceLog> getPersonalAttendanceHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        return attendanceLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public Page<AttendanceLog> getAttendanceLogsWithPagination(LocalDate date, int page, int size) {
        return attendanceLogRepository.findAll(PageRequest.of(page, size));
    }
}
