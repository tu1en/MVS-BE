package com.classroomapp.classroombackend.config.seed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.exammangement.Exam;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.exammangement.ExamRepository;

@Component
public class ExamSeeder {

    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;

    public void seed() {
        System.out.println("🔄 [ExamSeeder] Starting exam seeding process...");
        System.out.println("🔄 [ExamSeeder] Current exam count: " + examRepository.count());

        if (examRepository.count() == 0) {
            System.out.println("🔄 [ExamSeeder] Seeding exams...");

            List<Classroom> classrooms = classroomRepository.findAll();
            System.out.println("🔄 [ExamSeeder] Found " + classrooms.size() + " classrooms");

            if (classrooms.isEmpty()) {
                System.out.println("⚠️ [ExamSeeder] No classrooms found. Skipping exam seeding.");
                return;
            }

            // Debug: Print all classroom names
            for (Classroom classroom : classrooms) {
                System.out.println("🔄 [ExamSeeder] Classroom found: " + classroom.getName() + " (ID: " + classroom.getId() + ")");
            }

            // Find classrooms by partial name
            Classroom mathClass = findClassroomByPartialName(classrooms, "Toán");
            Classroom litClass = findClassroomByPartialName(classrooms, "Văn");
            Classroom csClass = findClassroomByPartialName(classrooms, "Công nghệ");

            System.out.println("🔄 [ExamSeeder] Math class found: " + (mathClass != null ? mathClass.getName() : "null"));
            System.out.println("🔄 [ExamSeeder] Literature class found: " + (litClass != null ? litClass.getName() : "null"));
            System.out.println("🔄 [ExamSeeder] CS class found: " + (csClass != null ? csClass.getName() : "null"));

            int examsCreated = 0;

            if (mathClass != null) {
                System.out.println("🔄 [ExamSeeder] Creating exams for math class: " + mathClass.getName());
                createExamForClassroom(mathClass, "Kiểm tra giữa kỳ Toán", 120, 10);
                createExamForClassroom(mathClass, "Kiểm tra cuối kỳ Toán", 180, 30);
                examsCreated += 2;
                System.out.println("✅ [ExamSeeder] Created 2 exams for math class");
            }

            if (litClass != null) {
                System.out.println("🔄 [ExamSeeder] Creating exam for literature class: " + litClass.getName());
                createExamForClassroom(litClass, "Bài thi hết môn Văn", 150, 25);
                examsCreated += 1;
                System.out.println("✅ [ExamSeeder] Created 1 exam for literature class");
            }

            if (csClass != null) {
                System.out.println("🔄 [ExamSeeder] Creating exam for CS class: " + csClass.getName());
                createExamForClassroom(csClass, "Thi thực hành Java", 90, 15);
                examsCreated += 1;
                System.out.println("✅ [ExamSeeder] Created 1 exam for CS class");
            }

            if (examsCreated > 0) {
                System.out.println("✅ [ExamSeeder] Created " + examsCreated + " sample exams for various classes.");
            } else {
                 System.out.println("⚠️ [ExamSeeder] Could not find relevant classrooms to seed exams.");
            }

        } else {
            System.out.println("✅ [ExamSeeder] Exams already seeded (count: " + examRepository.count() + ")");
        }
    }

    private void createExamForClassroom(Classroom classroom, String title, int duration, int daysFromNow) {
        try {
            System.out.println("🔄 [ExamSeeder] Creating exam: " + title + " for classroom: " + classroom.getName());

            Instant startTime = LocalDateTime.now().plusDays(daysFromNow).withHour(9).withMinute(0).atZone(ZoneId.systemDefault()).toInstant();
            Instant endTime = startTime.plusSeconds(duration * 60);

            System.out.println("🔄 [ExamSeeder] Start time: " + startTime);
            System.out.println("🔄 [ExamSeeder] End time: " + endTime);
            System.out.println("🔄 [ExamSeeder] Duration: " + duration + " minutes");

            Exam exam = new Exam();
            exam.setTitle(title);
            exam.setStartTime(startTime);
            exam.setEndTime(endTime);
            exam.setDurationInMinutes(duration);
            exam.setClassroom(classroom);

            Exam savedExam = examRepository.save(exam);
            System.out.println("✅ [ExamSeeder] Successfully created exam with ID: " + savedExam.getId());

        } catch (Exception e) {
            System.out.println("❌ [ExamSeeder] Error creating exam: " + title);
            System.out.println("❌ [ExamSeeder] Error details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Classroom findClassroomByPartialName(List<Classroom> classrooms, String partialName) {
        return classrooms.stream()
                .filter(c -> c.getName().contains(partialName))
                .findFirst()
                .orElse(null);
    }
} 