package com.classroomapp.classroombackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.classroomapp.classroombackend.model.ClassroomEnrollment;

public interface ClassroomEnrollmentRepository extends JpaRepository<ClassroomEnrollment, Long> {
} 