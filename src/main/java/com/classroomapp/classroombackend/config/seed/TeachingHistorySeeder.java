package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeachingHistorySeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final LectureRepository lectureRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    @Override
    public void run(String... args) {
        seed();
    }

    @Transactional
    public void seed() {
        // 1️⃣ Fix sessions that have null teacher_id
        List<AttendanceSession> sessionsWithoutTeacher = attendanceSessionRepository.findAll()
                .stream()
                .filter(s -> s.getTeacher() == null)
                .toList();

        if (!sessionsWithoutTeacher.isEmpty()) {
            User teacher = userRepository.findByUsername("teacher")
                    .orElseThrow(() -> new RuntimeException("Teacher user not found"));

            sessionsWithoutTeacher.forEach(s -> {
                s.setTeacher(teacher);
                if (s.getTeacherClockInTime() == null) {
                    s.setTeacherClockInTime(LocalDateTime.now());
                }
            });

            attendanceSessionRepository.saveAll(sessionsWithoutTeacher);
            System.out.println("✅ TeachingHistorySeeder: Fixed " + sessionsWithoutTeacher.size() + " sessions without teacher.");
            return; // Nếu muốn seed thêm bản ghi mẫu → bỏ return
        }

        // 2️⃣ Nếu bảng hoàn toàn trống => tạo dữ liệu mẫu
        if (attendanceSessionRepository.count() == 0) {
            User teacher = userRepository.findByUsername("teacher")
                    .orElseGet(() -> {
                        User newTeacher = new User();
                        newTeacher.setFullName("Nguyen Van A");
                        newTeacher.setEmail("teacher@test.com");
                        newTeacher.setUsername("teacher");
                        newTeacher.setPassword("$2a$10$7ZwjWu9F8pFKcFdqZxzRJO1LkQjN2WzCfTFdqQnzIzOyV8dGz.r6m");
                        newTeacher.setRoleId(2); // ROLE_TEACHER
                        return userRepository.save(newTeacher);
                    });

            Classroom classroom = new Classroom();
            classroom.setName("Lập trình Java cơ bản");
            classroom.setDescription("Môn học lập trình Java dành cho sinh viên năm nhất");
            classroom.setTeacher(teacher);
            classroomRepository.save(classroom);

            Lecture lecture = new Lecture();
            lecture.setTitle("OOP in Java");
            lecture.setClassroom(classroom);
            lecture.setLectureDate(LocalDate.now());
            lectureRepository.save(lecture);

            AttendanceSession session = AttendanceSession.builder()
                    .classroom(classroom)
                    .lecture(lecture)
                    .teacher(teacher)
                    .sessionDate(LocalDate.now())
                    .teacherClockInTime(LocalDateTime.now())
                    .isOpen(false)
                    .isActive(true)
                    .autoMarkTeacherAttendance(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            attendanceSessionRepository.save(session);
            System.out.println("✅ TeachingHistorySeeder: Created a new sample teaching history session.");
        } else {
            System.out.println("✅ TeachingHistorySeeder: No action needed (teaching history exists).");
        }
    }
}
