package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.absencemanagement.AbsenceDto;
import com.classroomapp.classroombackend.dto.absencemanagement.CreateAbsenceDto;
import com.classroomapp.classroombackend.dto.absencemanagement.TeacherLeaveInfoDto;

import java.util.List;

public interface AbsenceService {
    
    // Employee operations (Teacher + Accountant)
    AbsenceDto createAbsenceRequest(CreateAbsenceDto createDto, Long userId);
    List<AbsenceDto> getMyAbsenceRequests(Long userId);
    AbsenceDto getAbsenceById(Long absenceId, Long userId);
    
    // Manager operations
    List<AbsenceDto> getAllAbsenceRequests();
    List<AbsenceDto> getPendingAbsenceRequests();
    List<TeacherLeaveInfoDto> getAllTeachersLeaveInfo(); // Tráº£ vá» cáº£ Teacher vÃ  Accountant
    TeacherLeaveInfoDto getTeacherLeaveInfo(Long employeeId); // Tráº£ vá» info cho Teacher hoáº·c Accountant
    
    // Approval operations
    AbsenceDto approveAbsence(Long absenceId, Long managerId);
    AbsenceDto rejectAbsence(Long absenceId, String reason, Long managerId);
    
    // Utility operations
    void resetAnnualLeave(); // Scheduled task to reset leave for all teachers
    void resetUserAnnualLeave(Long userId); // Reset leave for specific user
} 
