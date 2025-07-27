package com.classroomapp.classroombackend.service.impl;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.service.AttendanceExplanationService;

@Service
public class AttendanceExplanationServiceImpl implements AttendanceExplanationService {

    @Autowired
    private AttendanceExplanationRepository repository;

    @Override
    public AttendanceExplanation submitExplanation(AttendanceExplanation explanation) {
        explanation.setSubmittedAt(LocalDateTime.now());
        explanation.setStatus(ExplanationStatus.PENDING);
        
        // ✅ Ensure explanationText is not null
        if (explanation.getExplanationText() == null || explanation.getExplanationText().isEmpty()) {
            explanation.setExplanationText(explanation.getReason());
        }
        
        // ✅ Ensure violationId is set
        if (explanation.getViolationId() == null) {
            explanation.setViolationId(1L);
        }
        
        return repository.save(explanation);
    }

    @Override
    public Page<AttendanceExplanation> getReports(LocalDate startDate, LocalDate endDate, ExplanationStatus status, String department, Pageable pageable) {
        return repository.findByFilters(startDate, endDate, status, department, pageable);
    }

    @Override
    public AttendanceExplanation approveExplanation(Long id, String approverName) {
        AttendanceExplanation explanation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Explanation not found with id: " + id));
        explanation.setStatus(ExplanationStatus.APPROVED);
        explanation.setApproverName(approverName);
        return repository.save(explanation);
    }

    @Override
    public AttendanceExplanation rejectExplanation(Long id, String approverName) {
        AttendanceExplanation explanation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Explanation not found with id: " + id));
        explanation.setStatus(ExplanationStatus.REJECTED);
        explanation.setApproverName(approverName);
        return repository.save(explanation);
    }

    @Override
    public Map<String, Long> getReasonStatistics(LocalDate startDate, LocalDate endDate) {
        Object[][] results = repository.countByReason(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put((String) result[0], (Long) result[1]);
        }
        return statistics;
    }

    @Override
    public Map<String, Long> getStatusStatistics(LocalDate startDate, LocalDate endDate) {
        Object[][] results = repository.countByStatus(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put(result[0].toString(), (Long) result[1]);
        }
        return statistics;
    }

    @Override
    public byte[] exportExcel(LocalDate startDate, LocalDate endDate, ExplanationStatus status, String department) {
        Page<AttendanceExplanation> explanations = repository.findByFilters(startDate, endDate, status, department, Pageable.unpaged());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Explanations");

            // Create header row - ✅ Updated to include explanation_text
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Submitter", "Absence Date", "Reason", "Explanation Text", "Submitted At", "Status", "Approver", "Department", "Violation ID"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Create data rows - ✅ Updated to include explanation_text
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (AttendanceExplanation exp : explanations) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(exp.getId());
                row.createCell(1).setCellValue(exp.getSubmitterName());
                row.createCell(2).setCellValue(exp.getAbsenceDate().format(dateFormatter));
                row.createCell(3).setCellValue(exp.getReason());
                row.createCell(4).setCellValue(exp.getExplanationText() != null ? exp.getExplanationText() : "N/A"); // ✅ New field
                row.createCell(5).setCellValue(exp.getSubmittedAt().format(dateTimeFormatter));
                row.createCell(6).setCellValue(exp.getStatus().toString());
                row.createCell(7).setCellValue(exp.getApproverName() != null ? exp.getApproverName() : "N/A");
                row.createCell(8).setCellValue(exp.getDepartment() != null ? exp.getDepartment() : "N/A");
                row.createCell(9).setCellValue(exp.getViolationId() != null ? exp.getViolationId().toString() : "1"); // ✅ New field
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel file", e);
        }
    }
}