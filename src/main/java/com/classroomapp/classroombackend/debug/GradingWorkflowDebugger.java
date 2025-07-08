package com.classroomapp.classroombackend.debug;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;

@Component
public class GradingWorkflowDebugger {

    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;

    public void generateGradingReport(Long classroomId) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 BÁO CÁO TÌNH TRẠNG CHẤM ĐIỂM - CLASSROOM " + classroomId);
        System.out.println("=".repeat(80));
        System.out.println("⏰ Thời gian tạo báo cáo: " + LocalDateTime.now());
        System.out.println();

        List<Assignment> assignments = assignmentRepository.findByClassroomId(classroomId);
        
        if (assignments.isEmpty()) {
            System.out.println("⚠️  Không có assignment nào trong classroom này!");
            return;
        }

        for (Assignment assignment : assignments) {
            generateAssignmentReport(assignment);
        }

        generateSummaryReport(classroomId);
    }

    private void generateAssignmentReport(Assignment assignment) {
        System.out.println("📝 ASSIGNMENT: " + assignment.getTitle());
        System.out.println("   ├── ID: " + assignment.getId());
        System.out.println("   ├── Due Date: " + assignment.getDueDate());
        System.out.println("   ├── Points: " + assignment.getPoints());
        
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignment.getId());
        
        if (submissions.isEmpty()) {
            System.out.println("   └── ❌ Chưa có sinh viên nào nộp bài");
            System.out.println();
            return;
        }

        // Thống kê theo trạng thái
        Map<String, Long> statusCount = submissions.stream()
            .collect(Collectors.groupingBy(this::getSubmissionStatus, Collectors.counting()));

        System.out.println("   ├── 📊 THỐNG KÊ SUBMISSIONS:");
        System.out.println("   │   ├── Tổng số bài nộp: " + submissions.size());
        System.out.println("   │   ├── Đã chấm điểm: " + statusCount.getOrDefault("GRADED", 0L));
        System.out.println("   │   ├── Chờ chấm điểm: " + statusCount.getOrDefault("SUBMITTED", 0L));
        System.out.println("   │   ├── Nộp muộn: " + statusCount.getOrDefault("LATE", 0L));
        System.out.println("   │   └── Chưa nộp: " + statusCount.getOrDefault("NOT_SUBMITTED", 0L));

        // Thống kê điểm số
        List<Submission> gradedSubmissions = submissions.stream()
            .filter(s -> s.getScore() != null)
            .collect(Collectors.toList());

        if (!gradedSubmissions.isEmpty()) {
            double avgGrade = gradedSubmissions.stream()
                .mapToDouble(Submission::getScore)
                .average().orElse(0.0);
            
            double maxGrade = gradedSubmissions.stream()
                .mapToDouble(Submission::getScore)
                .max().orElse(0.0);
            
            double minGrade = gradedSubmissions.stream()
                .mapToDouble(Submission::getScore)
                .min().orElse(0.0);

            System.out.println("   ├── 📈 THỐNG KÊ ĐIỂM SỐ:");
            System.out.println("   │   ├── Điểm trung bình: " + String.format("%.2f", avgGrade));
            System.out.println("   │   ├── Điểm cao nhất: " + String.format("%.2f", maxGrade));
            System.out.println("   │   └── Điểm thấp nhất: " + String.format("%.2f", minGrade));
        }

        // Chi tiết từng submission
        System.out.println("   └── 📋 CHI TIẾT SUBMISSIONS:");
        for (int i = 0; i < Math.min(submissions.size(), 5); i++) {
            Submission sub = submissions.get(i);
            String prefix = (i == Math.min(submissions.size(), 5) - 1) ? "       └── " : "       ├── ";
            
            System.out.printf("%s[%d] %s - %s", 
                prefix, 
                sub.getId(), 
                sub.getStudent().getFullName(),
                getSubmissionStatus(sub));
            
            if (sub.getScore() != null) {
                System.out.printf(" (%.1f điểm)", sub.getScore().doubleValue());
            }
            System.out.println();
        }
        
        if (submissions.size() > 5) {
            System.out.println("       └── ... và " + (submissions.size() - 5) + " submissions khác");
        }
        
        System.out.println();
    }

    private void generateSummaryReport(Long classroomId) {
        System.out.println("🔢 TỔNG KẾT CLASSROOM " + classroomId);
        System.out.println("-".repeat(50));

        List<Assignment> assignments = assignmentRepository.findByClassroomId(classroomId);
        List<Submission> allSubmissions = assignments.stream()
            .flatMap(a -> submissionRepository.findByAssignmentId(a.getId()).stream())
            .collect(Collectors.toList());

        int totalAssignments = assignments.size();
        int totalSubmissions = allSubmissions.size();
        long gradedSubmissions = allSubmissions.stream()
            .filter(s -> s.getScore() != null)
            .count();
        long pendingSubmissions = allSubmissions.stream()
            .filter(s -> s.getScore() == null)
            .count();

        System.out.println("📚 Tổng số assignments: " + totalAssignments);
        System.out.println("📝 Tổng số submissions: " + totalSubmissions);
        System.out.println("✅ Đã chấm điểm: " + gradedSubmissions);
        System.out.println("⏳ Chờ chấm điểm: " + pendingSubmissions);
        
        if (totalSubmissions > 0) {
            double gradingProgress = (double) gradedSubmissions / totalSubmissions * 100;
            System.out.printf("📊 Tiến độ chấm điểm: %.1f%%\n", gradingProgress);
        }

        System.out.println("-".repeat(50));
        System.out.println("✨ Sẵn sàng test grading workflow tại:");
        System.out.println("🌐 http://localhost:3000/teacher/courses/" + classroomId + "/assignments");
        System.out.println("=".repeat(80));
    }

    public void debugSubmissionDetails(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            System.out.println("❌ Không tìm thấy submission với ID: " + submissionId);
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔍 CHI TIẾT SUBMISSION #" + submissionId);
        System.out.println("=".repeat(60));
        System.out.println("👤 Sinh viên: " + submission.getStudent().getFullName());
        System.out.println("📧 Email: " + submission.getStudent().getEmail());
        System.out.println("📝 Assignment: " + submission.getAssignment().getTitle());
        System.out.println("📅 Ngày nộp: " + submission.getSubmittedAt());
        System.out.println("📊 Trạng thái: " + getSubmissionStatus(submission));
        
        if (submission.getScore() != null) {
            System.out.println("🎯 Điểm: " + submission.getScore());
            System.out.println("📅 Ngày chấm: " + submission.getGradedAt());
        }
        
        if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
            System.out.println("💬 Feedback:");
            System.out.println("   " + submission.getFeedback().replace("\n", "\n   "));
            
            // Check for encoding issues
            if (hasEncodingIssues(submission.getFeedback())) {
                System.out.println("⚠️  WARNING: Feedback có thể có vấn đề về encoding UTF-8!");
                System.out.println("   Raw feedback: " + submission.getFeedback());
            }
        }
        
        if (submission.getComment() != null && !submission.getComment().isEmpty()) {
            System.out.println("📄 Comment từ sinh viên:");
            String preview = submission.getComment().substring(0, 
                Math.min(200, submission.getComment().length()));
            System.out.println("   " + preview.replace("\n", "\n   ") + "...");
        }
        
        System.out.println("=".repeat(60));
    }

    public void validateGradingWorkflow() {
        System.out.println("\n🔧 KIỂM TRA GRADING WORKFLOW");
        System.out.println("-".repeat(40));
        
        // Test 1: Kiểm tra assignments có submissions
        List<Assignment> assignmentsWithSubmissions = assignmentRepository.findAll().stream()
            .filter(a -> !submissionRepository.findByAssignmentId(a.getId()).isEmpty())
            .collect(Collectors.toList());
        
        System.out.println("✅ Assignments có submissions: " + assignmentsWithSubmissions.size());
        
        // Test 2: Kiểm tra submissions chờ chấm điểm
        long pendingSubmissions = submissionRepository.findAll().stream()
            .filter(s -> s.getScore() == null)
            .count();
        
        System.out.println("⏳ Submissions chờ chấm điểm: " + pendingSubmissions);
        
        // Test 3: Kiểm tra graded submissions
        long gradedSubmissions = submissionRepository.findAll().stream()
            .filter(s -> s.getScore() != null)
            .count();
        
        System.out.println("✅ Submissions đã chấm điểm: " + gradedSubmissions);
        
        // Test 4: Kiểm tra UTF-8 encoding
        long feedbackWithEncodingIssues = submissionRepository.findAll().stream()
            .filter(s -> s.getFeedback() != null)
            .filter(s -> hasEncodingIssues(s.getFeedback()))
            .count();
        
        System.out.println("⚠️  Feedback có vấn đề encoding: " + feedbackWithEncodingIssues);
        
        System.out.println("-".repeat(40));
        
        if (pendingSubmissions > 0) {
            System.out.println("🎯 Sẵn sàng test grading workflow!");
            System.out.println("📝 Có " + pendingSubmissions + " submissions chờ được chấm điểm");
        } else {
            System.out.println("⚠️  Cần tạo thêm submissions để test grading workflow");
        }
        
        if (feedbackWithEncodingIssues > 0) {
            System.out.println("🔧 Có " + feedbackWithEncodingIssues + " feedback cần sửa lỗi encoding");
            System.out.println("   Chạy endpoint: GET /api/admin/utf8/check-submissions để kiểm tra chi tiết");
        }
    }

    /**
     * Lấy status của submission
     */
    private String getSubmissionStatus(Submission submission) {
        if (submission.getScore() != null) {
            return "GRADED";
        }
        
        if (submission.getSubmittedAt() != null) {
            // Check if submitted after due date
            if (submission.getAssignment().getDueDate() != null && 
                submission.getSubmittedAt().isAfter(submission.getAssignment().getDueDate())) {
                return "LATE";
            }
            return "SUBMITTED";
        }
        
        return "NOT_SUBMITTED";
    }

    /**
     * Kiểm tra xem feedback có vấn đề encoding không
     */
    private boolean hasEncodingIssues(String feedback) {
        if (feedback == null) return false;
        
        // Common signs of encoding issues in Vietnamese text
        return feedback.contains("?") || 
               feedback.contains("�") || 
               feedback.contains("??t") || // "rất" becomes "r??t"
               feedback.contains("??ng") || // "đúng" becomes "??ng" 
               feedback.contains("??y") ||  // "đầy" becomes "??y"
               feedback.contains("?i?t");   // "điểm" becomes "?i?t"
    }
}
