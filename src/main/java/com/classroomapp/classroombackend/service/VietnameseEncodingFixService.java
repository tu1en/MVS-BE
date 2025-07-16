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

    // Mapping cá»§a cÃ¡c kÃ½ tá»± bá»‹ lá»—i encoding thÆ°á»ng gáº·p
    private static final Map<String, String> ENCODING_FIX_MAP = new HashMap<>();
    
    static {
        // CÃ¡c kÃ½ tá»± tiáº¿ng Viá»‡t thÆ°á»ng bá»‹ lá»—i
        ENCODING_FIX_MAP.put("ÃƒÂ¡", "Ã¡");
        ENCODING_FIX_MAP.put("Ãƒ ", "Ã ");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ£", "áº£");
        ENCODING_FIX_MAP.put("ÃƒÂ£", "Ã£");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ¡", "áº¡");
        ENCODING_FIX_MAP.put("ÃƒÂ¢", "Ã¢");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ¥", "áº¥");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ§", "áº§");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ©", "áº©");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ«", "áº«");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ­", "áº­");
        ENCODING_FIX_MAP.put("Ã„", "Äƒ");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ¯", "áº¯");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ±", "áº±");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ³", "áº³");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ¡", "áº¡");
        ENCODING_FIX_MAP.put("ÃƒÂ©", "Ã©");
        ENCODING_FIX_MAP.put("ÃƒÂ¨", "Ã¨");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ»", "áº»");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ½", "áº½");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ¹", "áº¹");
        ENCODING_FIX_MAP.put("ÃƒÂª", "Ãª");
        ENCODING_FIX_MAP.put("Ã¡ÂºÂ¿", "áº¿");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»ƒ");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»…");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»‡");
        ENCODING_FIX_MAP.put("ÃƒÂ­", "Ã­");
        ENCODING_FIX_MAP.put("ÃƒÂ¬", "Ã¬");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»‰");
        ENCODING_FIX_MAP.put("Ã„Â©", "Ä©");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»‹");
        ENCODING_FIX_MAP.put("ÃƒÂ³", "Ã³");
        ENCODING_FIX_MAP.put("ÃƒÂ²", "Ã²");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»");
        ENCODING_FIX_MAP.put("ÃƒÂµ", "Ãµ");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»");
        ENCODING_FIX_MAP.put("ÃƒÂ´", "Ã´");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»‘");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»“");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»•");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»—");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»™");
        ENCODING_FIX_MAP.put("Ã†Â¡", "Æ¡");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»›");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»Ÿ");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»¡");
        ENCODING_FIX_MAP.put("Ã¡Â»Â£", "á»£");
        ENCODING_FIX_MAP.put("ÃƒÂº", "Ãº");
        ENCODING_FIX_MAP.put("ÃƒÂ¹", "Ã¹");
        ENCODING_FIX_MAP.put("Ã¡Â»Â§", "á»§");
        ENCODING_FIX_MAP.put("Ã…Â©", "Å©");
        ENCODING_FIX_MAP.put("Ã¡Â»Â¥", "á»¥");
        ENCODING_FIX_MAP.put("Ã†Â°", "Æ°");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»©");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»«");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»­");
        ENCODING_FIX_MAP.put("Ã¡Â»", "á»¯");
        ENCODING_FIX_MAP.put("Ã¡Â»Â±", "á»±");
        ENCODING_FIX_MAP.put("ÃƒÂ½", "Ã½");
        ENCODING_FIX_MAP.put("Ã¡Â»Â³", "á»³");
        ENCODING_FIX_MAP.put("Ã¡Â»Â·", "á»·");
        ENCODING_FIX_MAP.put("Ã¡Â»Â¹", "á»¹");
        ENCODING_FIX_MAP.put("Ã¡Â»Âµ", "á»µ");
        ENCODING_FIX_MAP.put("Ã„", "Ä‘");
        
        // CÃ¡c pattern phá»• biáº¿n bá»‹ lá»—i
        ENCODING_FIX_MAP.put("c?p", "cáº¥p");
        ENCODING_FIX_MAP.put("h?c", "há»c");
        ENCODING_FIX_MAP.put("Vi?t", "Viá»‡t");
        ENCODING_FIX_MAP.put("ti?ng", "tiáº¿ng");
        ENCODING_FIX_MAP.put("Ti?ng", "Tiáº¿ng");
        ENCODING_FIX_MAP.put("ngh?", "nghá»‡");
        ENCODING_FIX_MAP.put("co b?n", "cÆ¡ báº£n");
        ENCODING_FIX_MAP.put("l?p", "láº­p");
        ENCODING_FIX_MAP.put("L?p", "Láº­p");
        ENCODING_FIX_MAP.put("Nguy?n", "Nguyá»…n");
        ENCODING_FIX_MAP.put("Tr?n", "Tráº§n");
        ENCODING_FIX_MAP.put("Th?", "Thá»‹");
        ENCODING_FIX_MAP.put("Ph?m", "Pháº¡m");
        ENCODING_FIX_MAP.put("Van", "VÄƒn");
        ENCODING_FIX_MAP.put("t?p", "táº­p");
        ENCODING_FIX_MAP.put("BÃ i t?p", "BÃ i táº­p");
        ENCODING_FIX_MAP.put("v?", "vá»");
        ENCODING_FIX_MAP.put("Ma tr?n", "Ma tráº­n");
        ENCODING_FIX_MAP.put("D?nh", "Äá»‹nh");
        ENCODING_FIX_MAP.put("th?c", "thá»©c");
        ENCODING_FIX_MAP.put("tÃ¡c ph?m", "tÃ¡c pháº©m");
        ENCODING_FIX_MAP.put("tho", "thÆ¡");
        ENCODING_FIX_MAP.put("H?", "Há»“");
        ENCODING_FIX_MAP.put("Ki?m", "Kiá»ƒm");
        ENCODING_FIX_MAP.put("gi?a", "giá»¯a");
        ENCODING_FIX_MAP.put("k?", "ká»³");
        ENCODING_FIX_MAP.put("cu?i", "cuá»‘i");
        ENCODING_FIX_MAP.put("h?t", "háº¿t");
        ENCODING_FIX_MAP.put("mÃ´n", "mÃ´n");
        ENCODING_FIX_MAP.put("V?n", "VÄƒn");
        ENCODING_FIX_MAP.put("th?c", "thá»±c");
        ENCODING_FIX_MAP.put("hÃ nh", "hÃ nh");
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("ðŸ”§ Báº¯t Ä‘áº§u kiá»ƒm tra vÃ  sá»­a lá»—i encoding tiáº¿ng Viá»‡t...");
        
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
                log.info("âœ… ÄÃ£ sá»­a {} lá»—i encoding tiáº¿ng Viá»‡t", fixedCount);
            } else {
                log.info("âœ… KhÃ´ng tÃ¬m tháº¥y lá»—i encoding nÃ o cáº§n sá»­a");
            }
            
        } catch (Exception e) {
            log.error("âŒ Lá»—i khi sá»­a encoding tiáº¿ng Viá»‡t: {}", e.getMessage(), e);
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
                    log.info("ðŸ“ Sá»­a tÃªn lá»›p: '{}' -> '{}'", originalName, fixedName);
                }
            }
            
            if (classroom.getDescription() != null) {
                String originalDesc = classroom.getDescription();
                String fixedDesc = fixVietnameseText(originalDesc);
                
                if (!originalDesc.equals(fixedDesc)) {
                    classroom.setDescription(fixedDesc);
                    classroomRepository.save(classroom);
                    fixedCount++;
                    log.info("ðŸ“ Sá»­a mÃ´ táº£ lá»›p: '{}' -> '{}'", originalDesc, fixedDesc);
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
                    log.info("ðŸ‘¤ Sá»­a tÃªn ngÆ°á»i dÃ¹ng: '{}' -> '{}'", originalName, fixedName);
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
                    log.info("ðŸ“š Sá»­a tiÃªu Ä‘á» bÃ i táº­p: '{}' -> '{}'", originalTitle, fixedTitle);
                }
            }
            
            if (assignment.getDescription() != null) {
                String originalDesc = assignment.getDescription();
                String fixedDesc = fixVietnameseText(originalDesc);
                
                if (!originalDesc.equals(fixedDesc)) {
                    assignment.setDescription(fixedDesc);
                    assignmentRepository.save(assignment);
                    fixedCount++;
                    log.info("ðŸ“š Sá»­a mÃ´ táº£ bÃ i táº­p: '{}' -> '{}'", originalDesc, fixedDesc);
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
                    log.info("ðŸ’¬ Sá»­a comment bÃ i ná»™p: '{}' -> '{}'", originalComment, fixedComment);
                }
            }
            
            if (submission.getFeedback() != null) {
                String originalFeedback = submission.getFeedback();
                String fixedFeedback = fixVietnameseText(originalFeedback);
                
                if (!originalFeedback.equals(fixedFeedback)) {
                    submission.setFeedback(fixedFeedback);
                    submissionRepository.save(submission);
                    fixedCount++;
                    log.info("ðŸ“ Sá»­a feedback bÃ i ná»™p: '{}' -> '{}'", originalFeedback, fixedFeedback);
                }
            }
        }
        
        return fixedCount;
    }

    /**
     * Sá»­a text tiáº¿ng Viá»‡t bá»‹ lá»—i encoding
     * 
     * @param text Text cáº§n sá»­a
     * @return Text Ä‘Ã£ Ä‘Æ°á»£c sá»­a
     */
    public String fixVietnameseText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String fixedText = text;
        
        // Ãp dá»¥ng cÃ¡c fix tá»« mapping
        for (Map.Entry<String, String> entry : ENCODING_FIX_MAP.entrySet()) {
            fixedText = fixedText.replace(entry.getKey(), entry.getValue());
        }
        
        // Sá»­ dá»¥ng regex Ä‘á»ƒ fix cÃ¡c pattern phá»• biáº¿n
        // Fix dáº¥u há»i cháº¥m trong tiáº¿ng Viá»‡t
        fixedText = fixedText.replaceAll("\\b([A-Za-z]+)\\?([a-z]+)\\b", "$1á»$2");
        fixedText = fixedText.replaceAll("\\b([A-Za-z]+)\\?([A-Za-z]+)\\b", "$1á»‡$2");
        
        return fixedText;
    }

    /**
     * Kiá»ƒm tra xem text cÃ³ chá»©a kÃ½ tá»± tiáº¿ng Viá»‡t bá»‹ lá»—i encoding khÃ´ng
     * 
     * @param text Text cáº§n kiá»ƒm tra
     * @return true náº¿u cÃ³ lá»—i encoding
     */
    public boolean hasEncodingIssues(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Kiá»ƒm tra cÃ¡c pattern thÆ°á»ng gáº·p
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
     * Káº¿t quáº£ validation
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