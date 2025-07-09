package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface ClassroomEnrollmentRepository extends JpaRepository<ClassroomEnrollment, ClassroomEnrollmentId> {
    
    // Find an enrollment by the combination of classroom and user objects
    Optional<ClassroomEnrollment> findByClassroomAndUser(Classroom classroom, User user);
    
    // Find all enrollments for a specific user by their ID
    List<ClassroomEnrollment> findById_UserId(Long userId);
    
    // Find all enrollments for a specific classroom by its ID
    List<ClassroomEnrollment> findById_ClassroomId(Long classroomId);
    
    List<ClassroomEnrollment> findByUser_Id(Long studentId);

    Optional<ClassroomEnrollment> findByClassroomIdAndUserId(Long classroomId, Long studentId);

    List<ClassroomEnrollment> findByClassroomId(Long classroomId);

    List<ClassroomEnrollment> findByUserId(Long studentId);
}