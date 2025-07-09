package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.exammangement.CreateExamSubmissionDto;
import com.classroomapp.classroombackend.dto.exammangement.ExamSubmissionDto;
import com.classroomapp.classroombackend.dto.exammangement.GradeExamDto;

public interface ExamSubmissionService {

    /**
     * Creates a new submission record when a student starts an exam.
     * @param examId The ID of the exam being started.
     * @return The created exam submission.
     */
    ExamSubmissionDto startExam(Long examId);

    /**
     * Submits the answers for an ongoing exam submission.
     * @param submissionId The ID of the submission record created by startExam.
     * @param submissionDto The DTO containing the submission content.
     * @return The updated exam submission.
     */
    ExamSubmissionDto submitExam(Long submissionId, CreateExamSubmissionDto submissionDto);

    /**
     * Retrieves all submissions for a specific exam. (For teachers/admins)
     * @param examId The ID of the exam.
     * @return A list of all submissions.
     */
    List<ExamSubmissionDto> getSubmissionsForExam(Long examId);

    /**
     * Retrieves a student's submission for a specific exam.
     * @param examId The ID of the exam.
     * @return The student's submission.
     */
    ExamSubmissionDto getStudentSubmissionForExam(Long examId);

    /**
     * Grades an exam submission. (For teachers/admins)
     * @param submissionId The ID of the submission to grade.
     * @param gradeDto The DTO containing the grade and feedback.
     * @return The graded submission.
     */
    ExamSubmissionDto gradeSubmission(Long submissionId, GradeExamDto gradeDto);

} 