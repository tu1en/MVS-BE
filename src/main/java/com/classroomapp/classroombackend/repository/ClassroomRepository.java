package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    
    // Find classrooms where user is a teacher
    List<Classroom> findByTeacher(User teacher);
    
    // Find classrooms where user is enrolled as a student
    List<Classroom> findByStudentsContaining(User student);
    
    // Find classrooms by subject
    List<Classroom> findBySubject(String subject);
    
    // Find classrooms by name containing search term (case-insensitive)
    List<Classroom> findByNameContainingIgnoreCase(String name);
} 