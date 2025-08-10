package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for StaffAttendanceLog to match frontend expectations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffAttendanceLogDto {
    private Long id;
    private Long userId;
    private String userName;
    private String role;
    private String department;
    private LocalDate date;
    private String shift;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String status;
    
    /**
     * Constructor to map from StaffAttendanceLog entity
     */
    public StaffAttendanceLogDto(Long id, Long userId, String userName, String role, String department, 
                                LocalDate attendanceDate, LocalTime checkInTime, LocalTime checkOutTime) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.department = department;
        this.date = attendanceDate;
        this.shift = "Ca chính"; // Default shift
        this.checkIn = checkInTime;
        this.checkOut = checkOutTime;
        this.status = determineStatus(checkInTime, checkOutTime);
    }
    
    /**
     * Determine attendance status based on check-in/out times
     */
    private String determineStatus(LocalTime checkIn, LocalTime checkOut) {
        if (checkIn == null) {
            return "ABSENT";
        }
        
        // Consider late if check-in is after 8:30 AM
        LocalTime lateThreshold = LocalTime.of(8, 30);
        if (checkIn.isAfter(lateThreshold)) {
            return "LATE";
        }
        
        return "PRESENT";
    }
}
