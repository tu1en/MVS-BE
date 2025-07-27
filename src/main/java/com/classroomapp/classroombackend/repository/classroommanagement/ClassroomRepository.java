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
    
    // Basic queries by teacher
    List<Classroom> findByTeacher(User teacher);
    
    @Query("SELECT DISTINCT c FROM Classroom c WHERE c.teacher.id = :teacherId")
    List<Classroom> findByTeacherId(@Param("teacherId") Long teacherId);
    
    // Queries for student enrollments - using JOIN with enrollments table
    @Query("SELECT DISTINCT c FROM Classroom c JOIN c.enrollments e WHERE e.user = :student")
    List<Classroom> findByStudentsContaining(@Param("student") User student);

    @Query("SELECT DISTINCT c FROM Classroom c JOIN c.enrollments e WHERE e.user.id = :studentId")
    List<Classroom> findByStudents_Id(@Param("studentId") Long studentId);

    @Query("SELECT DISTINCT c FROM Classroom c JOIN c.enrollments e WHERE e.user.id = :studentId")
    List<Classroom> findClassroomsByStudentId(@Param("studentId") Long studentId);
    
    // Alternative query using enrollment repository approach
    @Query("SELECT DISTINCT c FROM Classroom c JOIN ClassroomEnrollment ce ON c.id = ce.classroom.id WHERE ce.user.id = :studentId")
    List<Classroom> findClassroomsByStudentIdAlternative(@Param("studentId") Long studentId);
    
    // Get classroom IDs only (for performance)
    @Query("SELECT c.id FROM Classroom c JOIN c.enrollments e WHERE e.user.id = :studentId")
    List<Long> findClassroomsIdsByStudentId(@Param("studentId") Long studentId);
    
    // Search queries
    List<Classroom> findBySubject(String subject);
    List<Classroom> findByNameContainingIgnoreCase(String name);
    Page<Classroom> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Fetch with relationships for performance
    @Query("SELECT DISTINCT c FROM Classroom c LEFT JOIN FETCH c.enrollments e LEFT JOIN FETCH e.user WHERE c.teacher = :teacher")
    List<Classroom> findByTeacherWithStudents(@Param("teacher") User teacher);
    
    @Query("SELECT DISTINCT c FROM Classroom c LEFT JOIN FETCH c.enrollments e LEFT JOIN FETCH e.user WHERE c.teacher.id = :teacherId")
    List<Classroom> findByTeacherIdWithStudents(@Param("teacherId") Long teacherId);

    @Query("SELECT DISTINCT c FROM Classroom c LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.lectures WHERE c.id = :classroomId")
    Optional<Classroom> findDetailsById(@Param("classroomId") Long classroomId);

    // Enhanced query with all necessary joins for detailed view
    @Query("SELECT DISTINCT c FROM Classroom c " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.enrollments e " +
           "LEFT JOIN FETCH e.user " +
           "LEFT JOIN FETCH c.assignments " +
           "LEFT JOIN FETCH c.lectures " +
           "WHERE c.id = :classroomId")
    Optional<Classroom> findDetailsByIdWithAllRelations(@Param("classroomId") Long classroomId);

    // Performance optimized queries for dashboard
    @Query("SELECT c.id, c.name, c.description, c.subject, c.section, " +
           "c.teacher.id, c.teacher.fullName, " +
           "COUNT(e.id) as studentCount " +
           "FROM Classroom c " +
           "LEFT JOIN c.enrollments e " +
           "WHERE c.teacher.id = :teacherId " +
           "GROUP BY c.id, c.name, c.description, c.subject, c.section, c.teacher.id, c.teacher.fullName")
    List<Object[]> findTeacherClassroomsSummary(@Param("teacherId") Long teacherId);

    @Query("SELECT c.id, c.name, c.description, c.subject, c.section, " +
           "c.teacher.id, c.teacher.fullName " +
           "FROM Classroom c " +
           "JOIN c.enrollments e " +
           "WHERE e.user.id = :studentId")
    List<Object[]> findStudentClassroomsSummary(@Param("studentId") Long studentId);

    // Check if student is enrolled
    @Query("SELECT COUNT(e) > 0 FROM Classroom c JOIN c.enrollments e WHERE c.id = :classroomId AND e.user.id = :studentId")
    boolean isStudentEnrolled(@Param("classroomId") Long classroomId, @Param("studentId") Long studentId);

    // Count students in classroom
    @Query("SELECT COUNT(e) FROM Classroom c JOIN c.enrollments e WHERE c.id = :classroomId")
    Long countStudentsInClassroom(@Param("classroomId") Long classroomId);

    // Find classrooms with active status (if you have status field)
    @Query("SELECT c FROM Classroom c WHERE c.teacher.id = :teacherId AND c.status = 'ACTIVE'")
    List<Classroom> findActiveClassroomsByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT c FROM Classroom c JOIN c.enrollments e WHERE e.user.id = :studentId AND c.status = 'ACTIVE'")
    List<Classroom> findActiveClassroomsByStudentId(@Param("studentId") Long studentId);
}