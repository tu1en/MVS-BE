package com.classroomapp.classroombackend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VietnameseEncodingFixService implements CommandLineRunner {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    // Mapping của các ký tự bị lỗi encoding thường gặp
    private static final Map<String, String> ENCODING_FIX_MAP = new HashMap<>();
    
    static {
        // Các ký tự tiếng Việt thường bị lỗi
        ENCODING_FIX_MAP.put("Ã¡", "á");
        ENCODING_FIX_MAP.put("Ã ", "à");
        ENCODING_FIX_MAP.put("áº£", "ả");
        ENCODING_FIX_MAP.put("Ã£", "ã");
        ENCODING_FIX_MAP.put("áº¡", "ạ");
        ENCODING_FIX_MAP.put("Ã¢", "â");
        ENCODING_FIX_MAP.put("áº¥", "ấ");
        ENCODING_FIX_MAP.put("áº§", "ầ");
        ENCODING_FIX_MAP.put("áº©", "ẩ");
        ENCODING_FIX_MAP.put("áº«", "ẫ");
        ENCODING_FIX_MAP.put("áº­", "ậ");
        ENCODING_FIX_MAP.put("Ä", "ă");
        ENCODING_FIX_MAP.put("áº¯", "ắ");
        ENCODING_FIX_MAP.put("áº±", "ằ");
        ENCODING_FIX_MAP.put("áº³", "ẳ");
        ENCODING_FIX_MAP.put("áº¡", "ạ");
        ENCODING_FIX_MAP.put("Ã©", "é");
        ENCODING_FIX_MAP.put("Ã¨", "è");
        ENCODING_FIX_MAP.put("áº»", "ẻ");
        ENCODING_FIX_MAP.put("áº½", "ẽ");
        ENCODING_FIX_MAP.put("áº¹", "ẹ");
        ENCODING_FIX_MAP.put("Ãª", "ê");
        ENCODING_FIX_MAP.put("áº¿", "ế");
        ENCODING_FIX_MAP.put("á»", "ề");
        ENCODING_FIX_MAP.put("á»", "ể");
        ENCODING_FIX_MAP.put("á»", "ễ");
        ENCODING_FIX_MAP.put("á»", "ệ");
        ENCODING_FIX_MAP.put("Ã­", "í");
        ENCODING_FIX_MAP.put("Ã¬", "ì");
        ENCODING_FIX_MAP.put("á»", "ỉ");
        ENCODING_FIX_MAP.put("Ä©", "ĩ");
        ENCODING_FIX_MAP.put("á»", "ị");
        ENCODING_FIX_MAP.put("Ã³", "ó");
        ENCODING_FIX_MAP.put("Ã²", "ò");
        ENCODING_FIX_MAP.put("á»", "ỏ");
        ENCODING_FIX_MAP.put("Ãµ", "õ");
        ENCODING_FIX_MAP.put("á»", "ọ");
        ENCODING_FIX_MAP.put("Ã´", "ô");
        ENCODING_FIX_MAP.put("á»", "ố");
        ENCODING_FIX_MAP.put("á»", "ồ");
        ENCODING_FIX_MAP.put("á»", "ổ");
        ENCODING_FIX_MAP.put("á»", "ỗ");
        ENCODING_FIX_MAP.put("á»", "ộ");
        ENCODING_FIX_MAP.put("Æ¡", "ơ");
        ENCODING_FIX_MAP.put("á»", "ớ");
        ENCODING_FIX_MAP.put("á»", "ờ");
        ENCODING_FIX_MAP.put("á»", "ở");
        ENCODING_FIX_MAP.put("á»", "ỡ");
        ENCODING_FIX_MAP.put("á»£", "ợ");
        ENCODING_FIX_MAP.put("Ãº", "ú");
        ENCODING_FIX_MAP.put("Ã¹", "ù");
        ENCODING_FIX_MAP.put("á»§", "ủ");
        ENCODING_FIX_MAP.put("Å©", "ũ");
        ENCODING_FIX_MAP.put("á»¥", "ụ");
        ENCODING_FIX_MAP.put("Æ°", "ư");
        ENCODING_FIX_MAP.put("á»", "ứ");
        ENCODING_FIX_MAP.put("á»", "ừ");
        ENCODING_FIX_MAP.put("á»", "ử");
        ENCODING_FIX_MAP.put("á»", "ữ");
        ENCODING_FIX_MAP.put("á»±", "ự");
        ENCODING_FIX_MAP.put("Ã½", "ý");
        ENCODING_FIX_MAP.put("á»³", "ỳ");
        ENCODING_FIX_MAP.put("á»·", "ỷ");
        ENCODING_FIX_MAP.put("á»¹", "ỹ");
        ENCODING_FIX_MAP.put("á»µ", "ỵ");
        ENCODING_FIX_MAP.put("Ä", "đ");
        
        // Các pattern phổ biến bị lỗi
        ENCODING_FIX_MAP.put("c?p", "cấp");
        ENCODING_FIX_MAP.put("h?c", "học");
        ENCODING_FIX_MAP.put("Vi?t", "Việt");
        ENCODING_FIX_MAP.put("ti?ng", "tiếng");
        ENCODING_FIX_MAP.put("Ti?ng", "Tiếng");
        ENCODING_FIX_MAP.put("ngh?", "nghệ");
        ENCODING_FIX_MAP.put("co b?n", "cơ bản");
        ENCODING_FIX_MAP.put("l?p", "lập");
        ENCODING_FIX_MAP.put("L?p", "Lập");
        ENCODING_FIX_MAP.put("Nguy?n", "Nguyễn");
        ENCODING_FIX_MAP.put("Tr?n", "Trần");
        ENCODING_FIX_MAP.put("Th?", "Thị");
        ENCODING_FIX_MAP.put("Ph?m", "Phạm");
        ENCODING_FIX_MAP.put("Van", "Văn");
        ENCODING_FIX_MAP.put("t?p", "tập");
        ENCODING_FIX_MAP.put("Bài t?p", "Bài tập");
        ENCODING_FIX_MAP.put("v?", "về");
        ENCODING_FIX_MAP.put("Ma tr?n", "Ma trận");
        ENCODING_FIX_MAP.put("D?nh", "Định");
        ENCODING_FIX_MAP.put("th?c", "thức");
        ENCODING_FIX_MAP.put("tác ph?m", "tác phẩm");
        ENCODING_FIX_MAP.put("tho", "thơ");
        ENCODING_FIX_MAP.put("H?", "Hồ");
        ENCODING_FIX_MAP.put("Ki?m", "Kiểm");
        ENCODING_FIX_MAP.put("gi?a", "giữa");
        ENCODING_FIX_MAP.put("k?", "kỳ");
        ENCODING_FIX_MAP.put("cu?i", "cuối");
        ENCODING_FIX_MAP.put("h?t", "hết");
        ENCODING_FIX_MAP.put("môn", "môn");
        ENCODING_FIX_MAP.put("V?n", "Văn");
        ENCODING_FIX_MAP.put("th?c", "thực");
        ENCODING_FIX_MAP.put("hành", "hành");
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🔧 Bắt đầu kiểm tra và sửa lỗi encoding tiếng Việt...");
        
        try {
            int fixedCount = 0;
            
            // Fix classroom names
            fixedCount += fixClassroomNames();
            
            // Fix user names
            fixedCount += fixUserNames();
            
            // Fix assignment titles and descriptions
            fixedCount += fixAssignmentData();
            
            // Fix submission comments and feedback
            fixedCount += fixSubmissionData();
            
            if (fixedCount > 0) {
                log.info("✅ Đã sửa {} lỗi encoding tiếng Việt", fixedCount);
            } else {
                log.info("✅ Không tìm thấy lỗi encoding nào cần sửa");
            }
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi sửa encoding tiếng Việt: {}", e.getMessage(), e);
        }
    }

    private int fixClassroomNames() {
        int fixedCount = 0;
        var classrooms = classroomRepository.findAll();
        
        for (var classroom : classrooms) {
            if (classroom.getName() != null) {
                String originalName = classroom.getName();
                String fixedName = fixVietnameseText(originalName);
                
                if (!originalName.equals(fixedName)) {
                    classroom.setName(fixedName);
                    classroomRepository.save(classroom);
                    fixedCount++;
                    log.info("📝 Sửa tên lớp: '{}' -> '{}'", originalName, fixedName);
                }
            }
            
            if (classroom.getDescription() != null) {
                String originalDesc = classroom.getDescription();
                String fixedDesc = fixVietnameseText(originalDesc);
                
                if (!originalDesc.equals(fixedDesc)) {
                    classroom.setDescription(fixedDesc);
                    classroomRepository.save(classroom);
                    fixedCount++;
                    log.info("📝 Sửa mô tả lớp: '{}' -> '{}'", originalDesc, fixedDesc);
                }
            }
        }
        
        return fixedCount;
    }

    private int fixUserNames() {
        int fixedCount = 0;
        var users = userRepository.findAll();
        
        for (var user : users) {
            if (user.getFullName() != null) {
                String originalName = user.getFullName();
                String fixedName = fixVietnameseText(originalName);
                
                if (!originalName.equals(fixedName)) {
                    user.setFullName(fixedName);
                    userRepository.save(user);
                    fixedCount++;
                    log.info("👤 Sửa tên người dùng: '{}' -> '{}'", originalName, fixedName);
                }
            }
        }
        
        return fixedCount;
    }

    private int fixAssignmentData() {
        int fixedCount = 0;
        var assignments = assignmentRepository.findAll();
        
        for (var assignment : assignments) {
            if (assignment.getTitle() != null) {
                String originalTitle = assignment.getTitle();
                String fixedTitle = fixVietnameseText(originalTitle);
                
                if (!originalTitle.equals(fixedTitle)) {
                    assignment.setTitle(fixedTitle);
                    assignmentRepository.save(assignment);
                    fixedCount++;
                    log.info("📚 Sửa tiêu đề bài tập: '{}' -> '{}'", originalTitle, fixedTitle);
                }
            }
            
            if (assignment.getDescription() != null) {
                String originalDesc = assignment.getDescription();
                String fixedDesc = fixVietnameseText(originalDesc);
                
                if (!originalDesc.equals(fixedDesc)) {
                    assignment.setDescription(fixedDesc);
                    assignmentRepository.save(assignment);
                    fixedCount++;
                    log.info("📚 Sửa mô tả bài tập: '{}' -> '{}'", originalDesc, fixedDesc);
                }
            }
        }
        
        return fixedCount;
    }

    private int fixSubmissionData() {
        int fixedCount = 0;
        var submissions = submissionRepository.findAll();
        
        for (var submission : submissions) {
            if (submission.getComment() != null) {
                String originalComment = submission.getComment();
                String fixedComment = fixVietnameseText(originalComment);
                
                if (!originalComment.equals(fixedComment)) {
                    submission.setComment(fixedComment);
                    submissionRepository.save(submission);
                    fixedCount++;
                    log.info("💬 Sửa comment bài nộp: '{}' -> '{}'", originalComment, fixedComment);
                }
            }
            
            if (submission.getFeedback() != null) {
                String originalFeedback = submission.getFeedback();
                String fixedFeedback = fixVietnameseText(originalFeedback);
                
                if (!originalFeedback.equals(fixedFeedback)) {
                    submission.setFeedback(fixedFeedback);
                    submissionRepository.save(submission);
                    fixedCount++;
                    log.info("📝 Sửa feedback bài nộp: '{}' -> '{}'", originalFeedback, fixedFeedback);
                }
            }
        }
        
        return fixedCount;
    }

    /**
     * Sửa text tiếng Việt bị lỗi encoding
     * 
     * @param text Text cần sửa
     * @return Text đã được sửa
     */
    public String fixVietnameseText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String fixedText = text;
        
        // Áp dụng các fix từ mapping
        for (Map.Entry<String, String> entry : ENCODING_FIX_MAP.entrySet()) {
            fixedText = fixedText.replace(entry.getKey(), entry.getValue());
        }
        
        // Sử dụng regex để fix các pattern phổ biến
        // Fix dấu hỏi chấm trong tiếng Việt
        fixedText = fixedText.replaceAll("\\b([A-Za-z]+)\\?([a-z]+)\\b", "$1ỏ$2");
        fixedText = fixedText.replaceAll("\\b([A-Za-z]+)\\?([A-Za-z]+)\\b", "$1ệ$2");
        
        return fixedText;
    }

    /**
     * Kiểm tra xem text có chứa ký tự tiếng Việt bị lỗi encoding không
     * 
     * @param text Text cần kiểm tra
     * @return true nếu có lỗi encoding
     */
    public boolean hasEncodingIssues(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Kiểm tra các pattern thường gặp
        return text.contains("?") && (
            text.contains("c?p") || 
            text.contains("h?c") || 
            text.contains("Vi?t") || 
            text.contains("ngh?") || 
            text.contains("t?p") ||
            text.contains("Nguy?n") ||
            text.contains("Tr?n") ||
            text.contains("Ph?m")
        );
    }

    /**
     * Validate Vietnamese text encoding
     * 
     * @param text Text to validate
     * @return Validation result
     */
    public ValidationResult validateVietnameseEncoding(String text) {
        if (text == null || text.isEmpty()) {
            return ValidationResult.builder()
                .isValid(true)
                .message("Text is null or empty")
                .build();
        }
        
        boolean hasIssues = hasEncodingIssues(text);
        String fixedText = hasIssues ? fixVietnameseText(text) : text;
        
        return ValidationResult.builder()
            .isValid(!hasIssues)
            .originalText(text)
            .fixedText(fixedText)
            .message(hasIssues ? "Text has encoding issues" : "Text encoding is valid")
            .build();
    }

    /**
     * Kết quả validation
     */
    public static class ValidationResult {
        private boolean isValid;
        private String originalText;
        private String fixedText;
        private String message;
        
        public static ValidationResultBuilder builder() {
            return new ValidationResultBuilder();
        }
        
        // Getters
        public boolean isValid() { return isValid; }
        public String getOriginalText() { return originalText; }
        public String getFixedText() { return fixedText; }
        public String getMessage() { return message; }
        
        // Builder pattern
        public static class ValidationResultBuilder {
            private boolean isValid;
            private String originalText;
            private String fixedText;
            private String message;
            
            public ValidationResultBuilder isValid(boolean isValid) {
                this.isValid = isValid;
                return this;
            }
            
            public ValidationResultBuilder originalText(String originalText) {
                this.originalText = originalText;
                return this;
            }
            
            public ValidationResultBuilder fixedText(String fixedText) {
                this.fixedText = fixedText;
                return this;
            }
            
            public ValidationResultBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public ValidationResult build() {
                ValidationResult result = new ValidationResult();
                result.isValid = this.isValid;
                result.originalText = this.originalText;
                result.fixedText = this.fixedText;
                result.message = this.message;
                return result;
            }
        }
    }
} 