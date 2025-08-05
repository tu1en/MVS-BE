package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.entity.LessonTemplate;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.repository.CourseTemplateRepository;
import com.classroomapp.classroombackend.repository.LessonTemplateRepository;
import com.classroomapp.classroombackend.repository.MaterialRepository;

@Service
public class ExcelImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);
    
    @Autowired
    private CourseTemplateRepository courseTemplateRepository;
    
    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;
    
    @Autowired
    private MaterialRepository materialRepository;
    
    /**
     * Parse Excel file for course templates
     */
    @Transactional
    public List<LessonData> parseExcelFile(MultipartFile file) throws IOException {
        List<LessonData> lessons = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                LessonData lesson = new LessonData();
                
                try {
                    // Week column
                    Cell weekCell = row.getCell(0);
                    if (weekCell != null) {
                        lesson.setWeek(getIntegerFromCell(weekCell));
                    }
                    
                    // Topic name column
                    Cell topicCell = row.getCell(1);
                    if (topicCell != null) {
                        lesson.setTopicName(getStringFromCell(topicCell));
                    }
                    
                    // Lesson type column
                    Cell typeCell = row.getCell(2);
                    if (typeCell != null) {
                        lesson.setLessonType(getStringFromCell(typeCell));
                    }
                    
                    // Objectives column
                    Cell objectivesCell = row.getCell(3);
                    if (objectivesCell != null) {
                        lesson.setObjectives(getStringFromCell(objectivesCell));
                    }
                    
                    // Requirements column
                    Cell requirementsCell = row.getCell(4);
                    if (requirementsCell != null) {
                        lesson.setRequirements(getStringFromCell(requirementsCell));
                    }
                    
                    // Preparations column
                    Cell preparationsCell = row.getCell(5);
                    if (preparationsCell != null) {
                        lesson.setPreparations(getStringFromCell(preparationsCell));
                    }
                    
                    // Duration column (optional)
                    Cell durationCell = row.getCell(6);
                    if (durationCell != null) {
                        lesson.setDurationMinutes(getIntegerFromCell(durationCell));
                    } else {
                        lesson.setDurationMinutes(120);
                    }
                    
                    // Only add if week and topic name are valid
                    if (lesson.isValid()) {
                        lessons.add(lesson);
                        logger.debug("Parsed lesson: Week {}, Topic: {}", lesson.getWeek(), lesson.getTopicName());
                    }
                    
                } catch (Exception e) {
                    logger.warn("Error parsing row {}: {}", i, e.getMessage());
                }
            }
            
        } catch (IOException e) {
            logger.error("Error reading Excel file: {}", e.getMessage());
            throw new IOException("Cannot read Excel file", e);
        }
        
        logger.info("Successfully parsed {} lessons from Excel", lessons.size());
        return lessons;
    }
    
    /**
     * Create Excel template for download
     */
    @Transactional
    public byte[] createExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Course Template");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Tuần", "Tên Chủ Đề", "Loại Hình", "Mục Đích", "Yêu Cầu Đạt Được", "Chuẩn Bị", "Thời Lượng (Phút)"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Add sample data
            addSampleData(sheet);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
    
    /**
     * Export course template to Excel
     */
    @Transactional
    public byte[] exportCourseTemplateToExcel(Long courseTemplateId) throws IOException {
        CourseTemplate courseTemplate = courseTemplateRepository.findById(courseTemplateId)
            .orElseThrow(() -> new RuntimeException("Course template not found"));
        
        try (Workbook workbook = new XSSFWorkbook()) {
            String sheetName = courseTemplate.getName().replaceAll("[\\/*[\\]?:]", "");
            if (sheetName.length() > 31) {
                sheetName = sheetName.substring(0, 31);
            }
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Create header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Tuần", "Tên Chủ Đề", "Loại Hình", "Mục Đích", "Yêu Cầu Đạt Được", "Chuẩn Bị", "Thời Lượng (Phút)"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Add lesson data
            List<LessonTemplate> lessons = lessonTemplateRepository.findByCourseTemplateIdOrderByWeekNumberAscSortOrderAsc(courseTemplateId);
            int rowNum = 1;
            
            for (LessonTemplate lesson : lessons) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(lesson.getWeekNumber());
                row.createCell(1).setCellValue(lesson.getTopicName() != null ? lesson.getTopicName() : "");
                row.createCell(2).setCellValue(lesson.getLessonType() != null ? lesson.getLessonType() : "");
                row.createCell(3).setCellValue(lesson.getObjectives() != null ? lesson.getObjectives() : "");
                row.createCell(4).setCellValue(lesson.getRequirements() != null ? lesson.getRequirements() : "");
                row.createCell(5).setCellValue(lesson.getPreparations() != null ? lesson.getPreparations() : "");
                row.createCell(6).setCellValue(lesson.getDurationMinutes());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
    
    /**
     * Import course template from Excel file
     */
    @Transactional
    public CourseTemplate importCourseFromExcel(MultipartFile file, String courseName, Long createdBy) throws IOException {
        // Parse lessons from Excel
        List<LessonData> lessons = parseExcelFile(file);
        
        if (lessons.isEmpty()) {
            throw new RuntimeException("No valid lessons found in Excel file");
        }
        
        // Create course template
        CourseTemplate courseTemplate = new CourseTemplate();
        courseTemplate.setName(courseName);
        courseTemplate.setCreatedBy(createdBy);
        courseTemplate.setTotalWeeks(lessons.size());
        
        courseTemplate = courseTemplateRepository.save(courseTemplate);
        
        // Create lesson templates
        int sortOrder = 0;
        for (LessonData lessonData : lessons) {
            LessonTemplate lessonTemplate = new LessonTemplate();
            lessonTemplate.setCourseTemplate(courseTemplate);
            lessonTemplate.setWeekNumber(lessonData.getWeek());
            lessonTemplate.setTopicName(lessonData.getTopicName());
            lessonTemplate.setLessonType(lessonData.getLessonType());
            lessonTemplate.setObjectives(lessonData.getObjectives());
            lessonTemplate.setRequirements(lessonData.getRequirements());
            lessonTemplate.setPreparations(lessonData.getPreparations());
            lessonTemplate.setDurationMinutes(lessonData.getDurationMinutes());
            lessonTemplate.setSortOrder(sortOrder++);
            
            lessonTemplateRepository.save(lessonTemplate);
        }
        
        // Update total weeks based on distinct week numbers
        Integer maxWeek = lessonTemplateRepository.findMaxWeekNumberByCourseTemplateId(courseTemplate.getId());
        courseTemplate.setTotalWeeks(maxWeek);
        courseTemplateRepository.save(courseTemplate);
        
        logger.info("Successfully imported course '{}' with {} lessons", courseName, lessons.size());
        return courseTemplate;
    }
    
    private String getStringFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getRichStringCellValue().getString();
            default:
                return null;
        }
    }
    
    private Integer getIntegerFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    private void addSampleData(Sheet sheet) {
        String[][] sampleData = {
            {"1", "Giới thiệu về Java", "Lý thuyết", "Hiểu cơ bản về lập trình Java", "Nắm vững cú pháp cơ bản", "Đọc tài liệu Java", "120"},
            {"2", "Cú pháp cơ bản", "Thực hành", "Học cú pháp cơ bản Java", "Viết được chương trình đơn giản", "IDE, JDK", "120"},
            {"3", "Biến và kiểu dữ liệu", "Thực hành", "Xử lý biến và kiểu dữ liệu", "Khai báo và sử dụng biến", "Tài liệu tham khảo", "150"}
        };
        
        for (int rowIndex = 0; rowIndex < sampleData.length; rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            for (int colIndex = 0; colIndex < sampleData[rowIndex].length; colIndex++) {
                row.createCell(colIndex).setCellValue(sampleData[rowIndex][colIndex]);
            }
        }
    }
}