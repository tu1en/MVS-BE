package com.classroomapp.classroombackend.config.seed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.util.LoggingUtils;

/**
 * Comprehensive Data Verification Tool
 * Kiểm tra toàn diện tính toàn vẹn và nhất quán của dữ liệu trong hệ thống
 */
@Component
public class ComprehensiveDataVerifier {
    
    private static final Logger log = LoggerFactory.getLogger(ComprehensiveDataVerifier.class);
    
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private ClassroomEnrollmentRepository enrollmentRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private LectureRepository lectureRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private SubmissionRepository submissionRepository;
    
    private final List<DataIntegrityIssue> issues = new ArrayList<>();
    
    /**
     * Chạy kiểm tra toàn diện tính toàn vẹn dữ liệu
     */
    @Transactional(readOnly = true)
    public DataVerificationReport runComprehensiveVerification() {
        log.info(LoggingUtils.SEARCH + " ============== COMPREHENSIVE DATA VERIFICATION STARTED ==============");
        issues.clear();
        
        // 1. Kiểm tra entities cơ bản
        verifyBasicEntities();
        
        // 2. Kiểm tra referential integrity
        verifyReferentialIntegrity();
        
        // 3. Kiểm tra business logic constraints
        verifyBusinessLogicConstraints();
        
        // 4. Kiểm tra data consistency
        verifyDataConsistency();
        
        // 5. Kiểm tra orphaned records
        verifyOrphanedRecords();
        
        // 6. Tạo báo cáo
        DataVerificationReport report = generateReport();
        
        log.info("✅ ============== COMPREHENSIVE DATA VERIFICATION COMPLETED ==============");
        log.info("📊 Found {} issues: {} Critical, {} Warning, {} Info", 
            report.getTotalIssues(), 
            report.getCriticalIssues(), 
            report.getWarningIssues(), 
            report.getInfoIssues());
            
        return report;
    }
    
    /**
     * Kiểm tra các entities cơ bản
     */
    private void verifyBasicEntities() {
        log.info(LoggingUtils.SEARCH + " Verifying basic entities...");
        
        // Kiểm tra Users
        long userCount = userRepository.count();
        if (userCount == 0) {
            addIssue(IssueSeverity.CRITICAL, "USER_EMPTY", "No users found in database", null);
        } else {
            log.info(LoggingUtils.SUCCESS + " Found {} users", userCount);
            verifyUserRoles();
        }
        
        // Kiểm tra Courses
        long courseCount = courseRepository.count();
        if (courseCount == 0) {
            addIssue(IssueSeverity.WARNING, "COURSE_EMPTY", "No courses found in database", null);
        } else {
            log.info(LoggingUtils.SUCCESS + " Found {} courses", courseCount);
        }
        
        // Kiểm tra Classrooms
        long classroomCount = classroomRepository.count();
        if (classroomCount == 0) {
            addIssue(IssueSeverity.CRITICAL, "CLASSROOM_EMPTY", "No classrooms found in database", null);
        } else {
            log.info("✅ Found {} classrooms", classroomCount);
        }
    }
    
    /**
     * Kiểm tra user roles
     */
    private void verifyUserRoles() {
        Map<Integer, Long> roleDistribution = userRepository.findAll().stream()
            .collect(Collectors.groupingBy(User::getRoleId, Collectors.counting()));
            
        log.info("👥 User role distribution: {}", roleDistribution);
        
        // Kiểm tra có ít nhất 1 admin
        if (!roleDistribution.containsKey(4) || roleDistribution.get(4) == 0) {
            addIssue(IssueSeverity.WARNING, "NO_ADMIN", "No admin users found", null);
        }
        
        // Kiểm tra có ít nhất 1 teacher
        if (!roleDistribution.containsKey(2) || roleDistribution.get(2) == 0) {
            addIssue(IssueSeverity.CRITICAL, "NO_TEACHER", "No teacher users found", null);
        }
        
        // Kiểm tra có ít nhất 1 student
        if (!roleDistribution.containsKey(1) || roleDistribution.get(1) == 0) {
            addIssue(IssueSeverity.WARNING, "NO_STUDENT", "No student users found", null);
        }
    }
    
