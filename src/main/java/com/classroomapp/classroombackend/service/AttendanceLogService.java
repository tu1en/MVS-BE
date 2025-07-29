package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;

@Service
public class AttendanceLogService {

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    public List<AttendanceLog> getTeacherAttendanceStatus(LocalDate date, String shift) {
        if (shift != null && !shift.isEmpty()) {
            return attendanceLogRepository.findByStaffRoleIdAndAttendanceDate(2, date); // 2 = TEACHER
        }
        return attendanceLogRepository.findByStaffRoleIdAndAttendanceDate(2, date); // 2 = TEACHER
    }

    public List<AttendanceLog> getDailyAttendanceByShift(LocalDate date, String shift) {
        return attendanceLogRepository.findByAttendanceDateAndShiftName(date, shift);
    }

    public List<AttendanceLog> getAllAttendanceLogs(LocalDate date) {
        return attendanceLogRepository.findByAttendanceDate(date);
    }

    public List<AttendanceLog> getPersonalAttendanceHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        return attendanceLogRepository.findByStaffIdAndAttendanceDateBetween(userId, startDate, endDate);
    }

    public Page<AttendanceLog> getAttendanceLogsWithPagination(LocalDate date, int page, int size) {
        return attendanceLogRepository.findAll(PageRequest.of(page, size));
    }
}
