package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.absencemanagement.AbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.CreateAbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.TeacherLeaveInfoDTO;

import java.util.List;

public interface AbsenceService {
    
    // Employee operations (Teacher + Accountant)
    AbsenceDTO createAbsenceRequest(CreateAbsenceDTO createDto, Long userId);
    List<AbsenceDTO> getMyAbsenceRequests(Long userId);
    AbsenceDTO getAbsenceById(Long absenceId, Long userId);
    
    // Manager operations
    List<AbsenceDTO> getAllAbsenceRequests();
    List<AbsenceDTO> getPendingAbsenceRequests();
    List<TeacherLeaveInfoDTO> getAllTeachersLeaveInfo(); // Trả về cả Teacher và Accountant
    TeacherLeaveInfoDTO getTeacherLeaveInfo(Long employeeId); // Trả về info cho Teacher hoặc Accountant
    
    // Approval operations
    AbsenceDTO approveAbsence(Long absenceId, Long managerId);
    AbsenceDTO rejectAbsence(Long absenceId, String reason, Long managerId);
    
    // Utility operations
    void resetAnnualLeave(); // Scheduled task to reset leave for all teachers
    void resetUserAnnualLeave(Long userId); // Reset leave for specific user
} 