package com.classroomapp.classroombackend.repository.exammangement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.exammangement.ExamSubmission;

@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {

    List<ExamSubmission> findByExamId(Long examId);

    Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId);
} 