package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Find classroom IDs by student ID (for schedule service)
    @Query("SELECT c.id FROM Classroom c JOIN c.enrollments e WHERE e.user.id = :studentId")
    List<Long> findClassroomsIdsByStudentId(@Param("studentId") Long studentId);

    // Additional methods for Classroom & Slot Management

    // Search classrooms by name containing keyword (with pagination)
    @Query("SELECT c FROM Classroom c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Classroom> findByClassroomNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Check if classroom name exists for a specific teacher
    @Query("SELECT COUNT(c) > 0 FROM Classroom c WHERE c.name = :name AND c.createdBy.id = :teacherId")
    boolean existsByClassroomNameAndTeacherId(@Param("name") String name, @Param("teacherId") Long teacherId);

    // Count classrooms by teacher ID
    @Query("SELECT COUNT(c) FROM Classroom c WHERE c.createdBy.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    // Find classrooms by student ID (thông qua enrollment relationship)
    @Query("SELECT c FROM Classroom c JOIN c.enrollments e WHERE e.user.id = :studentId")
    List<Classroom> findByStudents_Id(@Param("studentId") Long studentId);
}