    /**
     * Kiểm tra referential integrity
     */
    private void verifyReferentialIntegrity() {
        log.info("🔍 Verifying referential integrity...");
        
        // Kiểm tra Classroom -> Teacher relationship
        List<Classroom> classroomsWithoutTeacher = classroomRepository.findAll().stream()
            .filter(c -> c.getTeacher() == null)
            .collect(Collectors.toList());
            
        if (!classroomsWithoutTeacher.isEmpty()) {
            addIssue(IssueSeverity.CRITICAL, "CLASSROOM_NO_TEACHER", 
                "Found classrooms without teachers", 
                "Classroom IDs: " + classroomsWithoutTeacher.stream()
                    .map(c -> c.getId() != null ? c.getId().toString() : "NULL").collect(Collectors.joining(", ")));
        }
        
        // Kiểm tra Classroom -> Course relationship (nếu có courseId)
        List<Classroom> classroomsWithInvalidCourse = classroomRepository.findAll().stream()
            .filter(c -> c.getCourseId() != null && !courseRepository.existsById(c.getCourseId()))
            .collect(Collectors.toList());
            
        if (!classroomsWithInvalidCourse.isEmpty()) {
            addIssue(IssueSeverity.CRITICAL, "CLASSROOM_INVALID_COURSE", 
                "Found classrooms with invalid course references", 
                "Classroom IDs: " + classroomsWithInvalidCourse.stream()
                    .map(c -> c.getId() != null ? c.getId().toString() : "NULL").collect(Collectors.joining(", ")));
        }
        
        // Kiểm tra Assignment -> Classroom relationship
        verifyAssignmentIntegrity();
        
        // Kiểm tra Submission -> Assignment và Student relationship
        verifySubmissionIntegrity();
        
        // Kiểm tra Enrollment integrity
        verifyEnrollmentIntegrity();
    }
    
    /**
     * Kiểm tra Assignment integrity
     */
    private void verifyAssignmentIntegrity() {
        List<Assignment> assignments = assignmentRepository.findAll();
        
        for (Assignment assignment : assignments) {
            // Kiểm tra classroom reference
            if (assignment.getClassroom() == null) {
                addIssue(IssueSeverity.CRITICAL, "ASSIGNMENT_NO_CLASSROOM", 
                    "Assignment without classroom", 
                    "Assignment ID: " + assignment.getId() + ", Title: " + assignment.getTitle());
            }
            
            // Kiểm tra due date
            if (assignment.getDueDate() == null) {
                addIssue(IssueSeverity.WARNING, "ASSIGNMENT_NO_DUE_DATE", 
                    "Assignment without due date", 
                    "Assignment ID: " + assignment.getId());
            }
        }
    }
    
    /**
     * Kiểm tra Submission integrity
     */
    private void verifySubmissionIntegrity() {
        List<Submission> submissions = submissionRepository.findAll();
        
        for (Submission submission : submissions) {
            // Kiểm tra assignment reference
            if (submission.getAssignment() == null) {
                addIssue(IssueSeverity.CRITICAL, "SUBMISSION_NO_ASSIGNMENT", 
                    "Submission without assignment", 
                    "Submission ID: " + submission.getId());
            }
            
            // Kiểm tra student reference
            if (submission.getStudent() == null) {
                addIssue(IssueSeverity.CRITICAL, "SUBMISSION_NO_STUDENT", 
                    "Submission without student", 
                    "Submission ID: " + submission.getId());
            }
            
            // Kiểm tra student có đúng role không
            if (submission.getStudent() != null && submission.getStudent().getRoleId() != 1) {
                addIssue(IssueSeverity.WARNING, "SUBMISSION_INVALID_STUDENT_ROLE", 
                    "Submission by non-student user", 
                    "Submission ID: " + submission.getId() + ", User role: " + submission.getStudent().getRoleId());
            }
        }
    }
    
