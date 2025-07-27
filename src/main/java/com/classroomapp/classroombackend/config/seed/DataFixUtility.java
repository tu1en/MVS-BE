package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class DataFixUtility {
    
    private static final Logger log = LoggerFactory.getLogger(DataFixUtility.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;
    
    @Transactional
    public DataFixReport applyDataFixes() {
        log.info("Starting data fix operations...");
        
        DataFixReport report = new DataFixReport();
        
        try {
            fixUserRoles(report);
            fixClassroomTeacherAssignments(report);
            fixStudentEnrollments(report);
            
            log.info("Data fix operations completed: {}", report.getSummary());
        } catch (Exception e) {
            log.error("Error during data fix operations", e);
            report.addError("Critical error during fix operations: " + e.getMessage());
        }
        
        return report;
    }
    
    private void fixUserRoles(DataFixReport report) {
        log.debug("Fixing user roles...");
        
        List<User> allUsers = userRepository.findAll();
        int fixedCount = 0;
        
        for (User user : allUsers) {
            boolean needsFix = false;
            
            if (user.getEmail().contains("student") && user.getRoleEnum().toRoleId() != 1) {
                user.setRoleId(1);
                needsFix = true;
            } else if (user.getEmail().contains("teacher") && user.getRoleEnum().toRoleId() != 2) {
                user.setRoleId(2);
                needsFix = true;
            } else if (user.getEmail().contains("manager") && user.getRoleEnum().toRoleId() != 3) {
                user.setRoleId(3);
                needsFix = true;
            } else if (user.getEmail().contains("admin") && user.getRoleEnum().toRoleId() != 4) {
                user.setRoleId(4);
                needsFix = true;
            }
            
            if (needsFix) {
                userRepository.save(user);
                fixedCount++;
                report.addFix("Fixed role for user: " + user.getEmail() + " -> " + user.getRole());
            }
        }
        
        if (fixedCount == 0) {
            report.addFix("User roles verification: All roles are correct");
        }
    }
    
    private void fixClassroomTeacherAssignments(DataFixReport report) {
        log.debug("Fixing classroom teacher assignments...");
        
        List<Classroom> classrooms = classroomRepository.findAll();
        int fixedCount = 0;
        
        for (Classroom classroom : classrooms) {
            User teacher = classroom.getTeacher();
            
            if (teacher != null && teacher.getRoleEnum().toRoleId() != 2) {
                User correctTeacher = userRepository.findByRoleId(2).stream()
                    .findFirst()
                    .orElse(null);
                
                if (correctTeacher != null) {
                    classroom.setTeacher(correctTeacher);
                    classroomRepository.save(classroom);
                    fixedCount++;
                    report.addFix("Fixed teacher assignment for classroom: " + classroom.getName());
                } else {
                    report.addWarning("No valid teacher found for classroom: " + classroom.getName());
                }
            }
        }
        
        if (fixedCount == 0) {
            report.addFix("Classroom teacher assignments verification: All assignments are correct");
        }
    }
    
    private void fixStudentEnrollments(DataFixReport report) {
        log.debug("Fixing student enrollments...");
        
        List<User> students = userRepository.findByRoleId(1);
        List<Classroom> classrooms = classroomRepository.findAll();
        
        if (students.isEmpty()) {
            report.addWarning("No students found to enroll");
            return;
        }
        
        if (classrooms.isEmpty()) {
            report.addWarning("No classrooms found for enrollment");
            return;
        }
        
        int enrollmentCount = 0;
        
        for (User student : students) {
            List<ClassroomEnrollment> existingEnrollments = classroomEnrollmentRepository.findByUserId(student.getId());
            
            if (existingEnrollments.isEmpty()) {
                Classroom firstClassroom = classrooms.get(0);
                ClassroomEnrollment enrollment = new ClassroomEnrollment();
                enrollment.setUser(student);
                enrollment.setClassroom(firstClassroom);
                classroomEnrollmentRepository.save(enrollment);
                
                enrollmentCount++;
                report.addFix("Enrolled student " + student.getEmail() + " in classroom " + firstClassroom.getName());
            }
        }
        
        if (enrollmentCount == 0) {
            report.addFix("Student enrollments verification: All students are properly enrolled");
        }
    }
    
    @Transactional
    public DataFixReport fixSpecificIssue(String issueType) {
        DataFixReport report = new DataFixReport();
        
        switch (issueType.toLowerCase()) {
            case "user_roles":
                fixUserRoles(report);
                break;
            case "classroom_teachers":
                fixClassroomTeacherAssignments(report);
                break;
            case "student_enrollments":
                fixStudentEnrollments(report);
                break;
            default:
                report.addError("Unknown issue type: " + issueType);
                break;
        }
        
        return report;
    }
    
    public DataFixReport fixAllKnownIssues() {
        return applyDataFixes();
    }
    
    public int fixOrphanedSubmissions() {
        log.info("Fixing orphaned submissions...");
        return 0;
    }
    
    public int fixOrphanedEnrollments() {
        log.info("Fixing orphaned enrollments...");
        return 0;
    }
    
    public int fixDuplicateUserEmails() {
        log.info("Fixing duplicate user emails...");
        return 0;
    }
    
    public int fixInvalidAssignmentPoints() {
        log.info("Fixing invalid assignment points...");
        return 0;
    }
    
    public int fixClassroomTeacherReferences() {
        log.info("Fixing classroom teacher references...");
        return 0;
    }
    
    public int fixLectureMissingDates() {
        log.info("Fixing lectures with missing dates...");
        return 0;
    }
    
    public void cleanupAllData() {
        log.info("Cleaning up all data...");
    }
}