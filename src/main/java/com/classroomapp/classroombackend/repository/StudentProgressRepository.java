package com.classroomapp.classroombackend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.StudentProgress;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    
    /**
     * Find progress for a specific student in a classroom
     */
    List<StudentProgress> findByStudentIdAndClassroomIdOrderByLastAccessedDesc(
            Long studentId, Long classroomId);
    
    /**
     * Find progress for a specific assignment
     */
    Optional<StudentProgress> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
    
    /**
     * Find all students' progress for a classroom
     */
    List<StudentProgress> findByClassroomIdOrderByStudentIdAscLastAccessedDesc(Long classroomId);
    
    /**
     * Find progress by type
     */
    List<StudentProgress> findByStudentIdAndClassroomIdAndProgressType(
            Long studentId, Long classroomId, StudentProgress.ProgressType progressType);
    
    /**
     * Get overall progress for a student in a classroom
     */
    @Query("SELECT sp FROM StudentProgress sp WHERE sp.studentId = :studentId " +
           "AND sp.classroomId = :classroomId AND sp.progressType = 'OVERALL'")
    Optional<StudentProgress> findOverallProgress(@Param("studentId") Long studentId,
                                                @Param("classroomId") Long classroomId);
    
    /**
     * Calculate average progress for a classroom
     */
    @Query("SELECT AVG(sp.progressPercentage) FROM StudentProgress sp " +
           "WHERE sp.classroomId = :classroomId AND sp.progressType = 'OVERALL'")
    BigDecimal getAverageProgressByClassroom(@Param("classroomId") Long classroomId);
    
    /**
     * Find students with low progress (below threshold)
     */
    @Query("SELECT sp FROM StudentProgress sp WHERE sp.classroomId = :classroomId " +
           "AND sp.progressType = 'OVERALL' AND sp.progressPercentage < :threshold " +
           "ORDER BY sp.progressPercentage ASC")
    List<StudentProgress> findStudentsWithLowProgress(@Param("classroomId") Long classroomId,
                                                    @Param("threshold") BigDecimal threshold);
    
    /**
     * Get total time spent by student in classroom
     */
    @Query("SELECT SUM(sp.timeSpentMinutes) FROM StudentProgress sp " +
           "WHERE sp.studentId = :studentId AND sp.classroomId = :classroomId")
    Integer getTotalTimeSpent(@Param("studentId") Long studentId, 
                            @Param("classroomId") Long classroomId);
    
    /**
     * Find all progress for a specific student across all classrooms
     */
    List<StudentProgress> findByStudentIdOrderByLastAccessedDesc(Long studentId);
    
    /**
     * Find overall progress for all classrooms of a student
     */
    @Query("SELECT sp FROM StudentProgress sp WHERE sp.studentId = :studentId " +
           "AND sp.progressType = 'OVERALL' ORDER BY sp.lastAccessed DESC")
    List<StudentProgress> findOverallProgressByStudent(@Param("studentId") Long studentId);
}
