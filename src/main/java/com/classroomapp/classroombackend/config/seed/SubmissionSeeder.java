package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.assignmentmanagement.SubmissionAttachment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class SubmissionSeeder {
    private static final Logger log = LoggerFactory.getLogger(SubmissionSeeder.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;

    private static final String SAMPLE_PDF_URL = "/static/sample_materials/sample.pdf";

    @Transactional
    public void seed() {
        log.info("🔧 Running SubmissionSeeder with ENROLLMENT VALIDATION to ensure test submissions exist");
        log.info("🛡️ FIXED: Now checking enrollment before creating submissions to prevent data inconsistency");

        List<Assignment> assignments = assignmentRepository.findAll();
        List<User> students = userRepository.findAllByRoleId(1L); // Role ID 1 for STUDENT

        if (assignments.isEmpty() || students.isEmpty()) {
            log.warn("⚠️ [SubmissionSeeder] No assignments or students found, skipping submission creation.");
            return;
        }

        log.info("Found {} assignments and {} students for seeding submissions", 
                assignments.size(), students.size());

        // First check for specific assignment ID 87 which is used in the demo
        Optional<Assignment> assignment87 = assignmentRepository.findById(87L);
        if (assignment87.isPresent()) {
            log.info("Found specific assignment ID 87 - creating test submissions for it");
            Assignment assignment = assignment87.get();
            
            // Create submissions for all students for this assignment
            for (User student : students) {
                createSubmissionIfNotExists(assignment, student);
            }
        } else {
            log.warn("Assignment ID 87 not found - continuing with regular seeding");
        }

        // Seed submissions for all assignments for the first 3 students
        int submissionsCreated = 0;
        for (Assignment assignment : assignments) {
            for (int j = 0; j < Math.min(students.size(), 3); j++) {
                User student = students.get(j);
                
                if (createSubmissionIfNotExists(assignment, student)) {
                    submissionsCreated++;
                }
            }
        }

        if (submissionsCreated > 0) {
            log.info("✅ [SubmissionSeeder] Created {} new sample submissions.", submissionsCreated);
        } else {
            log.info("ℹ️ [SubmissionSeeder] No new submissions were created. Data may already exist.");
        }
        
        // Log count for specific assignment ID 87
        if (assignment87.isPresent()) {
            List<Submission> submissions87 = submissionRepository.findByAssignmentId(87L);
            log.info("Final submission count for assignment ID 87: {}", submissions87.size());
        }
    }
    
    /**
     * Creates a submission for the assignment and student if one doesn't already exist
     * AND the student is enrolled in the classroom
     * @param assignment Assignment to create submission for
     * @param student Student to create submission for
     * @return true if a new submission was created, false if one already existed or student not enrolled
     */
    private boolean createSubmissionIfNotExists(Assignment assignment, User student) {
        // CRITICAL FIX: Check if student is enrolled in the classroom first
        Long classroomId = assignment.getClassroom().getId();
        boolean isEnrolled = classroomEnrollmentRepository.isStudentEnrolledInClassroom(classroomId, student.getId());

        if (!isEnrolled) {
            log.debug("Skipping submission creation for assignment {} and student {} - student not enrolled in classroom {}",
                     assignment.getId(), student.getId(), classroomId);
            return false;
        }

        // Check if a submission already exists to avoid duplicates
        List<Submission> existingSubmissions = submissionRepository.findByStudentAndAssignment(student, assignment);
        if (existingSubmissions.isEmpty()) {
            int studentIndex = student.getFullName().hashCode() % 3; // Deterministic variation
            createSubmission(assignment, student, studentIndex);
            log.debug("Created submission for assignment {} and student {} (enrolled in classroom {})",
                     assignment.getId(), student.getId(), classroomId);
            return true;
        } else {
            log.debug("Submission already exists for assignment {} and student {} (found {} submissions)",
                     assignment.getId(), student.getId(), existingSubmissions.size());
            return false;
        }
    }

    private void createSubmission(Assignment assignment, User student, int studentIndex) {
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setSubmittedAt(LocalDateTime.now().minusDays(studentIndex));

        // Create and add an attachment
        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setFileName("sample_submission.pdf");
        attachment.setFileUrl(SAMPLE_PDF_URL);
        submission.addAttachment(attachment);

        // Set different data based on student index
        switch (studentIndex % 3) {
            case 0: // First student variation
                submission.setComment("Đây là bài nộp của tôi cho bài tập " + assignment.getTitle());
                submission.setScore(95);
                submission.setFeedback("Làm rất tốt! Bài làm chi tiết và đầy đủ.");
                break;
            case 1: // Second student variation
                submission.setComment("Em đã hoàn thành bài tập theo yêu cầu.");
                submission.setScore(82);
                submission.setFeedback("Bài làm khá tốt, cần chú ý hơn đến phần định dạng.");
                break;
            case 2: // Third student variation - ungraded
                submission.setComment("Bài nộp cho " + assignment.getTitle());
                // No score or feedback yet
                break;
        }
        
        submissionRepository.save(submission);
    }
} 