    /**
     * Kiểm tra Enrollment integrity
     */
    private void verifyEnrollmentIntegrity() {
        List<ClassroomEnrollment> enrollments = enrollmentRepository.findAll();
        
        for (ClassroomEnrollment enrollment : enrollments) {
            // Kiểm tra classroom reference
            if (enrollment.getClassroom() == null) {
                addIssue(IssueSeverity.CRITICAL, "ENROLLMENT_NO_CLASSROOM", 
                    "Enrollment without classroom", 
                    "Enrollment ID: " + enrollment.getId());
            }
            
            // Kiểm tra user reference
            if (enrollment.getUser() == null) {
                addIssue(IssueSeverity.CRITICAL, "ENROLLMENT_NO_USER", 
                    "Enrollment without user", 
                    "Enrollment ID: " + enrollment.getId());
            }
        }
    }
    
    /**
     * Kiểm tra business logic constraints
     */
    private void verifyBusinessLogicConstraints() {
        log.info("🔍 Verifying business logic constraints...");
        
        // Kiểm tra teacher không được enroll vào classroom của chính mình như student
        verifyTeacherEnrollmentLogic();
        
        // Kiểm tra duplicate enrollments
        verifyDuplicateEnrollments();
        
        // Kiểm tra assignment deadlines
        verifyAssignmentDeadlines();
    }
    
    private void verifyTeacherEnrollmentLogic() {
        List<ClassroomEnrollment> teacherEnrollments = enrollmentRepository.findAll().stream()
            .filter(e -> e.getUser() != null && e.getClassroom() != null)
            .filter(e -> e.getClassroom().getTeacher() != null)
            .filter(e -> e.getUser().getId().equals(e.getClassroom().getTeacher().getId()))
            .collect(Collectors.toList());
            
        if (!teacherEnrollments.isEmpty()) {
            addIssue(IssueSeverity.WARNING, "TEACHER_ENROLLED_AS_STUDENT", 
                "Teachers enrolled as students in their own classrooms", 
                "Count: " + teacherEnrollments.size());
        }
    }
    
    private void verifyDuplicateEnrollments() {
        // Logic để kiểm tra duplicate enrollments sẽ được implement
        // Hiện tại unique constraint đã handle case này
    }
    
    private void verifyAssignmentDeadlines() {
        // Kiểm tra assignments có deadline trong quá khứ
        // Logic sẽ được implement
    }
    
    /**
     * Kiểm tra data consistency
     */
    private void verifyDataConsistency() {
        log.info("🔍 Verifying data consistency...");

        // Kiểm tra classroom names không trùng lặp
        verifyUniqueClassroomNames();

        // Kiểm tra user emails không trùng lặp
        verifyUniqueUserEmails();

        // Kiểm tra lecture dates hợp lý
        verifyLectureDateConsistency();

        // Kiểm tra assignment points hợp lý
        verifyAssignmentPointsConsistency();
    }

    private void verifyUniqueClassroomNames() {
        Map<String, Long> nameCount = classroomRepository.findAll().stream()
            .collect(Collectors.groupingBy(Classroom::getName, Collectors.counting()));

        nameCount.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .forEach(entry -> addIssue(IssueSeverity.WARNING, "DUPLICATE_CLASSROOM_NAME",
                "Duplicate classroom name found",
                "Name: '" + entry.getKey() + "', Count: " + entry.getValue()));
    }

    private void verifyUniqueUserEmails() {
        Map<String, Long> emailCount = userRepository.findAll().stream()
            .collect(Collectors.groupingBy(User::getEmail, Collectors.counting()));

        emailCount.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .forEach(entry -> addIssue(IssueSeverity.CRITICAL, "DUPLICATE_USER_EMAIL",
                "Duplicate user email found",
                "Email: '" + entry.getKey() + "', Count: " + entry.getValue()));
    }

    private void verifyLectureDateConsistency() {
        List<Lecture> lectures = lectureRepository.findAll();

        for (Lecture lecture : lectures) {
            if (lecture.getLectureDate() == null) {
                addIssue(IssueSeverity.WARNING, "LECTURE_NO_DATE",
                    "Lecture without date",
                    "Lecture ID: " + lecture.getId() + ", Title: " + lecture.getTitle());
            }
        }
    }

