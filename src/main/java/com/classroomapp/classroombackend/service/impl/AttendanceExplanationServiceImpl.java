package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceExplanationService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceExplanationServiceImpl implements AttendanceExplanationService {

    @Autowired
    private AttendanceExplanationRepository repository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public AttendanceExplanation submitExplanation(AttendanceExplanation explanation) {
        explanation.setSubmittedAt(LocalDateTime.now());
        explanation.setStatus(ExplanationStatus.PENDING);
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

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Submitter", "Absence Date", "Reason", "Submitted At", "Status", "Approver", "Department"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Create data rows
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (AttendanceExplanation exp : explanations) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(exp.getId());
                row.createCell(1).setCellValue(exp.getSubmitterName());
                row.createCell(2).setCellValue(exp.getAbsenceDate().format(dateFormatter));
                row.createCell(3).setCellValue(exp.getReason());
                row.createCell(4).setCellValue(exp.getSubmittedAt().format(dateTimeFormatter));
                row.createCell(5).setCellValue(exp.getStatus().toString());
                row.createCell(6).setCellValue(exp.getApproverName() != null ? exp.getApproverName() : "N/A");
                row.createCell(7).setCellValue(exp.getDepartment() != null ? exp.getDepartment() : "N/A");
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

    @Override
    public void clearAllExplanations() {
        repository.deleteAll();
    }
    
    @Override
    public void createTestData() {
        // Tạo dữ liệu test cho AttendanceExplanation
        String[] staffEmails = {
            "teacher@test.com", "math@test.com", "literature@test.com", "english@test.com",
            "teacher2@test.com", "teacher3@test.com", "accountant@test.com", "manager@test.com"
        };
        
        List<User> staffUsers = new ArrayList<>();
        for (String email : staffEmails) {
            userRepository.findByEmail(email).ifPresent(staffUsers::add);
        }
        
        if (staffUsers.isEmpty()) {
            // Fallback: tạo dữ liệu mà không có staff assignment (sẽ lỗi nhưng ít ra có thông báo)
            return;
        }
        
        List<AttendanceExplanation> testExplanations = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(30);
        
        for (int i = 1; i <= 10; i++) {
            AttendanceExplanation explanation = new AttendanceExplanation();
            
            // Assign a staff user
            User staff = staffUsers.get((i - 1) % staffUsers.size());
            explanation.setStaff(staff);
            explanation.setSubmitterName(staff.getFullName());
            
            explanation.setDepartment(i % 2 == 0 ? "IT" : "Marketing");
            explanation.setAbsenceDate(baseTime.plusDays(i * 2).toLocalDate());
            explanation.setReason(i % 3 == 0 ? "Ốm" : (i % 3 == 1 ? "Việc gia đình" : "Công tác"));
            explanation.setExplanationText("Giải trình chi tiết cho việc vắng mặt ngày " + explanation.getAbsenceDate() + 
                ". Nhân viên: " + staff.getFullName());
            explanation.setSubmittedAt(baseTime.plusDays(i * 2 + 1));
            explanation.setStatus(ExplanationStatus.values()[i % 3]); // PENDING, APPROVED, REJECTED
            testExplanations.add(explanation);
        }
        
        repository.saveAll(testExplanations);
    }
}
