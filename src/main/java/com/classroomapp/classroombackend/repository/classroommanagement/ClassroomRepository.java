package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByTeacher(User teacher);

    @Query("SELECT c FROM Classroom c JOIN c.enrollments e WHERE e.user = :student")
    List<Classroom> findByStudentsContaining(@Param("student") User student);
    
    // Find classrooms by subject
    List<Classroom> findBySubject(String subject);
    
    // Find classrooms by name containing search term (case-insensitive)
    List<Classroom> findByNameContainingIgnoreCase(String name);

    // Find classrooms by student ID using the enrollments relationship
    @Query("SELECT c FROM Classroom c JOIN c.enrollments e WHERE e.user.id = :studentId")
    List<Classroom> findClassroomsByStudentId(@Param("studentId") Long studentId);
    
    // Optimized query to fetch classrooms with enrollments and students in one query to avoid N+1 problem
    @Query("SELECT DISTINCT c FROM Classroom c LEFT JOIN FETCH c.enrollments e LEFT JOIN FETCH e.user WHERE c.teacher = :teacher")
    List<Classroom> findByTeacherWithStudents(@Param("teacher") User teacher);
    
    // Alternative query using teacher ID directly
    @Query("SELECT DISTINCT c FROM Classroom c LEFT JOIN FETCH c.enrollments e LEFT JOIN FETCH e.user WHERE c.teacher.id = :teacherId")
    List<Classroom> findByTeacherIdWithStudents(@Param("teacherId") Long teacherId);
    
    // Alternative query using teacher ID directly
    @Query("SELECT DISTINCT c FROM Classroom c WHERE c.teacher.id = :teacherId")
    List<Classroom> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.lectures WHERE c.id = :classroomId")
    Optional<Classroom> findDetailsById(@Param("classroomId") Long classroomId);
}
