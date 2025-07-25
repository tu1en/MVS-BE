package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class TonClassroomDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(TonClassroomDataSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;

    @Transactional
    public void seedTonClassroomData() {
        log.info("Starting Ton Classroom data seeding...");

        try {
            createTonClassroom();
            enrollStudentsInTonClassroom();

            log.info("Ton Classroom data seeding completed successfully");
        } catch (Exception e) {
            log.error("Error during Ton Classroom data seeding", e);
            throw e;
        }
    }

    private void createTonClassroom() {
        String tonClassName = "Lớp Toán Học Cơ Bản";

        List<Classroom> existingClassrooms = classroomRepository
                .findByNameContainingIgnoreCase(tonClassName, PageRequest.of(0, 10))
                .getContent();

        Classroom existingTonClassroom = existingClassrooms.stream().findFirst().orElse(null);
        if (existingTonClassroom != null) {
            log.info("Ton classroom already exists: {}", tonClassName);
            return;
        }

        User mathTeacher = userRepository.findByEmail("math@test.com").orElse(null);
        if (mathTeacher == null) {
            mathTeacher = userRepository.findByRoleId(2).stream()
                    .findFirst()
                    .orElse(null);
        }

        if (mathTeacher == null) {
            log.error("No teacher found for Ton classroom");
            return;
        }

        Classroom tonClassroom = new Classroom();
        tonClassroom.setName(tonClassName);
        tonClassroom.setDescription("Lớp học toán học cơ bản cho học sinh");
        tonClassroom.setTeacher(mathTeacher);

        classroomRepository.save(tonClassroom);
        log.info("Created Ton classroom: {} with teacher: {}", tonClassName, mathTeacher.getFullName());
    }

    private void enrollStudentsInTonClassroom() {
        List<Classroom> tonClassrooms = classroomRepository
                .findByNameContainingIgnoreCase("Lớp Toán Học Cơ Bản", PageRequest.of(0, 10))
                .getContent();

        Classroom tonClassroom = tonClassrooms.stream().findFirst().orElse(null);
        if (tonClassroom == null) {
            log.error("Ton classroom not found for enrollment");
            return;
        }

        List<User> students = userRepository.findByRoleId(1);
        int enrolledCount = 0;

        for (User student : students) {
            ClassroomEnrollment existingEnrollment = classroomEnrollmentRepository
                    .findByClassroomIdAndUserId(tonClassroom.getId(), student.getId())
                    .orElse(null);

            if (existingEnrollment == null) {
                ClassroomEnrollment enrollment = new ClassroomEnrollment();
                enrollment.setUser(student);
                enrollment.setClassroom(tonClassroom);
                classroomEnrollmentRepository.save(enrollment);

                enrolledCount++;
                log.debug("Enrolled student {} in Ton classroom", student.getEmail());
            }
        }

        if (enrolledCount > 0) {
            log.info("Enrolled {} students in Ton classroom", enrolledCount);
        } else {
            log.info("All students already enrolled in Ton classroom");
        }
    }

    @Transactional
    public void cleanTonClassroomData() {
        log.info("Cleaning Ton Classroom data...");

        List<Classroom> tonClassrooms = classroomRepository
                .findByNameContainingIgnoreCase("Lớp Toán Học Cơ Bản", PageRequest.of(0, 10))
                .getContent();

        Classroom tonClassroom = tonClassrooms.stream().findFirst().orElse(null);
        if (tonClassroom != null) {
            List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByClassroomId(tonClassroom.getId());
            classroomEnrollmentRepository.deleteAll(enrollments);
            classroomRepository.delete(tonClassroom);

            log.info("Cleaned Ton Classroom data");
        } else {
            log.info("No Ton Classroom data to clean");
        }
    }

    public void verifyTonClassroomData() {
        log.info("Verifying Ton Classroom data...");

        List<Classroom> tonClassrooms = classroomRepository
                .findByNameContainingIgnoreCase("Lớp Toán Học Cơ Bản", PageRequest.of(0, 10))
                .getContent();

        Classroom tonClassroom = tonClassrooms.stream().findFirst().orElse(null);
        if (tonClassroom == null) {
            log.warn("Ton classroom not found");
            return;
        }

        List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByClassroomId(tonClassroom.getId());
        log.info("Ton classroom {} has {} enrolled students", tonClassroom.getName(), enrollments.size());

        User teacher = tonClassroom.getTeacher();
        if (teacher != null) {
            log.info("Ton classroom teacher: {} ({})", teacher.getFullName(), teacher.getEmail());
        } else {
            log.warn("Ton classroom has no assigned teacher");
        }
    }
}
