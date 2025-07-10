package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeachingHistorySeeder {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public void seed() {
        System.out.println("🔄 [TeachingHistorySeeder] Starting teaching history seeding...");
        
        // Find the main teacher user
        User teacher = userRepository.findByUsername("teacher")
                .orElseThrow(() -> new RuntimeException("Teacher user not found"));
        
        System.out.println("✅ [TeachingHistorySeeder] Found teacher: " + teacher.getFullName() + " (ID: " + teacher.getId() + ")");
        
        // Get all classrooms for this teacher
        List<Classroom> classrooms = classroomRepository.findByTeacher(teacher);
        if (classrooms.isEmpty()) {
            System.out.println("⚠️ [TeachingHistorySeeder] No classrooms found for teacher. Skipping teaching history seeding.");
            return;
        }
        
        System.out.println("✅ [TeachingHistorySeeder] Found " + classrooms.size() + " classrooms for teacher");
        
        // Create teaching history records
        // Note: Since there's no specific TeachingHistory entity, we'll create data that can be used
        // by the teaching history API endpoints. This might involve creating additional records
        // in existing entities or creating a new entity if needed.
        
        // For now, we'll create some sample data that represents teaching history
        // This could be implemented as:
        // 1. Historical classroom assignments
        // 2. Past semester records
        // 3. Teaching performance metrics
        
        createHistoricalClassroomData(teacher, classrooms);
        
        System.out.println("✅ [TeachingHistorySeeder] Teaching history seeding completed");
    }
    
    private void createHistoricalClassroomData(User teacher, List<Classroom> currentClassrooms) {
        // Create historical data by modifying existing classrooms or creating new ones
        // representing past semesters
        
        LocalDate currentDate = LocalDate.now();
        
        // Create data for previous semesters
        for (int semesterBack = 1; semesterBack <= 3; semesterBack++) {
            LocalDate semesterStart = currentDate.minusMonths(semesterBack * 6);
            
            for (int i = 0; i < Math.min(2, currentClassrooms.size()); i++) {
                Classroom currentClassroom = currentClassrooms.get(i);
                
                // Create a historical classroom record
                Classroom historicalClassroom = new Classroom();
                historicalClassroom.setName(currentClassroom.getName() + " - Kỳ " + semesterBack);
                historicalClassroom.setDescription("Lớp học kỳ trước - " + currentClassroom.getDescription());
                historicalClassroom.setSection(currentClassroom.getSection() + "_H" + semesterBack);
                historicalClassroom.setSubject(currentClassroom.getSubject());
                historicalClassroom.setTeacher(teacher);
                historicalClassroom.setCourseId(currentClassroom.getCourseId());
                
                // Save the historical classroom
                classroomRepository.save(historicalClassroom);
                
                System.out.println("📚 [TeachingHistorySeeder] Created historical classroom: " + historicalClassroom.getName());
            }
        }
        
        System.out.println("✅ [TeachingHistorySeeder] Created historical classroom data for 3 previous semesters");
    }
}
