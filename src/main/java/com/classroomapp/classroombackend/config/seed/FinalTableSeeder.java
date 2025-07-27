package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class FinalTableSeeder {
    
    private static final Logger log = LoggerFactory.getLogger(FinalTableSeeder.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private UserSeeder userSeeder;
    
    @Autowired
    private ClassroomSeeder classroomSeeder;
    
    @Autowired
    private ClassroomEnrollmentSeeder classroomEnrollmentSeeder;
    
    @Autowired
    private LectureSeeder lectureSeeder;
    
    @Autowired
    private DataVerificationSeeder dataVerificationSeeder;
    
    @Transactional
    public void seedFinalTables() {
        log.info("=== Starting Final Table Seeding Process ===");
        
        try {
            ensureBasicData();
            runFinalVerification();
            
            log.info("=== Final Table Seeding Process Completed Successfully ===");
        } catch (Exception e) {
            log.error("Error during final table seeding", e);
            throw e;
        }
    }
    
    private void ensureBasicData() {
        log.info("Ensuring basic data exists...");
        
        if (userRepository.count() == 0) {
            log.info("No users found, running user seeder...");
            userSeeder.seed();
        }
        
        if (classroomRepository.count() == 0) {
            log.info("No classrooms found, running classroom seeder...");
            List<Classroom> classrooms = classroomSeeder.seed();
            
            log.info("Running enrollment seeder...");
            classroomEnrollmentSeeder.seed();
            
            log.info("Running lecture seeder...");
            lectureSeeder.seed(classrooms);
        }
        
        log.info("Basic data verification complete");
    }
    
    private void runFinalVerification() {
        log.info("Running final data verification...");
        
        dataVerificationSeeder.verifyDataIntegrity();
        
        long userCount = userRepository.count();
        long classroomCount = classroomRepository.count();
        
        log.info("Final counts: Users={}, Classrooms={}", userCount, classroomCount);
        
        if (userCount == 0) {
            log.error("CRITICAL: No users found after seeding!");
            throw new IllegalStateException("User seeding failed");
        }
        
        if (classroomCount == 0) {
            log.error("CRITICAL: No classrooms found after seeding!");
            throw new IllegalStateException("Classroom seeding failed");
        }
        
        log.info("Final verification completed successfully");
    }
    
    @Transactional
    public void reseedAllTables() {
        log.info("=== Starting Complete Re-seeding Process ===");
        
        try {
            log.info("Force re-running all seeders...");
            
            userSeeder.seed();
            List<Classroom> classrooms = classroomSeeder.seed();
            classroomEnrollmentSeeder.seed();
            lectureSeeder.seed(classrooms);
            
            runFinalVerification();
            
            log.info("=== Complete Re-seeding Process Completed ===");
        } catch (Exception e) {
            log.error("Error during complete re-seeding", e);
            throw e;
        }
    }
    
    public void seed() {
        seedFinalTables();
    }
    
    public void verifyTableIntegrity() {
        log.info("=== Verifying Table Integrity ===");
        
        long userCount = userRepository.count();
        long classroomCount = classroomRepository.count();
        
        log.info("Current table counts:");
        log.info("  - Users: {}", userCount);
        log.info("  - Classrooms: {}", classroomCount);
        
        List<User> students = userRepository.findByRoleId(1);
        List<User> teachers = userRepository.findByRoleId(2);
        
        log.info("User breakdown:");
        log.info("  - Students: {}", students.size());
        log.info("  - Teachers: {}", teachers.size());
        
        if (students.isEmpty()) {
            log.warn("WARNING: No students found");
        }
        
        if (teachers.isEmpty()) {
            log.warn("WARNING: No teachers found");
        }
        
        List<Classroom> classrooms = classroomRepository.findAll();
        for (Classroom classroom : classrooms) {
            User teacher = classroom.getTeacher();
            log.info("Classroom: {} -> Teacher: {}", 
                classroom.getName(), 
                teacher != null ? teacher.getFullName() : "NULL");
        }
        
        log.info("=== Table Integrity Verification Complete ===");
    }
}