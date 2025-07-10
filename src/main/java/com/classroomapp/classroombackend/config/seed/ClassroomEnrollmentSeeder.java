package com.classroomapp.classroombackend.config.seed;

import java.util.List;
import java.util.stream.Collectors;

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
            
            // Find specific classes by name (partial match)
            Classroom mathClass = findClassroomByPartialName(classrooms, "Toán");
            Classroom litClass = findClassroomByPartialName(classrooms, "Văn");
            Classroom engClass = findClassroomByPartialName(classrooms, "Anh");
            
            if (mathClass == null) {
                System.out.println("⚠️ [ClassroomEnrollmentSeeder] Math class not found");
            } else {
                System.out.println("✅ [ClassroomEnrollmentSeeder] Found Math class: " + mathClass.getName());
            }
            
            if (litClass == null) {
                System.out.println("⚠️ [ClassroomEnrollmentSeeder] Literature class not found");
            } else {
                System.out.println("✅ [ClassroomEnrollmentSeeder] Found Literature class: " + litClass.getName());
            }
            
            if (engClass == null) {
                System.out.println("⚠️ [ClassroomEnrollmentSeeder] English class not found");
            } else {
                System.out.println("✅ [ClassroomEnrollmentSeeder] Found English class: " + engClass.getName());
            }

            // Enroll all students in Math class (everyone needs math)
            if (mathClass != null) {
                for (User student : students) {
                    enrollStudent(mathClass, student);
                }
                System.out.println("✅ [ClassroomEnrollmentSeeder] Enrolled " + students.size() + " students in Math class");
            }

            // Enroll most students in Literature
            if (litClass != null) {
                for (int i = 0; i < Math.min(4, students.size()); i++) {
                    User student = students.get(i);
                    enrollStudent(litClass, student);
                }
                System.out.println("✅ [ClassroomEnrollmentSeeder] Enrolled first 4 students in Literature class");
            }

            // Enroll some students in English
            if (engClass != null) {
                for (int i = 1; i < Math.min(5, students.size()); i++) {
                    User student = students.get(i);
                    enrollStudent(engClass, student);
                }
                System.out.println("✅ [ClassroomEnrollmentSeeder] Enrolled students 2-5 in English class");
            }

            // Enroll students in remaining classes
            List<Classroom> otherClasses = classrooms.stream()
                    .filter(c -> !c.equals(mathClass) && !c.equals(litClass) && !c.equals(engClass))
                    .collect(Collectors.toList());
            
            if (!otherClasses.isEmpty()) {
                for (int i = 0; i < Math.min(otherClasses.size(), students.size()); i++) {
                    User student = students.get(i % students.size());
                    Classroom classroom = otherClasses.get(i % otherClasses.size());
                    enrollStudent(classroom, student);
                }
                System.out.println("✅ [ClassroomEnrollmentSeeder] Enrolled students in " + otherClasses.size() + " other classes");
            }

            System.out.println("✅ [ClassroomEnrollmentSeeder] Created classroom enrollments for students across all classes");
        } else {
            System.out.println("✅ [ClassroomEnrollmentSeeder] Classroom enrollments already seeded");
        }
    }
    
    private Classroom findClassroomByPartialName(List<Classroom> classrooms, String partialName) {
        return classrooms.stream()
                .filter(c -> c.getName().contains(partialName))
                .findFirst()
                .orElse(null);
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