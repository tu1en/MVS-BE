package com.classroomapp.classroombackend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.repository.AttendanceRepository;
import com.classroomapp.classroombackend.repository.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.ClassroomRepository;
import com.classroomapp.classroombackend.repository.UserRepository;

/**
 * Initialize test data when application starts
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;

    public DataLoader(
            UserRepository userRepository,
            ClassroomRepository classroomRepository,
            AttendanceSessionRepository sessionRepository,
            AttendanceRepository attendanceRepository) {
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Không cần xóa dữ liệu vì đã có data.sql
        // Chỉ thực hiện các tác vụ khởi tạo bổ sung nếu cần
    }
} 