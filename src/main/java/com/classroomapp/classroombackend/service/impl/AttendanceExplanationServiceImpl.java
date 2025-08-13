package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceExplanationService;
import com.classroomapp.classroombackend.util.SecurityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceExplanationServiceImpl implements AttendanceExplanationService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceExplanationServiceImpl.class);

    @Autowired
    private AttendanceExplanationRepository repository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public AttendanceExplanation submitExplanation(AttendanceExplanation explanation) {
        // Basic validation to avoid 500 on DB constraints
        if (explanation == null) {
            throw new IllegalArgumentException("Explanation payload is required");
        }
        if (explanation.getAbsenceDate() == null) {
            throw new IllegalArgumentException("absenceDate is required");
        }
        if (explanation.getReason() == null || explanation.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("reason is required");
        }

        // Resolve current user and set staff/submitter information
        com.classroomapp.classroombackend.model.usermanagement.User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        explanation.setStaff(currentUser);
        if (explanation.getSubmitterName() == null || explanation.getSubmitterName().trim().isEmpty()) {
            explanation.setSubmitterName(currentUser.getFullName());
        }
        // Fill department if missing and available on user
        if ((explanation.getDepartment() == null || explanation.getDepartment().trim().isEmpty())) {
            try {
                String dept = currentUser.getDepartment();
                if (dept != null && !dept.isEmpty()) {
                    explanation.setDepartment(dept);
                }
            } catch (Exception ignore) {
                // In case User doesn't have department field in some envs
            }
        }

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
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date phải trước end date");
        }
        
        Page<AttendanceExplanation> explanations = repository.findByFilters(startDate, endDate, status, department, Pageable.unpaged());
        
        if (explanations.isEmpty()) {
            logger.warn("Không tìm thấy dữ liệu attendance explanation cho các tiêu chí đã cho");
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Attendance Explanations");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Submitter", "Absence Date", "Reason", "Submitted At", "Status", "Approver", "Department"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setWrapText(true);

            // Create data rows
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (AttendanceExplanation exp : explanations) {
                try {
                    Row row = sheet.createRow(rowNum++);
                    
                    Cell idCell = row.createCell(0);
                    idCell.setCellValue(exp.getId() != null ? exp.getId() : 0);
                    idCell.setCellStyle(dataStyle);
                    
                    Cell submitterCell = row.createCell(1);
                    submitterCell.setCellValue(exp.getSubmitterName() != null ? exp.getSubmitterName() : "N/A");
                    submitterCell.setCellStyle(dataStyle);
                    
                    Cell dateCell = row.createCell(2);
                    dateCell.setCellValue(exp.getAbsenceDate() != null ? exp.getAbsenceDate().format(dateFormatter) : "");
                    dateCell.setCellStyle(dataStyle);
                    
                    Cell reasonCell = row.createCell(3);
                    String reason = exp.getReason() != null ? exp.getReason() : "";
                    if (reason.length() > 500) {
                        reason = reason.substring(0, 500) + "...";
                    }
                    reasonCell.setCellValue(reason);
                    reasonCell.setCellStyle(dataStyle);
                    
                    Cell submittedAtCell = row.createCell(4);
                    submittedAtCell.setCellValue(exp.getSubmittedAt() != null ? exp.getSubmittedAt().format(dateTimeFormatter) : "");
                    submittedAtCell.setCellStyle(dataStyle);
                    
                    Cell statusCell = row.createCell(5);
                    statusCell.setCellValue(exp.getStatus() != null ? exp.getStatus().toString() : "Unknown");
                    statusCell.setCellStyle(dataStyle);
                    
                    Cell approverCell = row.createCell(6);
                    approverCell.setCellValue(exp.getApproverName() != null ? exp.getApproverName() : "N/A");
                    approverCell.setCellStyle(dataStyle);
                    
                    Cell departmentCell = row.createCell(7);
                    departmentCell.setCellValue(exp.getDepartment() != null ? exp.getDepartment() : "N/A");
                    departmentCell.setCellStyle(dataStyle);
                    
                } catch (Exception e) {
                    logger.warn("Lỗi xử lý attendance explanation ID {}: {}", exp.getId(), e.getMessage());
                }
            }

            // Auto-size columns with limits
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
                if (currentWidth < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
            }

            // Set row heights for better readability
            for (int i = 1; i <= explanations.getContent().size(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    row.setHeightInPoints(25);
                }
            }

            workbook.write(out);
            byte[] result = out.toByteArray();
            
            logger.info("Thành công export {} attendance explanations, kích thước file: {} bytes", 
                       explanations.getContent().size(), result.length);
            
            return result;
            
        } catch (IOException e) {
            logger.error("Lỗi tạo file Excel export: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo file Excel: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Lỗi không mong muốn khi export attendance explanations: {}", e.getMessage());
            throw new RuntimeException("Lỗi export Excel: " + e.getMessage(), e);
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
