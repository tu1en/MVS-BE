package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Assignment;
import com.classroomapp.classroombackend.model.Submission;
import com.classroomapp.classroombackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    // Find submission by assignment and student
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    
    // Find all submissions for an assignment
    List<Submission> findByAssignment(Assignment assignment);
    
    // Find all submissions by student
    List<Submission> findByStudent(User student);
    
    // Find all submissions by assignment ordered by submission time
    List<Submission> findByAssignmentOrderBySubmittedAtDesc(Assignment assignment);
    
    // Find all graded submissions for an assignment
    List<Submission> findByAssignmentAndScoreIsNotNull(Assignment assignment);
    
    // Find all ungraded submissions for an assignment
    List<Submission> findByAssignmentAndScoreIsNull(Assignment assignment);
    
    // Count submissions for an assignment
    long countByAssignment(Assignment assignment);
} 