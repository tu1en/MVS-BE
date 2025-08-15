package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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
    
    @Transactional
    public List<LessonData> parseExcelFile(MultipartFile file) throws IOException {
        List<LessonData> lessons = new ArrayList<>();
        
        // VALIDATION CHUẨN CẤP 3 - NGHIÊM NGẶT THEO TEMPLATE
        validateTemplateStrictly(file);
        
        // Enhanced validation
        if (file == null || file.isEmpty()) {
            throw new IOException("❌ File không được để trống. Vui lòng sử dụng template.xlsx chuẩn được cung cấp");
        }
        
        if (file.getSize() > 50 * 1024 * 1024) { // 50MB limit
            throw new IOException("❌ File không được vượt quá 50MB");
        }
        
        // QUAN TRỌNG: CHỈ CHẤP NHẬN .xlsx THEO ĐÚNG TEMPLATE CHUẨN
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            throw new IOException("❌ CHỈ CHẤP NHẬN FILE .xlsx THEO ĐÚNG TEMPLATE CHUẨN!\n" +
                "Vui lòng download template.xlsx chính thức từ hệ thống và sử dụng đúng định dạng này.\n" +
                "KHÔNG chấp nhận file .xls hoặc định dạng khác!");
        }
        
        // Validate MIME type nghiêm ngặt
        String contentType = file.getContentType();
        if (contentType == null || 
            !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            throw new IOException("❌ File không đúng định dạng .xlsx chuẩn!\n" +
                "File upload có MIME type: " + contentType + "\n" +
                "Yêu cầu: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\n" +
                "Vui lòng sử dụng template.xlsx chính thức được cung cấp!");
        }
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            
            if (totalRows < 1) {
                throw new IOException("File Excel không có dữ liệu");
            }
            
            if (totalRows > 1000) { // Limit to prevent memory issues
                logger.warn("File có {} dòng, chỉ xử lý 1000 dòng đầu", totalRows);
                totalRows = 1000;
            }
            
            // Skip header row (row 0)
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                LessonData lesson = new LessonData();
                
                try {
                    // Week column (required)
                    Cell weekCell = row.getCell(0);
                    if (weekCell != null) {
                        Integer week = getIntegerFromCell(weekCell);
                        if (week == null || week < 1 || week > 52) {
                            logger.warn("Dòng {}: Tuần học không hợp lệ: {}", i, week);
                            continue;
                        }
                        lesson.setWeek(week);
                    } else {
                        logger.warn("Dòng {}: Thiếu thông tin tuần học", i);
                        continue;
                    }
                    
                    // Topic name column (required)
                    Cell topicCell = row.getCell(1);
                    if (topicCell != null) {
                        String topic = getStringFromCell(topicCell);
                        if (topic == null || topic.trim().isEmpty()) {
                            logger.warn("Dòng {}: Thiếu tên chủ đề", i);
                            continue;
                        }
                        if (topic.length() > 255) {
                            topic = topic.substring(0, 255);
                            logger.warn("Dòng {}: Tên chủ đề quá dài, đã cắt ngắn", i);
                        }
                        lesson.setTopicName(topic);
                    } else {
                        logger.warn("Dòng {}: Thiếu tên chủ đề", i);
                        continue;
                    }
                    
                    // Lesson type column
                    Cell typeCell = row.getCell(2);
                    if (typeCell != null) {
                        String type = getStringFromCell(typeCell);
                        if (type != null && type.length() > 100) {
                            type = type.substring(0, 100);
                        }
                        lesson.setLessonType(type);
                    }
                    
                    // Objectives column
                    Cell objectivesCell = row.getCell(3);
                    if (objectivesCell != null) {
                        String objectives = getStringFromCell(objectivesCell);
                        if (objectives != null && objectives.length() > 1000) {
                            objectives = objectives.substring(0, 1000);
                        }
                        lesson.setObjectives(objectives);
                    }
                    
                    // Requirements column
                    Cell requirementsCell = row.getCell(4);
                    if (requirementsCell != null) {
                        String requirements = getStringFromCell(requirementsCell);
                        if (requirements != null && requirements.length() > 1000) {
                            requirements = requirements.substring(0, 1000);
                        }
                        lesson.setRequirements(requirements);
                    }
                    
                    // Preparations column
                    Cell preparationsCell = row.getCell(5);
                    if (preparationsCell != null) {
                        String preparations = getStringFromCell(preparationsCell);
                        if (preparations != null && preparations.length() > 1000) {
                            preparations = preparations.substring(0, 1000);
                        }
                        lesson.setPreparations(preparations);
                    }
                    
                    // Duration column (optional)
                    Cell durationCell = row.getCell(6);
                    if (durationCell != null) {
                        Integer duration = getIntegerFromCell(durationCell);
                        if (duration != null && duration > 0 && duration <= 480) { // Max 8 hours
                            lesson.setDurationMinutes(duration);
                        } else {
                            lesson.setDurationMinutes(120); // Default 2 hours
                            if (duration != null) {
                                logger.warn("Dòng {}: Thời lượng không hợp lệ: {} phút, sử dụng mặc định 120 phút", i, duration);
                            }
                        }
                    } else {
                        lesson.setDurationMinutes(120); // Default 2 hours
                    }
                    
                    // Only add if lesson is valid
                    if (lesson.isValid()) {
                        lessons.add(lesson);
                        logger.debug("Parsed lesson: Week {}, Topic: {}", lesson.getWeek(), lesson.getTopicName());
                    } else {
                        logger.warn("Dòng {}: Bài học không hợp lệ, bỏ qua", i);
                    }
                    
                } catch (Exception e) {
                    logger.warn("Lỗi xử lý dòng {}: {}", i, e.getMessage());
                }
            }
            
        } catch (IOException e) {
            logger.error("Lỗi đọc file Excel: {}", e.getMessage());
            throw new IOException("Không thể đọc file Excel: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Lỗi xử lý file Excel: {}", e.getMessage());
            throw new IOException("Lỗi xử lý file Excel: " + e.getMessage(), e);
        }
        
        if (lessons.isEmpty()) {
            throw new IOException("Không tìm thấy bài học hợp lệ nào trong file Excel");
        }
        
        logger.info("Thành công phân tích {} bài học từ file Excel", lessons.size());
        
        // Debug logging - print first few lessons
        for (int i = 0; i < Math.min(3, lessons.size()); i++) {
            LessonData lesson = lessons.get(i);
            logger.info("DEBUG - Lesson {}: Week={}, Topic='{}', Type='{}', Duration={}", 
                       i+1, lesson.getWeek(), lesson.getTopicName(), lesson.getLessonType(), lesson.getDurationMinutes());
        }
        
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
    
    @Transactional
    public byte[] exportCourseTemplateToExcel(Long courseTemplateId) throws IOException {
        if (courseTemplateId == null) {
            throw new IllegalArgumentException("Course template ID không được null");
        }
        
        CourseTemplate courseTemplate = courseTemplateRepository.findById(courseTemplateId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy template khóa học với ID: " + courseTemplateId));
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sanitize sheet name
            String sheetName = courseTemplate.getName();
            if (sheetName == null || sheetName.trim().isEmpty()) {
                sheetName = "Course Template";
            } else {
                // Remove invalid characters for Excel sheet names
                sheetName = sheetName.replaceAll("[\\\\/*[\\]?:]", "");
                if (sheetName.length() > 31) {
                    sheetName = sheetName.substring(0, 31);
                }
                if (sheetName.trim().isEmpty()) {
                    sheetName = "Course Template";
                }
            }
            
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Create header with styling
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Tuần", "Tên Chủ Đề", "Loại Hình", "Mục Đích", "Yêu Cầu Đạt Được", "Chuẩn Bị", "Thời Lượng (Phút)"};
            
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
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Get lesson data
            List<LessonTemplate> lessons = lessonTemplateRepository.findByCourseTemplateIdOrderByWeekNumberAscSortOrderAsc(courseTemplateId);
            
            if (lessons.isEmpty()) {
                logger.warn("Không có bài học nào trong template {}", courseTemplateId);
            }
            
            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setWrapText(true);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
            
            // Add lesson data
            int rowNum = 1;
            for (LessonTemplate lesson : lessons) {
                Row row = sheet.createRow(rowNum++);
                
                // Week number
                Cell weekCell = row.createCell(0);
                weekCell.setCellValue(lesson.getWeekNumber() != null ? lesson.getWeekNumber() : 0);
                weekCell.setCellStyle(dataStyle);
                
                // Topic name
                Cell topicCell = row.createCell(1);
                topicCell.setCellValue(lesson.getTopicName() != null ? lesson.getTopicName() : "");
                topicCell.setCellStyle(dataStyle);
                
                // Lesson type
                Cell typeCell = row.createCell(2);
                typeCell.setCellValue(lesson.getLessonType() != null ? lesson.getLessonType() : "");
                typeCell.setCellStyle(dataStyle);
                
                // Objectives
                Cell objectivesCell = row.createCell(3);
                objectivesCell.setCellValue(lesson.getObjectives() != null ? lesson.getObjectives() : "");
                objectivesCell.setCellStyle(dataStyle);
                
                // Requirements
                Cell requirementsCell = row.createCell(4);
                requirementsCell.setCellValue(lesson.getRequirements() != null ? lesson.getRequirements() : "");
                requirementsCell.setCellStyle(dataStyle);
                
                // Preparations
                Cell preparationsCell = row.createCell(5);
                preparationsCell.setCellValue(lesson.getPreparations() != null ? lesson.getPreparations() : "");
                preparationsCell.setCellStyle(dataStyle);
                
                // Duration
                Cell durationCell = row.createCell(6);
                durationCell.setCellValue(lesson.getDurationMinutes() != null ? lesson.getDurationMinutes() : 120);
                durationCell.setCellStyle(dataStyle);
            }
            
            // Auto-size columns with limits
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set max width to prevent extremely wide columns
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth > 15000) { // ~58 characters
                    sheet.setColumnWidth(i, 15000);
                }
                // Set minimum width
                if (currentWidth < 2000) { // ~8 characters
                    sheet.setColumnWidth(i, 2000);
                }
            }
            
            // Set row heights for better readability
            for (int i = 1; i <= lessons.size(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    row.setHeightInPoints(30); // Increased height for wrapped text
                }
            }
            
            // Write to byte array
            try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                workbook.write(outputStream);
                byte[] result = outputStream.toByteArray();
                
                logger.info("Thành công export template '{}' với {} bài học, kích thước file: {} bytes", 
                           courseTemplate.getName(), lessons.size(), result.length);
                
                return result;
            }
            
        } catch (IOException e) {
            logger.error("Lỗi tạo file Excel cho template {}: {}", courseTemplateId, e.getMessage());
            throw new IOException("Không thể tạo file Excel: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Lỗi không mong muốn khi export template {}: {}", courseTemplateId, e.getMessage());
            throw new IOException("Lỗi export template: " + e.getMessage(), e);
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
    
    /**
     * VALIDATION TEMPLATE CHUẨN CẤP 3 - NGHIÊM NGẶT 100%
     * Bắt buộc phải sử dụng đúng template.xlsx chuẩn - KHÔNG CHO PHÉP SAI LỆCH
     */
    private void validateTemplateStrictly(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 1. VALIDATE HEADER CHÍNH XÁC 100%
            validateHeaderExact(sheet);
            
            // 2. VALIDATE CẤU TRÚC CHUẨN
            validateStructureStandard(sheet);
            
            logger.info("✅ Template validation PASSED - File đúng chuẩn template.xlsx");
            
        } catch (IOException e) {
            throw e; // Re-throw validation errors
        } catch (Exception e) {
            throw new IOException("❌ File không đúng chuẩn template.xlsx: " + e.getMessage());
        }
    }
    
    /**
     * Validate header phải chính xác 100% theo template chuẩn
     */
    private void validateHeaderExact(Sheet sheet) throws IOException {
        // Header chuẩn bắt buộc (KHÔNG ĐƯỢC THAY ĐỔI)
        String[] REQUIRED_HEADERS = {
            "Tuần", "Tên Chủ Đề", "Loại Hình", "Mục Đích", 
            "Yêu Cầu Đạt Được", "Chuẩn Bị", "Thời Lượng (Phút)"
        };
        
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IOException("❌ THIẾU DÒNG HEADER!\n" +
                "Template chuẩn phải có dòng tiêu đề đầu tiên.\n" +
                "Vui lòng sử dụng template.xlsx chính thức được cung cấp!");
        }
        
        // Kiểm tra số cột chính xác
        int actualColumns = headerRow.getLastCellNum();
        if (actualColumns != REQUIRED_HEADERS.length) {
            throw new IOException("❌ SỐ CỘT KHÔNG ĐÚNG CHUẨN!\n" +
                String.format("Template chuẩn phải có CHÍNH XÁC %d cột.\n", REQUIRED_HEADERS.length) +
                String.format("File của bạn có %d cột.\n", actualColumns) +
                "Vui lòng download và sử dụng template.xlsx chính thức!");
        }
        
        // Validate từng header một cách nghiêm ngặt
        StringBuilder errorMsg = new StringBuilder();
        for (int i = 0; i < REQUIRED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String actualHeader = (cell != null && cell.getStringCellValue() != null) 
                ? cell.getStringCellValue().trim() : "";
            
            String expectedHeader = REQUIRED_HEADERS[i];
            if (!expectedHeader.equals(actualHeader)) {
                errorMsg.append(String.format("• Cột %d: Mong đợi '%s', thực tế '%s'\n", 
                    i + 1, expectedHeader, actualHeader));
            }
        }
        
        if (errorMsg.length() > 0) {
            throw new IOException("❌ HEADER KHÔNG ĐÚNG CHUẨN TEMPLATE!\n\n" +
                "Các lỗi được phát hiện:\n" + errorMsg.toString() + "\n" +
                "🚫 KHÔNG ĐƯỢC THAY ĐỔI HEADER TRONG TEMPLATE!\n" +
                "✅ Vui lòng download template.xlsx chính thức và điền dữ liệu vào đúng cột tương ứng.\n" +
                "📋 Header chuẩn: [" + String.join(", ", REQUIRED_HEADERS) + "]");
        }
    }
    
    /**
     * Validate cấu trúc và dữ liệu chuẩn
     */
    private void validateStructureStandard(Sheet sheet) throws IOException {
        int totalRows = sheet.getLastRowNum();
        
        // Kiểm tra có dữ liệu không
        if (totalRows < 1) {
            throw new IOException("❌ TEMPLATE TRỐNG!\n" +
                "Template phải có ít nhất 1 dòng dữ liệu sau header.\n" +
                "Vui lòng điền thông tin khóa học vào template và upload lại!");
        }
        
        // Kiểm tra giới hạn dòng
        if (totalRows > 1000) {
            throw new IOException("❌ QUÁ NHIỀU DỮ LIỆU!\n" +
                String.format("Template chỉ hỗ trợ tối đa 1000 dòng dữ liệu. File của bạn có %d dòng.\n", totalRows) +
                "Vui lòng chia nhỏ dữ liệu và upload từng phần!");
        }
        
        // Validate sample data rows
        int validRows = 0;
        StringBuilder dataErrors = new StringBuilder();
        
        for (int i = 1; i <= Math.min(totalRows, 5); i++) { // Check first 5 rows for quick validation
            Row row = sheet.getRow(i);
            if (row != null) {
                String rowError = validateRowDataStandard(row, i);
                if (rowError == null) {
                    validRows++;
                } else {
                    dataErrors.append("• Dòng ").append(i + 1).append(": ").append(rowError).append("\n");
                }
            }
        }
        
        if (validRows == 0 && dataErrors.length() > 0) {
            throw new IOException("❌ DỮ LIỆU KHÔNG ĐÚNG ĐỊNH DẠNG!\n\n" +
                "Các lỗi được phát hiện:\n" + dataErrors.toString() + "\n" +
                "📋 Quy tắc nhập liệu:\n" +
                "• Cột 'Tuần': Số nguyên từ 1-52\n" +
                "• Cột 'Tên Chủ Đề': Bắt buộc, tối đa 255 ký tự\n" +
                "• Cột 'Thời Lượng': Số phút từ 1-480 (nếu có)\n" +
                "• Các cột khác: Tùy chọn, tối đa 1000 ký tự");
        }
        
        logger.info("✅ Đã validate {} dòng dữ liệu đầu tiên - Format đúng chuẩn", validRows);
    }
    
    /**
     * Validate một dòng dữ liệu theo chuẩn nghiêm ngặt
     */
    private String validateRowDataStandard(Row row, int rowIndex) {
        // Check if row has any data
        boolean hasData = false;
        for (int i = 0; i < 7; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.toString().trim().length() > 0) {
                hasData = true;
                break;
            }
        }
        
        if (!hasData) {
            return null; // Empty row - skip
        }
        
        // Validate required fields
        Cell weekCell = row.getCell(0);
        Integer week = getIntegerFromCell(weekCell);
        if (week == null) {
            return "Thiếu số tuần (cột 1)";
        }
        if (week < 1 || week > 52) {
            return "Số tuần phải từ 1-52, hiện tại: " + week;
        }
        
        Cell topicCell = row.getCell(1);
        String topic = getStringFromCell(topicCell);
        if (topic == null || topic.trim().isEmpty()) {
            return "Thiếu tên chủ đề (cột 2)";
        }
        if (topic.length() > 255) {
            return "Tên chủ đề quá dài (>" + topic.length() + " ký tự)";
        }
        
        // Validate optional duration
        Cell durationCell = row.getCell(6);
        if (durationCell != null) {
            Integer duration = getIntegerFromCell(durationCell);
            if (duration != null && (duration < 1 || duration > 480)) {
                return "Thời lượng phải từ 1-480 phút, hiện tại: " + duration;
            }
        }
        
        return null; // Valid row
    }
}