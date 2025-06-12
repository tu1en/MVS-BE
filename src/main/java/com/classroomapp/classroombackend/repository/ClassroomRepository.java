package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.User;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    
    // Find classrooms where user is a teacher
    List<Classroom> findByTeacher(User teacher);
    
    // Find classrooms where user is enrolled as a student
    List<Classroom> findByStudentsContaining(User student);
    
    // Find classrooms by subject
    List<Classroom> findBySubject(String subject);
    
    // Find classrooms by name containing search term (case-insensitive)
    List<Classroom> findByNameContainingIgnoreCase(String name);    // Find classrooms by student ID using the students relationship
    @Query("SELECT c FROM Classroom c JOIN c.students s WHERE s.id = :studentId")
    List<Classroom> findClassroomsByStudentId(@Param("studentId") Long studentId);
}