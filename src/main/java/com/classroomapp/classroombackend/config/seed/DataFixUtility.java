package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Utility để sửa các vấn đề dữ liệu đã được phát hiện
 * Cung cấp các method để fix specific data issues
 */
@Component
public class DataFixUtility {
    
    private static final Logger log = LoggerFactory.getLogger(DataFixUtility.class);
    
    @Autowired private UserRepository userRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private ClassroomEnrollmentRepository enrollmentRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private LectureRepository lectureRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    
    /**
     * Fix tất cả các vấn đề dữ liệu đã biết
     */
    @Transactional
    public DataFixReport fixAllKnownIssues() {
        log.info("🔧 ============== STARTING DATA FIX PROCESS ==============");
        
        DataFixReport report = new DataFixReport();
        
        try {
            // Fix orphaned records
            report.addResult("orphaned_submissions", fixOrphanedSubmissions());
            report.addResult("orphaned_enrollments", fixOrphanedEnrollments());
            
            // Fix data consistency issues
            report.addResult("duplicate_emails", fixDuplicateUserEmails());
            report.addResult("invalid_assignment_points", fixInvalidAssignmentPoints());
            report.addResult("lecture_missing_dates", fixLectureMissingDates());

            // Fix referential integrity
            report.addResult("classroom_teacher_references", fixClassroomTeacherReferences());
            
            log.info("✅ ============== DATA FIX PROCESS COMPLETED ==============");
            log.info("📊 Fix Summary: {}", report.getSummary());
            
        } catch (Exception e) {
            log.error("❌ Data fix process failed: {}", e.getMessage(), e);
            report.addError("general_error", e.getMessage());
        }
        
        return report;
    }
    
    /**
     * Xóa orphaned submissions (submissions không có assignment hoặc student)
     */
    @Transactional
    public int fixOrphanedSubmissions() {
        log.info("🔧 Fixing orphaned submissions...");
        
        List<Submission> orphanedSubmissions = submissionRepository.findAll().stream()
            .filter(s -> s.getAssignment() == null || s.getStudent() == null)
            .collect(Collectors.toList());
            
        if (orphanedSubmissions.isEmpty()) {
            log.info("✅ No orphaned submissions found");
            return 0;
        }
        
        log.warn("⚠️ Found {} orphaned submissions, deleting...", orphanedSubmissions.size());
        
        for (Submission submission : orphanedSubmissions) {
            log.debug("Deleting orphaned submission ID: {}", submission.getId());
            submissionRepository.delete(submission);
        }
        
        log.info("✅ Deleted {} orphaned submissions", orphanedSubmissions.size());
        return orphanedSubmissions.size();
    }
    
    /**
     * Xóa orphaned enrollments (enrollments không có user hoặc classroom)
     */
    @Transactional
    public int fixOrphanedEnrollments() {
        log.info("🔧 Fixing orphaned enrollments...");
        
        List<ClassroomEnrollment> orphanedEnrollments = enrollmentRepository.findAll().stream()
            .filter(e -> e.getUser() == null || e.getClassroom() == null)
            .collect(Collectors.toList());
            
        if (orphanedEnrollments.isEmpty()) {
            log.info("✅ No orphaned enrollments found");
            return 0;
        }
        
        log.warn("⚠️ Found {} orphaned enrollments, deleting...", orphanedEnrollments.size());
        
        for (ClassroomEnrollment enrollment : orphanedEnrollments) {
            log.debug("Deleting orphaned enrollment ID: {}", enrollment.getId());
            enrollmentRepository.delete(enrollment);
        }
        
        log.info("✅ Deleted {} orphaned enrollments", orphanedEnrollments.size());
        return orphanedEnrollments.size();
    }
    
