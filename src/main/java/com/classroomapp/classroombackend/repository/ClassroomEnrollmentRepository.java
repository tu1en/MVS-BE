package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface ClassroomEnrollmentRepository extends JpaRepository<ClassroomEnrollment, Long> {
    
    // Find enrollment by user and classroom
    Optional<ClassroomEnrollment> findByUserAndClassroom(User user, Classroom classroom);
    
    // Find all enrollments by classroom
    List<ClassroomEnrollment> findByClassroom(Classroom classroom);
    
    // Find all enrollments by user
    List<ClassroomEnrollment> findByUser(User user);
}