    private void verifyAssignmentPointsConsistency() {
        List<Assignment> assignments = assignmentRepository.findAll();

        for (Assignment assignment : assignments) {
            if (assignment.getPoints() == null || assignment.getPoints() < 0) {
                addIssue(IssueSeverity.WARNING, "ASSIGNMENT_INVALID_POINTS",
                    "Assignment with invalid points",
                    "Assignment ID: " + assignment.getId() + ", Points: " + assignment.getPoints());
            }

            if (assignment.getPoints() != null && assignment.getPoints() > 1000) {
                addIssue(IssueSeverity.INFO, "ASSIGNMENT_HIGH_POINTS",
                    "Assignment with unusually high points",
                    "Assignment ID: " + assignment.getId() + ", Points: " + assignment.getPoints());
            }
        }
    }

    /**
     * Kiểm tra orphaned records
     */
    private void verifyOrphanedRecords() {
        log.info("🔍 Verifying orphaned records...");

        // Kiểm tra lectures không có classroom
        verifyOrphanedLectures();

        // Kiểm tra schedules không có classroom hoặc teacher
        verifyOrphanedSchedules();

        // Kiểm tra submissions không có assignment hoặc student
        verifyOrphanedSubmissions();

        // Kiểm tra enrollments không có user hoặc classroom
        verifyOrphanedEnrollments();
    }

    private void verifyOrphanedLectures() {
        List<Lecture> orphanedLectures = lectureRepository.findAll().stream()
            .filter(lecture -> lecture.getClassroom() == null)
            .collect(Collectors.toList());

        if (!orphanedLectures.isEmpty()) {
            addIssue(IssueSeverity.CRITICAL, "ORPHANED_LECTURES",
                "Found lectures without classrooms",
                "Count: " + orphanedLectures.size() + ", IDs: " +
                orphanedLectures.stream().map(l -> l.getId() != null ? l.getId().toString() : "NULL").collect(Collectors.joining(", ")));
        }
    }

    private void verifyOrphanedSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();

        List<Schedule> orphanedSchedules = schedules.stream()
            .filter(schedule -> schedule.getClassroom() == null || schedule.getTeacher() == null)
            .collect(Collectors.toList());

        if (!orphanedSchedules.isEmpty()) {
            addIssue(IssueSeverity.CRITICAL, "ORPHANED_SCHEDULES",
                "Found schedules without classroom or teacher",
                "Count: " + orphanedSchedules.size());
        }
    }

    private void verifyOrphanedSubmissions() {
        List<Submission> orphanedSubmissions = submissionRepository.findAll().stream()
            .filter(submission -> submission.getAssignment() == null || submission.getStudent() == null)
            .collect(Collectors.toList());

        if (!orphanedSubmissions.isEmpty()) {
            addIssue(IssueSeverity.CRITICAL, "ORPHANED_SUBMISSIONS",
                "Found submissions without assignment or student",
                "Count: " + orphanedSubmissions.size());
        }
    }

    private void verifyOrphanedEnrollments() {
        List<ClassroomEnrollment> orphanedEnrollments = enrollmentRepository.findAll().stream()
            .filter(enrollment -> enrollment.getUser() == null || enrollment.getClassroom() == null)
            .collect(Collectors.toList());

        if (!orphanedEnrollments.isEmpty()) {
            addIssue(IssueSeverity.CRITICAL, "ORPHANED_ENROLLMENTS",
                "Found enrollments without user or classroom",
                "Count: " + orphanedEnrollments.size());
        }
    }
    
    /**
     * Thêm issue vào danh sách
     */
    private void addIssue(IssueSeverity severity, String code, String message, String details) {
        DataIntegrityIssue issue = new DataIntegrityIssue(severity, code, message, details);
        issues.add(issue);
        
        String logMessage = String.format("[%s] %s: %s", severity, code, message);
        if (details != null) {
            logMessage += " - " + details;
        }
        
        switch (severity) {
            case CRITICAL:
                log.error("❌ " + logMessage);
                break;
            case WARNING:
                log.warn("⚠️ " + logMessage);
                break;
            case INFO:
                log.info("ℹ️ " + logMessage);
                break;
        }
    }
    
    /**
     * Tạo báo cáo verification
     */
    private DataVerificationReport generateReport() {
        return new DataVerificationReport(new ArrayList<>(issues));
    }
}
