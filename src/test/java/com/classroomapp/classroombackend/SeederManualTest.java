package com.classroomapp.classroombackend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.config.seed.ComprehensiveTableSeeder;
import com.classroomapp.classroombackend.config.seed.FinalTableSeeder;
import com.classroomapp.classroombackend.config.seed.LectureSeeder;
import com.classroomapp.classroombackend.config.seed.RoleSeeder;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.administration.AuditLogRepository;
import com.classroomapp.classroombackend.repository.administration.RolePermissionRepository;
import com.classroomapp.classroombackend.repository.administration.SystemConfigurationRepository;
import com.classroomapp.classroombackend.repository.administration.SystemMonitoringRepository;
import com.classroomapp.classroombackend.repository.administration.SystemPermissionRepository;
import com.classroomapp.classroombackend.repository.administration.SystemRoleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SyllabusRepository;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Manual test để chạy seeders và verify kết quả
 * Chạy với: mvn test -Dtest=SeederManualTest -Dspring.profiles.active=test
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SeederManualTest {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private SystemRoleRepository systemRoleRepository;
    @Autowired private SystemPermissionRepository systemPermissionRepository;
    @Autowired private RolePermissionRepository rolePermissionRepository;
    @Autowired private SystemConfigurationRepository systemConfigurationRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private AbsenceRepository absenceRepository;
    @Autowired private RequestRepository requestRepository;
    @Autowired private SystemMonitoringRepository systemMonitoringRepository;
    @Autowired private SyllabusRepository syllabusRepository;

    @Autowired private RoleSeeder roleSeeder;
    @Autowired private LectureSeeder lectureSeeder;
    @Autowired private ComprehensiveTableSeeder comprehensiveTableSeeder;
    @Autowired private FinalTableSeeder finalTableSeeder;

    @Test
    public void testRunAllSeedersManually() {
        System.out.println("🧪 [SeederManualTest] Starting manual seeder test...");
        
        // Check initial state
        System.out.println("📊 [SeederManualTest] Initial counts:");
        printTableCounts();
        
        try {
            // Run RoleSeeder
            System.out.println("🔄 [SeederManualTest] Running RoleSeeder...");
            roleSeeder.seed();
            System.out.println("✅ [SeederManualTest] RoleSeeder completed");
            
            // Run ComprehensiveTableSeeder
            System.out.println("🔄 [SeederManualTest] Running ComprehensiveTableSeeder...");
            comprehensiveTableSeeder.seed();
            System.out.println("✅ [SeederManualTest] ComprehensiveTableSeeder completed");
            
            // Run FinalTableSeeder
            System.out.println("🔄 [SeederManualTest] Running FinalTableSeeder...");
            finalTableSeeder.seed();
            System.out.println("✅ [SeederManualTest] FinalTableSeeder completed");
            
            // Check final state
            System.out.println("📊 [SeederManualTest] Final counts:");
            printTableCounts();
            
            // Verify results
            verifySeederResults();
            
            System.out.println("🎉 [SeederManualTest] All seeders completed successfully!");
            
        } catch (Exception e) {
            System.out.println("❌ [SeederManualTest] Error running seeders: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private void printTableCounts() {
        System.out.println("  👥 Users: " + userRepository.count());
        System.out.println("  🔐 Roles: " + roleRepository.count());
        System.out.println("  🏫 Classrooms: " + classroomRepository.count());
        System.out.println("  ⚙️ System Roles: " + systemRoleRepository.count());
        System.out.println("  🔑 System Permissions: " + systemPermissionRepository.count());
        System.out.println("  🔗 Role Permissions: " + rolePermissionRepository.count());
        System.out.println("  ⚙️ System Configurations: " + systemConfigurationRepository.count());
        System.out.println("  📝 Audit Logs: " + auditLogRepository.count());
        System.out.println("  🏥 Absences: " + absenceRepository.count());
        System.out.println("  📨 Requests: " + requestRepository.count());
        System.out.println("  📊 System Monitoring: " + systemMonitoringRepository.count());
        System.out.println("  📚 Syllabi: " + syllabusRepository.count());
    }
    
    private void verifySeederResults() {
        // Verify RoleSeeder results
        long systemRoleCount = systemRoleRepository.count();
        long systemPermissionCount = systemPermissionRepository.count();
        long rolePermissionCount = rolePermissionRepository.count();
        
        System.out.println("🔍 [SeederManualTest] Verifying RoleSeeder results:");
        System.out.println("  Expected System Roles: 6, Actual: " + systemRoleCount);
        System.out.println("  Expected System Permissions: 8, Actual: " + systemPermissionCount);
        System.out.println("  Expected Role Permissions: ~30, Actual: " + rolePermissionCount);
        
        assertTrue(systemRoleCount >= 6, "Should have at least 6 system roles");
        assertTrue(systemPermissionCount >= 8, "Should have at least 8 system permissions");
        assertTrue(rolePermissionCount >= 20, "Should have at least 20 role permissions");
        
        // Verify ComprehensiveTableSeeder results
        long systemConfigCount = systemConfigurationRepository.count();
        long auditLogCount = auditLogRepository.count();
        
        System.out.println("🔍 [SeederManualTest] Verifying ComprehensiveTableSeeder results:");
        System.out.println("  Expected System Configurations: 15, Actual: " + systemConfigCount);
        System.out.println("  Expected Audit Logs: 300+, Actual: " + auditLogCount);
        
        assertTrue(systemConfigCount >= 15, "Should have at least 15 system configurations");
        assertTrue(auditLogCount >= 100, "Should have at least 100 audit logs");
        
        // Verify FinalTableSeeder results
        long absenceCount = absenceRepository.count();
        long requestCount = requestRepository.count();
        long systemMonitoringCount = systemMonitoringRepository.count();
        
        System.out.println("🔍 [SeederManualTest] Verifying FinalTableSeeder results:");
        System.out.println("  Expected Absences: 25, Actual: " + absenceCount);
        System.out.println("  Expected Requests: 30, Actual: " + requestCount);
        System.out.println("  Expected System Monitoring: 280+, Actual: " + systemMonitoringCount);
        
        assertTrue(absenceCount >= 25, "Should have at least 25 absences");
        assertTrue(requestCount >= 30, "Should have at least 30 requests");
        assertTrue(systemMonitoringCount >= 200, "Should have at least 200 system monitoring records");
        
        // Calculate total records
        long totalRecords = systemRoleCount + systemPermissionCount + rolePermissionCount + 
                           systemConfigCount + auditLogCount + absenceCount + requestCount + 
                           systemMonitoringCount;
        
        System.out.println("📊 [SeederManualTest] TOTAL SEEDED RECORDS: " + totalRecords);
        assertTrue(totalRecords >= 500, "Should have at least 500 total records from seeders");
        
        System.out.println("✅ [SeederManualTest] All verifications passed!");
    }
}
