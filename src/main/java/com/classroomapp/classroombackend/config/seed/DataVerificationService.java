package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DataVerificationService {
    
    private static final Logger log = LoggerFactory.getLogger(DataVerificationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;
    
    @Autowired
    private LectureRepository lectureRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public DataVerificationReport runManualVerification() {
        log.info("Running manual data verification...");
        
        DataVerificationReport report = new DataVerificationReport();
        
        verifyUsers(report);
        verifyClassrooms(report);
        verifyEnrollments(report);
        verifyLectures(report);
        
        log.info("Manual verification completed: {}", report.getSummary());
        return report;
    }
    
    public String getHealthStatus() {
        DataVerificationReport report = runManualVerification();
        if (report.isHasCriticalIssues()) {
            return "UNHEALTHY - " + report.getIssues().size() + " critical issues found";
        } else if (!report.getWarnings().isEmpty()) {
            return "WARNING - " + report.getWarnings().size() + " warnings found";
        } else {
            return "HEALTHY - All systems operational";
        }
    }
    
    public String runVerificationAndGetJsonReport() {
        try {
            DataVerificationReport report = runManualVerification();
            return objectMapper.writeValueAsString(report);
        } catch (Exception e) {
            log.error("Error generating JSON report", e);
            return "{\"error\": \"Failed to generate report: " + e.getMessage() + "\"}";
        }
    }
    
    public boolean hasCriticalIssues() {
        DataVerificationReport report = runManualVerification();
        return report.isHasCriticalIssues();
    }
    
    private void verifyUsers(DataVerificationReport report) {
        log.debug("Verifying users...");
        
        User student = userRepository.findByEmail("student@test.com").orElse(null);
        User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
        
        if (student != null) {
            if (student.getRoleEnum().toRoleId() == 1) {
                report.addSuccess("Student user found with correct role");
            } else {
                report.addIssue("Student user has wrong role. Expected: 1 (STUDENT), Actual: " + student.getRoleEnum().toRoleId());
            }
        } else {
            report.addIssue("Student user not found");
        }
        
        if (teacher != null) {
            if (teacher.getRoleEnum().toRoleId() == 2) {
                report.addSuccess("Teacher user found with correct role");
            } else {
                report.addIssue("Teacher user has wrong role. Expected: 2 (TEACHER), Actual: " + teacher.getRoleEnum().toRoleId());
            }
        } else {
            report.addIssue("Teacher user not found");
        }
        
        List<User> students = userRepository.findByRoleId(1);
        List<User> teachers = userRepository.findByRoleId(2);
        
        report.addSuccess(String.format("Found %d students and %d teachers", students.size(), teachers.size()));
    }
    
    private void verifyClassrooms(DataVerificationReport report) {
        log.debug("Verifying classrooms...");
        
        List<Classroom> classrooms = classroomRepository.findAll();
        
        if (classrooms.isEmpty()) {
            report.addIssue("No classrooms found");
            return;
        }
        
        report.addSuccess("Found " + classrooms.size() + " classrooms");
        
        for (Classroom classroom : classrooms) {
            User classroomTeacher = classroom.getTeacher();
            if (classroomTeacher != null && classroomTeacher.getRoleEnum().toRoleId() != 2) {
                report.addIssue("Classroom " + classroom.getName() + " has teacher with wrong role: " + classroomTeacher.getRoleEnum().toRoleId());
            }
        }
    }
    
    private void verifyEnrollments(DataVerificationReport report) {
        log.debug("Verifying enrollments...");
        
        User student = userRepository.findByEmail("student@test.com").orElse(null);
        if (student != null) {
            List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByUserId(student.getId());
            if (enrollments.isEmpty()) {
                report.addIssue("Student is not enrolled in any classrooms");
            } else {
                report.addSuccess("Student is enrolled in " + enrollments.size() + " classrooms");
            }
        }
    }
    
    private void verifyLectures(DataVerificationReport report) {
        log.debug("Verifying lectures...");
        
        List<Lecture> allLectures = lectureRepository.findAll();
        
        if (allLectures.isEmpty()) {
            report.addIssue("No lectures found in database");
            return;
        }
        
        report.addSuccess("Found " + allLectures.size() + " total lectures");
        
        List<Classroom> classrooms = classroomRepository.findAll();
        for (Classroom classroom : classrooms) {
            List<Lecture> classroomLectures = lectureRepository.findByClassroomId(classroom.getId());
            if (classroomLectures.isEmpty()) {
                report.addWarning("Classroom " + classroom.getName() + " has no lectures");
            }
        }
    }
}