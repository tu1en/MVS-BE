package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.impl.hrmanagement.TeacherSalaryCalculationService;
import com.classroomapp.classroombackend.service.impl.hrmanagement.TeacherSalaryCalculationService.TeacherSalaryResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo data loader để test service tính lương giáo viên từ lịch sử giảng dạy
 */
@Component
@Order(100) // Chạy sau DataLoader chính
@Slf4j
public class TeacherSalaryDemoDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;
    
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    private TeacherSalaryCalculationService teacherSalaryCalculationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔄 Starting Teacher Salary Demo Data Loader...");
        
        try {
            // Tìm giáo viên để test
            User teacher = userRepository.findByUsername("teacher").orElse(null);
            if (teacher == null) {
                log.warn("⚠️ Teacher user not found, skipping demo");
                return;
            }
            
            log.info("👨‍🏫 Found teacher: {} (ID: {})", teacher.getFullName(), teacher.getId());
            
            // Tìm classroom của giáo viên
            List<Classroom> teacherClassrooms = classroomRepository.findByTeacherId(teacher.getId());
            if (teacherClassrooms.isEmpty()) {
                log.warn("⚠️ Teacher has no classrooms, skipping demo");
                return;
            }
            
            log.info("🏫 Teacher has {} classrooms", teacherClassrooms.size());
            
            // Tìm schedule của giáo viên
            List<Schedule> teacherSchedules = scheduleRepository.findByTeacherId(teacher.getId());
            log.info("📅 Teacher has {} schedules", teacherSchedules.size());
            
            // Tìm lịch sử giảng dạy
            List<AttendanceSession> teachingHistory = attendanceSessionRepository
                    .findTeachingHistoryByTeacherId(teacher.getId());
            log.info("📚 Teacher has {} teaching sessions", teachingHistory.size());
            
            // Tìm hợp đồng
            Contract contract = contractRepository.findActiveContractByUserId(teacher.getId()).orElse(null);
            if (contract == null) {
                log.warn("⚠️ Teacher has no active contract, skipping demo");
                return;
            }
            
            log.info("📋 Teacher contract: {} VND/hour", contract.getHourlySalary());
            
            // Test tính lương cho tháng hiện tại
            LocalDate now = LocalDate.now();
            LocalDate monthStart = now.withDayOfMonth(1);
            LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
            
            log.info("🧮 Testing salary calculation for period: {} to {}", monthStart, monthEnd);
            
            TeacherSalaryResult result = teacherSalaryCalculationService.calculateSalaryFromTeachingHistory(
                    teacher.getId(), monthStart, monthEnd, contract);
            
            log.info("💰 Salary calculation result:");
            log.info("   - Total teaching hours: {}", result.getTotalTeachingHours());
            log.info("   - Total teaching days: {}", result.getTotalTeachingDays());
            log.info("   - Weekday hours: {}", result.getWeekdayHours());
            log.info("   - Weekend hours: {}", result.getWeekendHours());
            log.info("   - Hourly rate: {} VND", result.getHourlyRate());
            log.info("   - Total salary: {} VND", result.getTotalSalary());
            
            // Test tính lương cho tuần hiện tại
            LocalDate weekStart = now.with(java.time.DayOfWeek.MONDAY);
            LocalDate weekEnd = now.with(java.time.DayOfWeek.SUNDAY);
            
            log.info("📅 Testing salary calculation for week: {} to {}", weekStart, weekEnd);
            
            TeacherSalaryResult weekResult = teacherSalaryCalculationService.calculateSalaryFromTeachingHistory(
                    teacher.getId(), weekStart, weekEnd, contract);
            
            log.info("💰 Weekly salary result:");
            log.info("   - Total teaching hours: {}", weekResult.getTotalTeachingHours());
            log.info("   - Total teaching days: {}", weekResult.getTotalTeachingDays());
            log.info("   - Total salary: {} VND", weekResult.getTotalSalary());
            
            log.info("✅ Teacher Salary Demo completed successfully!");
            
        } catch (Exception e) {
            log.error("❌ Error in Teacher Salary Demo: {}", e.getMessage(), e);
        }
    }
}
