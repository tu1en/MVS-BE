package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
@Transactional
public class ClassroomEnrollmentSeeder {

    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    public void seed() {
        if (classroomEnrollmentRepository.count() == 0) {
            List<User> students = userRepository.findAllByRoleId(1L);
            List<Classroom> classrooms = classroomRepository.findAll();

            if (students.isEmpty() || classrooms.isEmpty()) {
                System.out.println("⚠️ [ClassroomEnrollmentSeeder] No students or classrooms found. Skipping.");
                return;
            }

            System.out.println("ℹ️ [ClassroomEnrollmentSeeder] Found " + students.size() + " students and " + classrooms.size() + " classrooms");

            // Enroll EVERY student in EVERY classroom to ensure they have data
            int totalEnrollments = 0;
            for (User student : students) {
                for (Classroom classroom : classrooms) {
                    enrollStudent(classroom, student);
                    totalEnrollments++;
                }
            }

            System.out.println("✅ [ClassroomEnrollmentSeeder] Created " + totalEnrollments + " total enrollments");
            System.out.println("✅ [ClassroomEnrollmentSeeder] Each of " + students.size() + " students enrolled in all " + classrooms.size() + " classrooms");

            // Verify enrollments for debugging
            for (User student : students) {
                List<Classroom> studentClassrooms = classroomRepository.findClassroomsByStudentId(student.getId());
                System.out.println("📊 [ClassroomEnrollmentSeeder] Student " + student.getFullName() + " (ID: " + student.getId() + ") enrolled in " + studentClassrooms.size() + " classrooms");
            }

        } else {
            System.out.println("✅ [ClassroomEnrollmentSeeder] Classroom enrollments already seeded");

            // Still verify current enrollments for debugging
            List<User> students = userRepository.findAllByRoleId(1L);
            for (User student : students) {
                List<Classroom> studentClassrooms = classroomRepository.findClassroomsByStudentId(student.getId());
                System.out.println("📊 [ClassroomEnrollmentSeeder] Student " + student.getFullName() + " (ID: " + student.getId() + ") enrolled in " + studentClassrooms.size() + " classrooms");
            }
        }
    }

    private void enrollStudent(Classroom classroom, User student) {
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroom.getId(), student.getId());
        if (!classroomEnrollmentRepository.existsById(enrollmentId)) {
            ClassroomEnrollment enrollment = new ClassroomEnrollment();
            enrollment.setId(enrollmentId);
            enrollment.setClassroom(classroom);
            enrollment.setUser(student);
            classroomEnrollmentRepository.save(enrollment);
        }
    }
} 