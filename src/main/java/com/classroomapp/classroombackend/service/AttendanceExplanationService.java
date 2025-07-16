package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface AttendanceExplanationService {

    AttendanceExplanation submitExplanation(AttendanceExplanation explanation);

    Page<AttendanceExplanation> getReports(LocalDate startDate, LocalDate endDate, ExplanationStatus status, String department, Pageable pageable);

    AttendanceExplanation approveExplanation(Long id, String approverName);

    AttendanceExplanation rejectExplanation(Long id, String approverName);

    Map<String, Long> getReasonStatistics(LocalDate startDate, LocalDate endDate);

    Map<String, Long> getStatusStatistics(LocalDate startDate, LocalDate endDate);

    byte[] exportExcel(LocalDate startDate, LocalDate endDate, ExplanationStatus status, String department);
}
