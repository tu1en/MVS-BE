package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.StaffAttendanceLogDto;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;

/**
 * Service for managing staff attendance logs (different from student attendance)
 */
@Service
public class StaffAttendanceService {
    
    @Autowired
    private StaffAttendanceLogRepository staffAttendanceLogRepository;
    
    /**
     * Get all staff attendance logs for a specific date
     * @param date the attendance date
     * @return list of staff attendance logs as DTOs
     */
    public List<StaffAttendanceLogDto> getAllStaffAttendanceLogsByDate(LocalDate date) {
        List<StaffAttendanceLog> logs = staffAttendanceLogRepository.findByAttendanceDateOrderByUserIdAsc(date);
        
        return logs.stream().map(log -> {
            // Map StaffAttendanceLog to DTO format expected by frontend
            StaffAttendanceLogDto dto = new StaffAttendanceLogDto();
            dto.setId(log.getId());
            dto.setUserId(log.getUser().getId());
            dto.setUserName(log.getUser().getFullName());
            dto.setRole(getUserRole(log.getUser().getRoleId()));
            dto.setDepartment(getUserDepartment(log.getUser()));
            dto.setDate(log.getAttendanceDate());
            dto.setShift("Ca chính"); // Default shift
            dto.setCheckIn(log.getCheckInTime());
            dto.setCheckOut(log.getCheckOutTime());
            dto.setStatus(determineAttendanceStatus(log.getCheckInTime(), log.getCheckOutTime()));
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Determine attendance status based on check-in/out times
     */
    private String determineAttendanceStatus(java.time.LocalTime checkIn, java.time.LocalTime checkOut) {
        if (checkIn == null) {
            return "ABSENT";
        }
        
        // Consider late if check-in is after 8:30 AM
        java.time.LocalTime lateThreshold = java.time.LocalTime.of(8, 30);
        if (checkIn.isAfter(lateThreshold)) {
            return "LATE";
        }
        
        return "PRESENT";
    }
    
    /**
     * Get user role name based on role ID
     */
    private String getUserRole(Integer roleId) {
        if (roleId == null) return "STAFF";
        
        switch (roleId) {
            case 1: return "STUDENT";
            case 2: return "TEACHER";
            case 3: return "MANAGER";
            case 4: return "ACCOUNTANT";
            default: return "STAFF";
        }
    }
    
    /**
     * Get user department from User entity
     */
    private String getUserDepartment(com.classroomapp.classroombackend.model.usermanagement.User user) {
        return user.getDepartment() != null ? user.getDepartment() : "Không xác định";
    }
}
