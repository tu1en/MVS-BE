package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Optimized method to get only student IDs for a classroom (avoids N+1 queries)
    @Query("SELECT e.user.id FROM ClassroomEnrollment e WHERE e.classroom.id = :classroomId")
    Set<Long> findStudentIdsByClassroomId(@Param("classroomId") Long classroomId);

    // Check if a student is enrolled in a classroom (more efficient than existsById)
    @Query("SELECT COUNT(e) > 0 FROM ClassroomEnrollment e WHERE e.classroom.id = :classroomId AND e.user.id = :studentId")
    boolean isStudentEnrolledInClassroom(@Param("classroomId") Long classroomId, @Param("studentId") Long studentId);

    /**
     * Find duplicate enrollment records (same student-classroom pair)
     * Returns: classroom_id, user_id, count, classroom_name, student_name
     */
    @Query(value = """
        SELECT ce.classroom_id, ce.user_id, COUNT(*) as duplicate_count,
               c.name as classroom_name, u.full_name as student_name
        FROM classroom_enrollments ce
        JOIN classrooms c ON ce.classroom_id = c.id
        JOIN users u ON ce.user_id = u.id
        GROUP BY ce.classroom_id, ce.user_id, c.name, u.full_name
        HAVING COUNT(*) > 1
        ORDER BY duplicate_count DESC
        """, nativeQuery = true)
    List<Object[]> findDuplicateEnrollments();
}