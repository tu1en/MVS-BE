package com.classroomapp.classroombackend.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }

    @PostMapping("/create-attendance-session")
    public ResponseEntity<Map<String, Object>> createTestAttendanceSession(@RequestBody Map<String, Object> request) {
        try {
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            Long classroomId = Long.valueOf(request.get("classroomId").toString());
            String lectureTitle = (String) request.get("lectureTitle");

            // Find teacher and classroom
            User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

            // Create lecture first
            Lecture lecture = new Lecture();
            lecture.setTitle(lectureTitle);
            lecture.setClassroom(classroom);
            lecture.setCreatedAt(LocalDateTime.now());
            lecture = lectureRepository.save(lecture);

            // Create attendance session
            AttendanceSession session = new AttendanceSession();
            session.setClassroom(classroom);
            session.setLecture(lecture);
            session.setSessionDate(LocalDate.now());
            session.setStartTime(Instant.now());
            session.setEndTime(Instant.now().plusSeconds(5400)); // 1.5 hours
            session.setStatus(AttendanceSession.SessionStatus.CLOSED);
            session.setCreatedAt(LocalDateTime.now());
            session = attendanceSessionRepository.save(session);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("lectureId", lecture.getId());
            response.put("message", "Attendance session created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/count")
    public Long getUserCount() {
        return userRepository.count();
    }

    @GetMapping("/users/teacher")
    public User getTeacherUser() {
        return userRepository.findByEmail("teacher@test.com").orElse(null);
    }
}