    /**
     * Fix duplicate user emails bằng cách thêm suffix
     */
    @Transactional
    public int fixDuplicateUserEmails() {
        log.info("🔧 Fixing duplicate user emails...");
        
        List<User> allUsers = userRepository.findAll();
        int fixedCount = 0;
        
        // Group by email để tìm duplicates
        var emailGroups = allUsers.stream()
            .collect(Collectors.groupingBy(User::getEmail));
            
        for (var entry : emailGroups.entrySet()) {
            String email = entry.getKey();
            List<User> usersWithSameEmail = entry.getValue();
            
            if (usersWithSameEmail.size() > 1) {
                log.warn("⚠️ Found {} users with duplicate email: {}", usersWithSameEmail.size(), email);
                
                // Giữ user đầu tiên, fix các user còn lại
                for (int i = 1; i < usersWithSameEmail.size(); i++) {
                    User user = usersWithSameEmail.get(i);
                    String newEmail = generateUniqueEmail(email, i);
                    
                    log.info("Changing email for user ID {}: {} -> {}", 
                        user.getId(), user.getEmail(), newEmail);
                    
                    user.setEmail(newEmail);
                    userRepository.save(user);
                    fixedCount++;
                }
            }
        }
        
        if (fixedCount > 0) {
            log.info("✅ Fixed {} duplicate email issues", fixedCount);
        } else {
            log.info("✅ No duplicate emails found");
        }
        
        return fixedCount;
    }
    
    /**
     * Fix invalid assignment points (null hoặc negative)
     */
    @Transactional
    public int fixInvalidAssignmentPoints() {
        log.info("🔧 Fixing invalid assignment points...");
        
        List<Assignment> invalidAssignments = assignmentRepository.findAll().stream()
            .filter(a -> a.getPoints() == null || a.getPoints() < 0)
            .collect(Collectors.toList());
            
        if (invalidAssignments.isEmpty()) {
            log.info("✅ No invalid assignment points found");
            return 0;
        }
        
        log.warn("⚠️ Found {} assignments with invalid points", invalidAssignments.size());
        
        for (Assignment assignment : invalidAssignments) {
            Integer oldPoints = assignment.getPoints();
            assignment.setPoints(100); // Default to 100 points
            
            log.info("Fixed assignment ID {}: points {} -> 100", 
                assignment.getId(), oldPoints);
            
            assignmentRepository.save(assignment);
        }
        
        log.info("✅ Fixed {} invalid assignment points", invalidAssignments.size());
        return invalidAssignments.size();
    }
    
    /**
     * Fix classroom teacher references (classrooms without teachers)
     */
    @Transactional
    public int fixClassroomTeacherReferences() {
        log.info("🔧 Fixing classroom teacher references...");
        
        List<Classroom> classroomsWithoutTeacher = classroomRepository.findAll().stream()
            .filter(c -> c.getTeacher() == null)
            .collect(Collectors.toList());
            
        if (classroomsWithoutTeacher.isEmpty()) {
            log.info("✅ No classrooms without teachers found");
            return 0;
        }
        
        // Tìm một teacher để assign
        List<User> teachers = userRepository.findByRoleId(2); // Teacher role
        if (teachers.isEmpty()) {
            log.error("❌ Cannot fix classroom teacher references: no teachers found");
            return 0;
        }
        
        User defaultTeacher = teachers.get(0);
        log.info("Using default teacher: {} (ID: {})", defaultTeacher.getFullName(), defaultTeacher.getId());
        
        for (Classroom classroom : classroomsWithoutTeacher) {
            log.info("Assigning teacher to classroom ID {}: {}", 
                classroom.getId(), classroom.getName());
            
            classroom.setTeacher(defaultTeacher);
            classroomRepository.save(classroom);
        }
        
        log.info("✅ Fixed {} classroom teacher references", classroomsWithoutTeacher.size());
        return classroomsWithoutTeacher.size();
    }
    
    /**
     * Fix lectures với missing dates
     */
    @Transactional
    public int fixLectureMissingDates() {
        log.info("🔧 Fixing lectures with missing dates...");

        List<Lecture> lecturesWithoutDates = lectureRepository.findAll().stream()
            .filter(lecture -> lecture.getLectureDate() == null)
            .collect(Collectors.toList());

        if (lecturesWithoutDates.isEmpty()) {
            log.info("✅ No lectures with missing dates found");
            return 0;
        }

        log.warn("⚠️ Found {} lectures with missing dates", lecturesWithoutDates.size());

        for (Lecture lecture : lecturesWithoutDates) {
            LocalDate assignedDate = calculateAppropriateDate(lecture);

            log.info("Fixing lecture ID {}: '{}' -> assigning date: {}",
                lecture.getId(), lecture.getTitle(), assignedDate);

            lecture.setLectureDate(assignedDate);
            lecture.setUpdatedAt(LocalDateTime.now());
            lectureRepository.save(lecture);
        }

        log.info("✅ Fixed {} lectures with missing dates", lecturesWithoutDates.size());
        return lecturesWithoutDates.size();
    }

