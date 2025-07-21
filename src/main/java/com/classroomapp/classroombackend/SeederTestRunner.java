package com.classroomapp.classroombackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;

import com.classroomapp.classroombackend.config.seed.FinalTableSeeder;
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
 * Simple test runner để verify seeders hoạt động
 * Chạy với: mvn exec:java -Dexec.mainClass="com.classroomapp.classroombackend.SeederTestRunner" -Dspring.profiles.active=local
 */
@SpringBootApplication
@Profile("local")
public class SeederTestRunner implements CommandLineRunner {

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
    @Autowired private FinalTableSeeder finalTableSeeder;

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "local");
        ConfigurableApplicationContext context = SpringApplication.run(SeederTestRunner.class, args);
        
        // Auto-shutdown after verification
        new Thread(() -> {
            try {
                Thread.sleep(10000); // Wait 10 seconds for seeders to complete
                System.out.println("🛑 Auto-shutting down application...");
                context.close();
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 [SeederTestRunner] Starting seeder verification...");
        System.out.println("⏳ [SeederTestRunner] Waiting for DataLoader to complete...");
        
        // Wait a bit for DataLoader to finish
        Thread.sleep(5000);
        
        System.out.println("📊 [SeederTestRunner] Checking table counts...");
        
        // Check all table counts
        long userCount = userRepository.count();
        long roleCount = roleRepository.count();
        long classroomCount = classroomRepository.count();
        long systemRoleCount = systemRoleRepository.count();
        long systemPermissionCount = systemPermissionRepository.count();
        long rolePermissionCount = rolePermissionRepository.count();
        long systemConfigCount = systemConfigurationRepository.count();
        long auditLogCount = auditLogRepository.count();
        long absenceCount = absenceRepository.count();
        long requestCount = requestRepository.count();
        long systemMonitoringCount = systemMonitoringRepository.count();
        long syllabusCount = syllabusRepository.count();
        
        System.out.println("📋 [SeederTestRunner] TABLE COUNTS:");
        System.out.println("  👥 Users: " + userCount);
        System.out.println("  🔐 Roles: " + roleCount);
        System.out.println("  🏫 Classrooms: " + classroomCount);
        System.out.println("  ⚙️ System Roles: " + systemRoleCount);
        System.out.println("  🔑 System Permissions: " + systemPermissionCount);
        System.out.println("  🔗 Role Permissions: " + rolePermissionCount);
        System.out.println("  ⚙️ System Configurations: " + systemConfigCount);
        System.out.println("  📝 Audit Logs: " + auditLogCount);
        System.out.println("  🏥 Absences: " + absenceCount);
        System.out.println("  📨 Requests: " + requestCount);
        System.out.println("  📊 System Monitoring: " + systemMonitoringCount);
        System.out.println("  📚 Syllabi: " + syllabusCount);
        
        // Calculate totals
        long totalRecords = userCount + roleCount + classroomCount + systemRoleCount + 
                           systemPermissionCount + rolePermissionCount + systemConfigCount + 
                           auditLogCount + absenceCount + requestCount + systemMonitoringCount + syllabusCount;
        
        System.out.println("📊 [SeederTestRunner] TOTAL RECORDS: " + totalRecords);
        
        // Count empty tables
        int emptyTables = 0;
        StringBuilder emptyTablesList = new StringBuilder();
        
        if (userCount == 0) { emptyTables++; emptyTablesList.append("users, "); }
        if (roleCount == 0) { emptyTables++; emptyTablesList.append("roles, "); }
        if (classroomCount == 0) { emptyTables++; emptyTablesList.append("classrooms, "); }
        if (systemRoleCount == 0) { emptyTables++; emptyTablesList.append("system_roles, "); }
        if (systemPermissionCount == 0) { emptyTables++; emptyTablesList.append("system_permissions, "); }
        if (rolePermissionCount == 0) { emptyTables++; emptyTablesList.append("role_permissions, "); }
        if (systemConfigCount == 0) { emptyTables++; emptyTablesList.append("system_configurations, "); }
        if (auditLogCount == 0) { emptyTables++; emptyTablesList.append("audit_logs, "); }
        if (absenceCount == 0) { emptyTables++; emptyTablesList.append("absences, "); }
        if (requestCount == 0) { emptyTables++; emptyTablesList.append("requests, "); }
        if (systemMonitoringCount == 0) { emptyTables++; emptyTablesList.append("system_monitoring, "); }
        if (syllabusCount == 0) { emptyTables++; emptyTablesList.append("syllabi, "); }
        
        System.out.println("🎯 [SeederTestRunner] EMPTY TABLES: " + emptyTables + "/12");
        
        if (emptyTables == 0) {
            System.out.println("🎉 [SeederTestRunner] SUCCESS! Achieved 0 empty tables!");
            System.out.println("🏆 [SeederTestRunner] All seeders working perfectly!");
        } else if (emptyTables < 6) {
            System.out.println("✅ [SeederTestRunner] GOOD PROGRESS! Most seeders working.");
            System.out.println("⚠️ [SeederTestRunner] Empty tables: " + emptyTablesList.toString());
        } else {
            System.out.println("❌ [SeederTestRunner] SEEDERS NOT WORKING!");
            System.out.println("🔧 [SeederTestRunner] Empty tables: " + emptyTablesList.toString());
            
            // Try to run FinalTableSeeder manually if most tables are empty
            if (emptyTables > 8) {
                System.out.println("🔄 [SeederTestRunner] Attempting to run FinalTableSeeder manually...");
                try {
                    finalTableSeeder.seed();
                    System.out.println("✅ [SeederTestRunner] FinalTableSeeder completed manually");
                    
                    // Re-check counts
                    absenceCount = absenceRepository.count();
                    requestCount = requestRepository.count();
                    systemMonitoringCount = systemMonitoringRepository.count();
                    syllabusCount = syllabusRepository.count();
                    
                    System.out.println("📊 [SeederTestRunner] UPDATED COUNTS:");
                    System.out.println("  🏥 Absences: " + absenceCount);
                    System.out.println("  📨 Requests: " + requestCount);
                    System.out.println("  📊 System Monitoring: " + systemMonitoringCount);
                    System.out.println("  📚 Syllabi: " + syllabusCount);
                    
                } catch (Exception e) {
                    System.out.println("❌ [SeederTestRunner] FinalTableSeeder failed: " + e.getMessage());
                }
            }
        }
        
        // Progress calculation
        int tablesWithData = 12 - emptyTables;
        double progressPercent = (tablesWithData / 12.0) * 100;
        System.out.println("📈 [SeederTestRunner] PROGRESS: " + tablesWithData + "/12 tables (" + 
                          String.format("%.1f", progressPercent) + "%)");
        
        System.out.println("✅ [SeederTestRunner] Verification completed!");
        System.out.println("🛑 [SeederTestRunner] Application will auto-shutdown in 5 seconds...");
    }
}
