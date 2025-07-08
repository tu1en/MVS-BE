package com.classroomapp.classroombackend.repository.assignmentmanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    // Find submission by assignment and student
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    
    // Find all submissions for an assignment
    List<Submission> findByAssignment(Assignment assignment);
    
    // Find all submissions by student
    List<Submission> findByStudent(User student);
    
    // Find submissions by assignment ordered by submission date
    List<Submission> findByAssignmentOrderBySubmittedAtDesc(Assignment assignment);
    
    // Find graded submissions for an assignment
    List<Submission> findByAssignmentAndScoreIsNotNull(Assignment assignment);
    
    // Find ungraded submissions for an assignment
    List<Submission> findByAssignmentAndScoreIsNull(Assignment assignment);
    
    // Find late submissions (submitted after due date)
    @Query("SELECT s FROM Submission s WHERE s.assignment = :assignment AND s.submittedAt > s.assignment.dueDate")
    List<Submission> findLateSubmissionsByAssignment(@Param("assignment") Assignment assignment);
    
    // Find submissions by student with score
    List<Submission> findByStudentAndScoreIsNotNull(User student);
    
    // Find submissions by student for specific assignment
    List<Submission> findByStudentAndAssignment(User student, Assignment assignment);
    
    // Count submissions for an assignment
    long countByAssignment(Assignment assignment);
    
    // Count graded submissions for an assignment
    long countByAssignmentAndScoreIsNotNull(Assignment assignment);
    
    // Get average score for an assignment
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignment = :assignment AND s.score IS NOT NULL")
    Optional<Double> getAverageScoreByAssignment(@Param("assignment") Assignment assignment);

    @Query("SELECT s FROM Submission s JOIN FETCH s.assignment JOIN FETCH s.student WHERE s.student = :student ORDER BY s.submittedAt DESC")
    List<Submission> findByStudentWithDetails(@Param("student") User student);

    List<Submission> findByAssignmentId(Long assignmentId);
    
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    
    List<Submission> findByStudentId(Long studentId);

    @Query("SELECT count(s) FROM Submission s WHERE s.assignment.classroom.id IN :classroomIds AND s.score IS NOT NULL")
    long countGradedSubmissionsByClassroomIds(@Param("classroomIds") List<Long> classroomIds);

    @Query("SELECT count(s) FROM Submission s WHERE s.assignment.classroom.id IN :classroomIds AND s.score IS NULL")
    long countPendingSubmissionsByClassroomIds(@Param("classroomIds") List<Long> classroomIds);
}
