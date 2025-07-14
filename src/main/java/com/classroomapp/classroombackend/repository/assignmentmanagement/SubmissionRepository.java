package com.classroomapp.classroombackend.repository.assignmentmanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    // Find submission by assignment and student (returns first if multiple exist due to legacy data)
    @Query("SELECT s FROM Submission s WHERE s.assignment = :assignment AND s.student = :student ORDER BY s.submittedAt DESC")
    List<Submission> findByAssignmentAndStudentList(@Param("assignment") Assignment assignment, @Param("student") User student);
    
    // Find submission by assignment and student - for backward compatibility
    // If multiple submissions exist, returns the most recent one
    default Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student) {
        List<Submission> submissions = findByAssignmentAndStudentList(assignment, student);
        return submissions.isEmpty() ? Optional.empty() : Optional.of(submissions.get(0));
    }
    
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

    /**
     * Find submissions from students who are not enrolled in the classroom
     * Returns: submission_id, assignment_id, student_id, student_name, student_email, classroom_id, classroom_name
     */
    @Query(value = """
        SELECT s.id as submission_id, s.assignment_id, s.student_id,
               u.full_name as student_name, u.email as student_email,
               a.classroom_id, c.name as classroom_name
        FROM submissions s
        JOIN assignments a ON s.assignment_id = a.id
        JOIN users u ON s.student_id = u.id
        JOIN classrooms c ON a.classroom_id = c.id
        LEFT JOIN classroom_enrollments ce ON ce.classroom_id = a.classroom_id AND ce.user_id = s.student_id
        WHERE ce.user_id IS NULL
        ORDER BY s.assignment_id, s.student_id
        """, nativeQuery = true)
    List<Object[]> findSubmissionsFromNonEnrolledStudents();

    /**
     * Delete submissions by list of IDs
     * @param ids List of submission IDs to delete
     * @return Number of deleted submissions
     */
    @Modifying
    @Query("DELETE FROM Submission s WHERE s.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Long> ids);

    /**
     * Find submissions with non-existent assignments (orphaned records)
     * Returns: submission_id, assignment_id, student_id, student_name
     */
    @Query(value = """
        SELECT s.id as submission_id, s.assignment_id, s.student_id, u.full_name as student_name
        FROM submissions s
        JOIN users u ON s.student_id = u.id
        LEFT JOIN assignments a ON s.assignment_id = a.id
        WHERE a.id IS NULL
        ORDER BY s.id
        """, nativeQuery = true)
    List<Object[]> findSubmissionsWithNonExistentAssignments();

    /**
     * Get submission counts by classroom
     * Returns: classroom_id, classroom_name, submission_count
     */
    @Query(value = """
        SELECT c.id as classroom_id, c.name as classroom_name, COUNT(s.id) as submission_count
        FROM classrooms c
        LEFT JOIN assignments a ON a.classroom_id = c.id
        LEFT JOIN submissions s ON s.assignment_id = a.id
        GROUP BY c.id, c.name
        ORDER BY submission_count DESC
        """, nativeQuery = true)
    List<Object[]> findSubmissionCountsByClassroom();
}
