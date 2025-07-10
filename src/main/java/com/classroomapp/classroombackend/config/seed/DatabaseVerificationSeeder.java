package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseVerificationSeeder {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    public void verify() {
        System.out.println("🔍 [DatabaseVerification] Starting database state verification...");
        
        // Check all users
        List<User> allUsers = userRepository.findAll();
        System.out.println("👥 [DatabaseVerification] Found " + allUsers.size() + " total users:");
        
        for (User user : allUsers) {
            System.out.println("   - ID: " + user.getId() + 
                             ", Username: " + user.getUsername() + 
                             ", Role: " + user.getRoleId() + 
                             ", Name: " + user.getFullName());
        }
        
        // Find teacher specifically
        User teacher = userRepository.findByUsername("teacher").orElse(null);
        if (teacher != null) {
            System.out.println("🎓 [DatabaseVerification] Teacher found - ID: " + teacher.getId() + 
                             ", Name: " + teacher.getFullName());
            
            // Check schedules for this teacher
            List<Schedule> teacherSchedules = scheduleRepository.findByTeacherId(teacher.getId());
            System.out.println("📅 [DatabaseVerification] Found " + teacherSchedules.size() + 
                             " schedules for teacher ID " + teacher.getId());
            
            if (!teacherSchedules.isEmpty()) {
                System.out.println("📋 [DatabaseVerification] Teacher's schedules:");
                for (int i = 0; i < Math.min(5, teacherSchedules.size()); i++) {
                    Schedule schedule = teacherSchedules.get(i);
                    System.out.println("   - Schedule " + (i+1) + ": " + 
                                     getDayName(schedule.getDayOfWeek()) + " " +
                                     schedule.getStartTime() + "-" + schedule.getEndTime() + 
                                     " | " + schedule.getSubject() + 
                                     " | Room: " + schedule.getRoom());
                }
                if (teacherSchedules.size() > 5) {
                    System.out.println("   ... and " + (teacherSchedules.size() - 5) + " more schedules");
                }
            } else {
                System.out.println("⚠️ [DatabaseVerification] NO SCHEDULES FOUND for teacher ID " + teacher.getId());
            }
        } else {
            System.out.println("❌ [DatabaseVerification] Teacher user not found!");
        }
        
        // Check all schedules
        List<Schedule> allSchedules = scheduleRepository.findAll();
        System.out.println("📅 [DatabaseVerification] Total schedules in database: " + allSchedules.size());
        
        if (!allSchedules.isEmpty()) {
            System.out.println("📋 [DatabaseVerification] All schedules by teacher:");
            allSchedules.stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getTeacher().getId()))
                .forEach((teacherId, schedules) -> {
                    User scheduleTeacher = userRepository.findById(teacherId).orElse(null);
                    String teacherName = scheduleTeacher != null ? scheduleTeacher.getFullName() : "Unknown";
                    System.out.println("   - Teacher ID " + teacherId + " (" + teacherName + "): " + 
                                     schedules.size() + " schedules");
                });
        }
        
        System.out.println("✅ [DatabaseVerification] Verification completed");
    }
    
    private String getDayName(int dayOfWeek) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return days[dayOfWeek];
    }
}
