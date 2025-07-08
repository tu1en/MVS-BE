package com.classroomapp.classroombackend.config;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Component để sửa lỗi encoding UTF-8 cho dữ liệu tiếng Việt
 * Chạy sau khi ứng dụng khởi động để kiểm tra và sửa dữ liệu bị lỗi encoding
 */
@Component
@Order(1000) // Chạy sau khi data loader hoàn thành
@Slf4j
public class UTF8EncodingFixer implements CommandLineRunner {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔧 Starting UTF-8 encoding verification and fix...");
        
        // Set system properties for UTF-8
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        
        // Fix existing feedback data
        fixSubmissionFeedbackEncoding();
        
        log.info("✅ UTF-8 encoding verification and fix completed.");
    }

    /**
     * Sửa lỗi encoding cho feedback trong submissions
     */
    private void fixSubmissionFeedbackEncoding() {
        log.info("🔍 Checking submission feedback encoding...");
        
        List<Submission> submissions = submissionRepository.findAll();
        int fixedCount = 0;
        
        for (Submission submission : submissions) {
            if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
                String originalFeedback = submission.getFeedback();
                String fixedFeedback = fixUTF8Encoding(originalFeedback);
                
                if (!originalFeedback.equals(fixedFeedback)) {
                    log.info("🔧 Fixing feedback encoding for submission ID: {}", submission.getId());
                    log.info("   Before: {}", originalFeedback);
                    log.info("   After:  {}", fixedFeedback);
                    
                    submission.setFeedback(fixedFeedback);
                    submissionRepository.save(submission);
                    fixedCount++;
                }
            }
        }
        
        log.info("✅ Fixed {} submission feedbacks with encoding issues", fixedCount);
    }

    /**
     * Sửa lỗi encoding UTF-8 cho chuỗi text
     * @param text Chuỗi cần sửa
     * @return Chuỗi đã sửa lỗi encoding
     */
    private String fixUTF8Encoding(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            // Kiểm tra xem text có chứa ký tự lỗi encoding không
            if (text.contains("?") || text.contains("�")) {
                // Thử decode/encode để sửa lỗi
                byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
                String fixed = new String(bytes, StandardCharsets.UTF_8);
                
                // Kiểm tra nếu kết quả có ý nghĩa hơn
                if (containsVietnameseCharacters(fixed)) {
                    return fixed;
                }
            }
            
            // Kiểm tra và sửa URL encoding issues
            if (text.contains("%")) {
                try {
                    String decoded = URLDecoder.decode(text, StandardCharsets.UTF_8.toString());
                    if (containsVietnameseCharacters(decoded)) {
                        return decoded;
                    }
                } catch (UnsupportedEncodingException e) {
                    // Ignore and continue
                }
            }
            
            // Nếu không có vấn đề, trả về text gốc
            return text;
            
        } catch (Exception e) {
            log.warn("Could not fix encoding for text: {}", text, e);
            return text;
        }
    }

    /**
     * Kiểm tra xem chuỗi có chứa ký tự tiếng Việt không
     */
    private boolean containsVietnameseCharacters(String text) {
        if (text == null) return false;
        
        String vietnamese = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ" +
                           "ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ";
        
        return text.chars().anyMatch(c -> vietnamese.indexOf(c) >= 0);
    }

    /**
     * Test encoding với text mẫu
     */
    public void testEncoding() {
        String[] testTexts = {
            "Làm rất tốt! Bài làm chi tiết và đầy đủ.",
            "Xuất sắc! Phân tích chính xác.",
            "Cần cải thiện thêm về phần kết luận."
        };
        
        log.info("🧪 Testing UTF-8 encoding...");
        for (String text : testTexts) {
            log.info("Original: {}", text);
            log.info("Fixed:    {}", fixUTF8Encoding(text));
            log.info("---");
        }
    }
}
