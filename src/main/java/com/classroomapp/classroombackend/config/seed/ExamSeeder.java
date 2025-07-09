package com.classroomapp.classroombackend.config.seed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        if (examRepository.count() == 0) {
            System.out.println("🔄 [ExamSeeder] Seeding exams...");

            // Kiểm tra lớp học 51 có tồn tại không
            Classroom classroom51 = classroomRepository.findById(51L).orElse(null);
            if (classroom51 == null) {
                System.out.println("⚠️ [ExamSeeder] Classroom 51 not found. Skipping exam seeding.");
                return;
            }

            // Helper method để chuyển đổi LocalDateTime sang Instant
            Instant startTime1 = LocalDateTime.of(2025, 7, 10, 9, 0).atZone(ZoneId.systemDefault()).toInstant();
            Instant endTime1 = LocalDateTime.of(2025, 7, 10, 11, 0).atZone(ZoneId.systemDefault()).toInstant();
            Instant startTime2 = LocalDateTime.of(2025, 7, 20, 9, 0).atZone(ZoneId.systemDefault()).toInstant();
            Instant endTime2 = LocalDateTime.of(2025, 7, 20, 12, 0).atZone(ZoneId.systemDefault()).toInstant();
            Instant startTime3 = LocalDateTime.of(2025, 7, 15, 14, 0).atZone(ZoneId.systemDefault()).toInstant();
            Instant endTime3 = LocalDateTime.of(2025, 7, 15, 15, 30).atZone(ZoneId.systemDefault()).toInstant();

            // Tạo exam mới với tiếng Việt đúng - sử dụng định dạng UTF-8
            Exam exam1 = new Exam();
            exam1.setTitle("Kiểm tra giữa kỳ");
            exam1.setStartTime(startTime1);
            exam1.setEndTime(endTime1);
            exam1.setDurationInMinutes(120);
            exam1.setClassroom(classroom51);
            examRepository.save(exam1);

            Exam exam2 = new Exam();
            exam2.setTitle("Kiểm tra cuối kỳ");
            exam2.setStartTime(startTime2);
            exam2.setEndTime(endTime2);
            exam2.setDurationInMinutes(180);
            exam2.setClassroom(classroom51);
            examRepository.save(exam2);

            Exam exam3 = new Exam();
            exam3.setTitle("Bài kiểm tra thực hành");
            exam3.setStartTime(startTime3);
            exam3.setEndTime(endTime3);
            exam3.setDurationInMinutes(90);
            exam3.setClassroom(classroom51);
            examRepository.save(exam3);

            System.out.println("✅ [ExamSeeder] Created 3 exams for classroom 51");
        } else {
            System.out.println("✅ [ExamSeeder] Exams already seeded");
        }
    }
} 