    /**
     * Calculate appropriate date for lecture based on various factors
     */
    private LocalDate calculateAppropriateDate(Lecture lecture) {
        // Strategy 1: Use classroom schedule if available
        if (lecture.getClassroom() != null) {
            List<Schedule> schedules = scheduleRepository.findByClassroomId(lecture.getClassroom().getId());
            if (!schedules.isEmpty()) {
                Schedule schedule = schedules.get(0);
                // Use schedule's start date or a reasonable date based on schedule
                LocalDate scheduleBasedDate = calculateDateFromSchedule(schedule);
                if (scheduleBasedDate != null) {
                    log.debug("Using schedule-based date for lecture {}: {}", lecture.getId(), scheduleBasedDate);
                    return scheduleBasedDate;
                }
            }
        }

        // Strategy 2: Use lecture creation timestamp if available
        if (lecture.getCreatedAt() != null) {
            LocalDate creationDate = lecture.getCreatedAt().toLocalDate();
            log.debug("Using creation date for lecture {}: {}", lecture.getId(), creationDate);
            return creationDate;
        }

        // Strategy 3: Use classroom context (fallback to reasonable date)
        if (lecture.getClassroom() != null) {
            // Use a reasonable date based on classroom ID to spread lectures
            LocalDate baseDate = LocalDate.now().minusDays(7); // Start from a week ago
            LocalDate adjustedDate = baseDate.plusDays(lecture.getClassroom().getId() % 7);
            log.debug("Using classroom-based calculated date for lecture {}: {}", lecture.getId(), adjustedDate);
            return adjustedDate;
        }

        // Strategy 4: Default to current date
        LocalDate defaultDate = LocalDate.now();
        log.debug("Using default current date for lecture {}: {}", lecture.getId(), defaultDate);
        return defaultDate;
    }

    /**
     * Calculate date from schedule information
     */
    private LocalDate calculateDateFromSchedule(Schedule schedule) {
        // If schedule has day of week info, calculate next occurrence
        if (schedule.getDayOfWeek() != null) {
            return calculateNextDateForDayOfWeek(schedule.getDayOfWeek());
        }

        // Fallback to a reasonable date based on schedule start time
        if (schedule.getStartTime() != null) {
            // Use today or tomorrow based on start time
            LocalDate today = LocalDate.now();
            return schedule.getStartTime().isAfter(LocalTime.now()) ? today : today.plusDays(1);
        }

        return null;
    }

    /**
     * Calculate next date for given day of week (Integer format: 0=Monday, 1=Tuesday, etc.)
     */
    private LocalDate calculateNextDateForDayOfWeek(Integer dayOfWeek) {
        LocalDate today = LocalDate.now();

        // Convert integer day to target day (0=Monday, 1=Tuesday, etc.)
        int targetDay = (dayOfWeek % 7) + 1; // Convert to Java DayOfWeek (1=Monday, 7=Sunday)
        int currentDay = today.getDayOfWeek().getValue();

        // Calculate days to add to get to target day
        int daysToAdd = (targetDay - currentDay + 7) % 7;
        if (daysToAdd == 0) {
            daysToAdd = 7; // If it's the same day, schedule for next week
        }

        return today.plusDays(daysToAdd);
    }

    /**
     * Generate unique email by adding suffix
     */
    private String generateUniqueEmail(String originalEmail, int suffix) {
        String[] parts = originalEmail.split("@");
        if (parts.length != 2) {
            return originalEmail + "." + suffix;
        }

        return parts[0] + "." + suffix + "@" + parts[1];
    }
    
    /**
     * Cleanup tất cả dữ liệu (DANGEROUS - chỉ dùng cho development)
     */
    @Transactional
    public void cleanupAllData() {
        log.warn("⚠️ ============== CLEANING UP ALL DATA ==============");
        log.warn("This will delete ALL data in the database!");
        
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        enrollmentRepository.deleteAll();
        classroomRepository.deleteAll();
        userRepository.deleteAll();
        
        log.warn("✅ All data cleaned up");
    }
}
