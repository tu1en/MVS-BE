package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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

    // New methods for enhanced reporting
    public List<AttendanceLog> getAttendanceLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceLogRepository.findByDateBetween(startDate, endDate);
    }

    public List<AttendanceLog> getAttendanceLogsByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return attendanceLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public List<AttendanceLog> getAttendanceLogsByDepartmentAndDateRange(String department, LocalDate startDate, LocalDate endDate) {
        return attendanceLogRepository.findByDepartmentAndDateBetween(department, startDate, endDate);
    }

    public Map<String, Object> getAttendanceStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<AttendanceLog> logs = getAttendanceLogsByDateRange(startDate, endDate);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", logs.size());
        stats.put("presentCount", logs.stream().filter(log -> "PRESENT".equals(log.getStatus())).count());
        stats.put("absentCount", logs.stream().filter(log -> "ABSENT".equals(log.getStatus())).count());
        stats.put("lateCount", logs.stream().filter(log -> "LATE".equals(log.getStatus())).count());
        
        // Group by department
        Map<String, Long> departmentStats = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getDepartment() != null ? log.getDepartment() : "Chưa phân loại",
                Collectors.counting()
            ));
        stats.put("departmentBreakdown", departmentStats);
        
        return stats;
    }
}
