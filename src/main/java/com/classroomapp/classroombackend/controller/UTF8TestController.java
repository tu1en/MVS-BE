package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.config.UTF8EncodingFixer;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller để kiểm tra và sửa lỗi UTF-8 encoding
 */
@RestController
@RequestMapping("/api/admin/utf8")
@Slf4j
public class UTF8TestController {

    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private UTF8EncodingFixer utf8EncodingFixer;

    /**
     * Kiểm tra trạng thái encoding của submission feedback
     */
    @GetMapping("/check-submissions")
    public ResponseEntity<Map<String, Object>> checkSubmissionsEncoding() {
        log.info("Checking UTF-8 encoding for submissions...");
        
        List<Submission> submissions = submissionRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        
        int totalSubmissions = submissions.size();
        int withFeedback = 0;
        int problematicFeedback = 0;
        
        for (Submission submission : submissions) {
            if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
                withFeedback++;
                
                // Check for encoding issues
                String feedback = submission.getFeedback();
                if (feedback.contains("?") || feedback.contains("�") || 
                    hasEncodingIssues(feedback)) {
                    problematicFeedback++;
                    log.warn("Submission ID {} has encoding issues: {}", 
                            submission.getId(), feedback);
                }
            }
        }
        
        result.put("totalSubmissions", totalSubmissions);
        result.put("submissionsWithFeedback", withFeedback);
        result.put("problematicFeedback", problematicFeedback);
        result.put("encodingHealthy", problematicFeedback == 0);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Test encoding với text cụ thể
     */
    @GetMapping("/test-text")
    public ResponseEntity<Map<String, String>> testTextEncoding(
            @RequestParam String text) {
        
        Map<String, String> result = new HashMap<>();
        result.put("original", text);
        result.put("hasVietnameseChars", String.valueOf(containsVietnameseCharacters(text)));
        result.put("hasEncodingIssues", String.valueOf(hasEncodingIssues(text)));
        
        // Test different encodings
        try {
            byte[] iso88591Bytes = text.getBytes("ISO-8859-1");
            String utf8FromIso = new String(iso88591Bytes, "UTF-8");
            result.put("iso88591ToUtf8", utf8FromIso);
        } catch (Exception e) {
            result.put("iso88591ToUtf8", "Error: " + e.getMessage());
        }
        
        try {
            byte[] windowsBytes = text.getBytes("windows-1252");
            String utf8FromWindows = new String(windowsBytes, "UTF-8");
            result.put("windows1252ToUtf8", utf8FromWindows);
        } catch (Exception e) {
            result.put("windows1252ToUtf8", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Chạy test encoding
     */
    @PostMapping("/run-encoding-test")
    public ResponseEntity<String> runEncodingTest() {
        utf8EncodingFixer.testEncoding();
        return ResponseEntity.ok("Encoding test completed. Check logs for results.");
    }

    /**
     * Tạo dữ liệu test với tiếng Việt
     */
    @PostMapping("/create-test-feedback")
    public ResponseEntity<Map<String, Object>> createTestFeedback() {
        String[] vietnameseFeedbacks = {
            "Làm rất tốt! Bài làm chi tiết và đầy đủ.",
            "Xuất sắc! ERD thiết kế chính xác, SQL viết đúng chuẩn.",
            "Tốt! Bài làm đạt yêu cầu, thiết kế CSDL hợp lý.",
            "Khá! Nắm được kiến thức cơ bản về thiết kế CSDL.",
            "Cần cải thiện! Bài làm chưa đạt yêu cầu tối thiểu."
        };
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Created test feedback data");
        result.put("testFeedbacks", vietnameseFeedbacks);
        
        // Log the feedbacks to check console encoding
        for (String feedback : vietnameseFeedbacks) {
            log.info("Test feedback: {}", feedback);
        }
        
        return ResponseEntity.ok(result);
    }

    private boolean hasEncodingIssues(String text) {
        if (text == null) return false;
        
        // Common signs of encoding issues
        return text.contains("?") || 
               text.contains("�") || 
               text.contains("??t") || // "rất" becomes "r??t"
               text.contains("??ng") || // "đúng" becomes "??ng" 
               text.contains("??y") ||  // "đầy" becomes "??y"
               text.contains("?i?t");   // "điểm" becomes "?i?t"
    }

    private boolean containsVietnameseCharacters(String text) {
        if (text == null) return false;
        
        String vietnamese = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ" +
                           "ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ";
        
        return text.chars().anyMatch(c -> vietnamese.indexOf(c) >= 0);
    }
}
