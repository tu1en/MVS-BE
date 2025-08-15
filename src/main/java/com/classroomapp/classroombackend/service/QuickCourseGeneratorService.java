package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service tạo nhanh khung chương trình với bài giảng theo template chuẩn
 * Hỗ trợ tạo template cho các môn học phổ biến với nội dung mẫu
 */
@Service
public class QuickCourseGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuickCourseGeneratorService.class);
    
    // Header chuẩn (KHÔNG ĐƯỢC THAY ĐỔI)
    private static final String[] HEADERS = {
        "Tuần", "Tên Chủ Đề", "Loại Hình", "Mục Đích", 
        "Yêu Cầu Đạt Được", "Chuẩn Bị", "Thời Lượng (Phút)"
    };
    
    /**
     * Tạo template nhanh cho môn Toán lớp 12
     */
    public byte[] generateMathGrade12Template() throws IOException {
        String[][] courseData = {
            {"1", "Ôn tập kiến thức lớp 11 - Hàm số và đạo hàm", "Ôn tập + Kiểm tra", 
             "Hệ thống lại kiến thức cơ bản về hàm số", 
             "Nắm vững khái niệm hàm số, tính đạo hàm cơ bản", 
             "SGK Toán 11, bài tập ôn tập", "90"},
             
            {"2", "Khảo sát hàm số - Tính đơn điệu", "Lý thuyết + Bài tập", 
             "Xác định khoảng đơn điệu của hàm số", 
             "Vận dụng đạo hàm để xét tính đơn điệu", 
             "Bảng biến thiên, bài tập mẫu", "90"},
             
            {"3", "Cực trị của hàm số", "Lý thuyết + Bài tập", 
             "Tìm cực đại, cực tiểu của hàm số", 
             "Giải thành thạo bài toán cực trị", 
             "Điều kiện cần và đủ của cực trị", "90"},
             
            {"4", "Giá trị lớn nhất và nhỏ nhất", "Bài tập", 
             "Tìm GTLN, GTNN trên đoạn và khoảng", 
             "Áp dụng vào bài toán thực tế", 
             "Phương pháp tìm GTLN, GTNN", "90"},
             
            {"5", "Đường tiệm cận của đồ thị hàm số", "Lý thuyết + Bài tập", 
             "Xác định tiệm cận đứng, ngang, xiên", 
             "Vẽ được tiệm cận của đồ thị hàm số", 
             "Các dạng tiệm cận, ví dụ minh họa", "90"},
             
            {"6", "Khảo sát và vẽ đồ thị hàm số", "Bài tập", 
             "Khảo sát hoàn chỉnh hàm bậc 3, bậc 4", 
             "Vẽ chính xác đồ thị hàm số", 
             "Sơ đồ khảo sát, giấy kẻ ô vuông", "90"},
             
            {"7", "Logarit và hàm số logarit", "Lý thuyết + Bài tập", 
             "Nắm vững khái niệm và tính chất logarit", 
             "Tính toán thành thạo với logarit", 
             "Bảng tính chất, máy tính bỏ túi", "90"},
             
            {"8", "Phương trình logarit", "Bài tập", 
             "Giải các dạng phương trình logarit", 
             "Vận dụng tính chất để giải phương trình", 
             "Phương pháp giải, bài tập phân loại", "90"},
             
            {"9", "Bất phương trình logarit", "Bài tập", 
             "Giải bất phương trình logarit cơ bản", 
             "Chú ý điều kiện xác định", 
             "Phương pháp đổi cơ số", "90"},
             
            {"10", "Nguyên hàm và tích phân", "Lý thuyết + Bài tập", 
             "Hiểu khái niệm nguyên hàm và tích phân", 
             "Tính nguyên hàm của hàm cơ bản", 
             "Bảng nguyên hàm, quy tắc tính", "90"},
             
            {"11", "Phương pháp tính tích phân", "Bài tập", 
             "Tích phân từng phần, đổi biến số", 
             "Vận dụng linh hoạt các phương pháp", 
             "Công thức và ví dụ minh họa", "90"},
             
            {"12", "Ứng dụng tích phân - Tính diện tích", "Bài tập", 
             "Tính diện tích hình phẳng bằng tích phân", 
             "Vẽ hình và thiết lập công thức", 
             "Các dạng bài tập diện tích", "90"},
             
            {"13", "Số phức - Khái niệm cơ bản", "Lý thuyết + Bài tập", 
             "Nắm vững khái niệm số phức", 
             "Thực hiện các phép toán với số phức", 
             "Dạng đại số của số phức", "90"},
             
            {"14", "Phương trình trong tập số phức", "Bài tập", 
             "Giải phương trình bậc 2 trong C", 
             "Tìm nghiệm phức của phương trình", 
             "Công thức nghiệm và ví dụ", "90"},
             
            {"15", "Ôn tập chương Hình học không gian", "Ôn tập", 
             "Hệ thống kiến thức hình học 11", 
             "Giải bài toán thể tích, diện tích", 
             "Mô hình hình học, công thức", "90"},
             
            {"16", "Kiểm tra cuối kỳ - Đánh giá tổng hợp", "Kiểm tra", 
             "Đánh giá toàn diện kiến thức đã học", 
             "Hoàn thành bài kiểm tra trong thời gian quy định", 
             "Đề thi mẫu, phiếu trả lời", "90"}
        };
        
        return generateTemplate("Toán lớp 12", courseData);
    }
    
    /**
     * Tạo template nhanh cho môn Ngữ văn lớp 12
     */
    public byte[] generateLiteratureGrade12Template() throws IOException {
        String[][] courseData = {
            {"1", "Ôn tập kiến thức lớp 11 - Văn học hiện đại", "Ôn tập + Kiểm tra", 
             "Hệ thống lại kiến thức văn học thế kỷ XX", 
             "Nắm vững đặc điểm văn học hiện đại", 
             "SGK Ngữ văn 11, tác phẩm đã học", "90"},
             
            {"2", "Thơ Xuân Diệu - Phong cách nghệ thuật", "Lý thuyết + Phân tích", 
             "Tìm hiểu phong cách thơ ca Xuân Diệu", 
             "Phân tích được đặc sắc nghệ thuật", 
             "Tuyển tập thơ Xuân Diệu", "90"},
             
            {"3", "Huy Cận và dòng thơ trữ tình", "Phân tích tác phẩm", 
             "Nghiên cứu chủ đề tình yêu trong thơ", 
             "Cảm nhận vẻ đẹp tình cảm chân thành", 
             "Thơ Huy Cận, tài liệu phân tích", "90"},
             
            {"4", "Tố Hữu - Thơ cách mạng và đời thường", "Phân tích tác phẩm", 
             "Tìm hiểu hai giai đoạn sáng tác", 
             "So sánh đặc điểm hai dòng thơ", 
             "Tuyển tập thơ Tố Hữu", "90"},
             
            {"5", "Chế Lan Viên và thơ thiên nhiên", "Phân tích tác phẩm", 
             "Khám phá tình yêu thiên nhiên quê hương", 
             "Cảm nhận vẻ đẹp miêu tả phong cảnh", 
             "Thơ Chế Lan Viên về thiên nhiên", "90"},
             
            {"6", "Văn xuôi Nguyễn Minh Châu", "Phân tích tác phẩm", 
             "Nghiên cứu nghệ thuật tâm lý học", 
             "Phân tích nhân vật và tình huống", 
             "Truyện ngắn Nguyễn Minh Châu", "90"},
             
            {"7", "Nguyễn Tuân - Phong cách tùy bút", "Phân tích tác phẩm", 
             "Tìm hiểu thể loại tùy bút độc đáo", 
             "Nắm được đặc điểm ngôn ngữ nghệ thuật", 
             "Tùy bút Nguyễn Tuân", "90"},
             
            {"8", "Thơ Quang Dũng - Chiến tranh và hòa bình", "Phân tích tác phẩm", 
             "Cảm nhận tinh thần yêu nước, yêu đời", 
             "Hiểu được ý nghĩa nhân văn sâu sắc", 
             "Thơ Quang Dũng về chiến tranh", "90"},
             
            {"9", "Truyện Dế Mèn phiêu lưu ký", "Phân tích tác phẩm", 
             "Nghiên cứu giá trị giáo dục của tác phẩm", 
             "Rút ra bài học về lòng dũng cảm", 
             "Truyện Tô Hoài, tài liệu phân tích", "90"},
             
            {"10", "Kiều - Nguyễn Du (Ôn tập)", "Ôn tập tác phẩm", 
             "Hệ thống lại kiến thức về Truyện Kiều", 
             "Vận dụng phân tích đoạn thơ Kiều", 
             "Truyện Kiều, tài liệu ôn tập", "90"},
             
            {"11", "Văn học dân gian - Truyện cổ tích", "Lý thuyết + Phân tích", 
             "Tìm hiểu đặc điểm truyện cổ tích Việt Nam", 
             "Nhận biết yếu tố kỳ ảo và thực tế", 
             "Truyện cổ tích Việt Nam", "90"},
             
            {"12", "Nghị luận văn học - Kỹ năng viết", "Lý thuyết + Thực hành", 
             "Nắm vững cấu trúc bài nghị luận văn học", 
             "Viết được bài nghị luận hoàn chỉnh", 
             "Mẫu bài nghị luận, đề thi mẫu", "90"},
             
            {"13", "Nghị luận xã hội - Vấn đề đương đại", "Lý thuyết + Thực hành", 
             "Phân tích các vấn đề xã hội hiện tại", 
             "Viết bài nghị luận xã hội có tính thuyết phục", 
             "Tài liệu thời sự, báo chí", "90"},
             
            {"14", "Luyện thi THPT Quốc gia - Phần văn học", "Ôn tập + Luyện thi", 
             "Ôn tập toàn diện kiến thức văn học", 
             "Làm thành thạo các dạng câu hỏi thi", 
             "Đề thi thử, tài liệu ôn tập", "90"},
             
            {"15", "Luyện thi THPT Quốc gia - Phần làm văn", "Ôn tập + Luyện thi", 
             "Luyện viết các thể loại văn thi đại học", 
             "Viết văn trong thời gian quy định", 
             "Đề thi mẫu, bài văn hay", "90"},
             
            {"16", "Thi thử THPT Quốc gia môn Ngữ văn", "Thi thử", 
             "Đánh giá năng lực chuẩn bị thi đại học", 
             "Hoàn thành bài thi trong thời gian quy định", 
             "Đề thi chuẩn, phiếu trả lời", "150"}
        };
        
        return generateTemplate("Ngữ văn lớp 12", courseData);
    }
    
    /**
     * Tạo template cho môn Tiếng Anh lớp 12
     */
    public byte[] generateEnglishGrade12Template() throws IOException {
        String[][] courseData = {
            {"1", "Ôn tập kiến thức lớp 11 - Grammar Review", "Ôn tập + Kiểm tra", 
             "Hệ thống lại các thì và cấu trúc ngữ pháp", 
             "Sử dụng chính xác các thì cơ bản", 
             "SGK Tiếng Anh 11, bài tập ôn tập", "90"},
             
            {"2", "NumPy - Thư viện tính toán khoa học", "Thực hành", 
             "Xử lý mảng và tính toán với NumPy", 
             "Thao tác dữ liệu số hiệu quả", 
             "NumPy documentation, examples", "120"},
             
            {"3", "Pandas - Thao tác và phân tích dữ liệu", "Thực hành", 
             "Làm sạch và xử lý dữ liệu với Pandas", 
             "Thành thạo DataFrame operations", 
             "Sample datasets, Pandas cheatsheet", "180"},
             
            {"4", "Matplotlib và Seaborn - Visualization", "Thực hành", 
             "Tạo biểu đồ và trực quan hóa dữ liệu", 
             "Thiết kế charts chuyên nghiệp", 
             "Visualization examples, color palettes", "150"},
             
            {"5", "Statistics cơ bản với Python", "Lý thuyết + Thực hành", 
             "Áp dụng thống kê mô tả và suy luận", 
             "Hiểu và tính toán các metrics", 
             "Statistical libraries, case studies", "180"},
             
            {"6", "Data Cleaning và Preprocessing", "Thực hành", 
             "Làm sạch và chuẩn bị dữ liệu cho phân tích", 
             "Xử lý missing data và outliers", 
             "Real datasets with quality issues", "150"},
             
            {"7", "Exploratory Data Analysis (EDA)", "Thực hành", 
             "Khám phá và hiểu dữ liệu sâu hơn", 
             "Tìm insights và patterns trong data", 
             "EDA templates, business cases", "180"},
             
            {"8", "Machine Learning với Scikit-learn", "Lý thuyết + Thực hành", 
             "Giới thiệu ML và supervised learning", 
             "Xây dựng model đầu tiên", 
             "Scikit-learn documentation", "180"},
             
            {"9", "Regression Analysis", "Thực hành", 
             "Dự đoán giá trị liên tục", 
             "Đánh giá và cải thiện regression models", 
             "Regression datasets", "150"},
             
            {"10", "Classification Algorithms", "Thực hành", 
             "Phân loại dữ liệu với ML", 
             "So sánh các thuật toán classification", 
             "Classification datasets", "180"},
             
            {"11", "Clustering và Unsupervised Learning", "Thực hành", 
             "Nhóm dữ liệu không có label", 
             "Khám phá cấu trúc ẩn trong data", 
             "Clustering examples", "150"},
             
            {"12", "Time Series Analysis", "Thực hành", 
             "Phân tích dữ liệu theo thời gian", 
             "Dự đoán xu hướng và seasonality", 
             "Time series datasets", "180"},
             
            {"13", "SQL cho Data Science", "Thực hành", 
             "Truy vấn dữ liệu từ database", 
             "Kết hợp SQL với Python", 
             "Sample database, SQL practice", "120"},
             
            {"14", "Web Scraping và API", "Thực hành", 
             "Thu thập dữ liệu từ web", 
             "Sử dụng APIs để lấy data", 
             "BeautifulSoup, requests library", "150"},
             
            {"15", "Capstone Project - Data Science Pipeline", "Project", 
             "Dự án phân tích dữ liệu hoàn chỉnh", 
             "Từ data collection đến insights", 
             "Real business problem", "300"},
             
            {"16", "Project Presentation và Portfolio", "Presentation", 
             "Trình bày findings và build portfolio", 
             "Kỹ năng storytelling with data", 
             "Presentation templates", "120"}
        };
        
        return generateTemplate("Tiếng Anh lớp 12", courseData);
    }
    
    /**
     * Template cho môn Mobile App Development
     */
    public byte[] generateMobileAppTemplate() throws IOException {
        String[][] courseData = {
            {"1", "Giới thiệu Mobile Development và Flutter", "Lý thuyết + Demo", 
             "Tổng quan mobile dev và cài đặt Flutter", 
             "Setup môi trường phát triển hoàn chỉnh", 
             "Flutter SDK, Android Studio", "150"},
             
            {"2", "Dart Programming Language", "Thực hành", 
             "Nắm vững ngôn ngữ Dart cho Flutter", 
             "Viết code Dart thành thạo", 
             "Dart documentation, DartPad", "120"},
             
            {"3", "Flutter Widgets cơ bản", "Thực hành", 
             "Xây dựng UI với widgets", 
             "Tạo giao diện đẹp và responsive", 
             "Widget catalog, design mockups", "180"},
             
            {"4", "Layout và Navigation", "Thực hành", 
             "Thiết kế layout và điều hướng app", 
             "Multi-screen app navigation", 
             "Navigation examples", "150"},
             
            {"5", "State Management với Provider", "Thực hành", 
             "Quản lý trạng thái ứng dụng", 
             "Hiểu lifecycle và state changes", 
             "Provider package, examples", "180"},
             
            {"6", "Forms và User Input", "Thực hành", 
             "Xử lý input và validation", 
             "Tạo forms tương tác tốt", 
             "Form examples, validation rules", "120"},
             
            {"7", "HTTP Requests và API Integration", "Thực hành", 
             "Kết nối với web services", 
             "Parse JSON và error handling", 
             "Public APIs, Postman", "150"},
             
            {"8", "Local Database với SQLite", "Thực hành", 
             "Lưu trữ dữ liệu local", 
             "CRUD operations với database", 
             "sqflite package, DB browser", "180"},
             
            {"9", "Camera và Image Picker", "Thực hành", 
             "Tích hợp camera và gallery", 
             "Xử lý images trong app", 
             "image_picker package", "120"},
             
            {"10", "Maps và Location Services", "Thực hành", 
             "Tích hợp bản đồ và GPS", 
             "Location-based features", 
             "Google Maps API", "150"},
             
            {"11", "Push Notifications", "Thực hành", 
             "Gửi và nhận notifications", 
             "Engagement với users", 
             "Firebase Cloud Messaging", "120"},
             
            {"12", "Authentication với Firebase", "Thực hành", 
             "User login và registration", 
             "Secure authentication flow", 
             "Firebase Auth setup", "150"},
             
            {"13", "App Store Deployment", "Thực hành", 
             "Chuẩn bị app để publish", 
             "App store guidelines và submission", 
             "Developer accounts", "120"},
             
            {"14", "Performance Optimization", "Thực hành", 
             "Tối ưu hóa hiệu suất app", 
             "Debugging và profiling", 
             "Performance tools", "150"},
             
            {"15", "Final Project - Complete Mobile App", "Project", 
             "Phát triển ứng dụng di động hoàn chỉnh", 
             "Sản phẩm sẵn sàng publish", 
             "Project requirements", "400"},
             
            {"16", "App Demo và Feedback", "Presentation", 
             "Trình diễn app và nhận góp ý", 
             "Marketing và user feedback", 
             "Demo guidelines", "120"}
        };
        
        return generateTemplate("Mobile App Development", courseData);
    }
    
    /**
     * Tạo template tùy chỉnh với thông tin cơ bản
     */
    public byte[] generateCustomTemplate(String courseName, int totalWeeks, 
                                       String defaultLessonType, int defaultDuration) throws IOException {
        List<String[]> courseData = new ArrayList<>();
        
        for (int week = 1; week <= totalWeeks; week++) {
            String[] lessonData = {
                String.valueOf(week),
                "Chủ đề tuần " + week + " - " + courseName,
                defaultLessonType,
                "Mục đích học tập tuần " + week,
                "Yêu cầu đạt được sau tuần " + week,
                "Chuẩn bị tài liệu và bài tập",
                String.valueOf(defaultDuration)
            };
            courseData.add(lessonData);
        }
        
        return generateTemplate(courseName, courseData.toArray(new String[0][]));
    }
    
    /**
     * Core method để tạo template Excel
     */
    private byte[] generateTemplate(String courseName, String[][] courseData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Tạo sheet với tên course
            String sheetName = sanitizeSheetName(courseName);
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Tạo header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Tạo header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Thêm dữ liệu course
            for (int rowIndex = 0; rowIndex < courseData.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                String[] rowData = courseData[rowIndex];
                
                for (int colIndex = 0; colIndex < rowData.length; colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    
                    // Set value based on column type
                    if (colIndex == 0 || colIndex == 6) { // Week, Duration
                        try {
                            cell.setCellValue(Integer.parseInt(rowData[colIndex]));
                        } catch (NumberFormatException e) {
                            cell.setCellValue(rowData[colIndex]);
                        }
                    } else {
                        cell.setCellValue(rowData[colIndex]);
                    }
                    
                    cell.setCellStyle(dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                // Set maximum width for long text columns
                if (i >= 3 && i <= 5 && sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }
            
            // Convert to byte array
            try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                workbook.write(outputStream);
                byte[] result = outputStream.toByteArray();
                
                logger.info("✅ Đã tạo template '{}' với {} tuần học, size: {} bytes", 
                           courseName, courseData.length, result.length);
                           
                return result;
            }
        }
    }
    
    /**
     * Tạo style cho header
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        
        // Borders
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        
        // Alignment
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * Tạo style cho data cells
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Borders
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        
        // Text wrapping
        style.setWrapText(true);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
        
        return style;
    }
    
    /**
     * Sanitize sheet name cho Excel
     */
    private String sanitizeSheetName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Course Template";
        }
        
        // Remove invalid characters
        String sanitized = name.replaceAll("[\\\\/*[\\]?:]", "");
        
        // Limit length
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        
        return sanitized.trim().isEmpty() ? "Course Template" : sanitized;
    }
    
    /**
     * Lấy danh sách các template có sẵn cho cấp 3
     */
    public Map<String, String> getAvailableTemplates() {
        Map<String, String> templates = new HashMap<>();
        templates.put("math", "Toán lớp 12 - 16 tuần");
        templates.put("literature", "Ngữ văn lớp 12 - 16 tuần");
        templates.put("english", "Tiếng Anh lớp 12 - 16 tuần");
        templates.put("physics", "Vật lý lớp 12 - 16 tuần");
        templates.put("custom", "Template tùy chỉnh");
        
        return templates;
    }
}