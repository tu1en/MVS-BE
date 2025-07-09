package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.AssignmentRubricDto;
import com.classroomapp.classroombackend.dto.AssignmentSubmissionDto;
import com.classroomapp.classroombackend.dto.BulkGradingDto;
import com.classroomapp.classroombackend.dto.BulkGradingResultDto;
import com.classroomapp.classroombackend.dto.CreateFeedbackDto;
import com.classroomapp.classroombackend.dto.CreateRubricDto;
import com.classroomapp.classroombackend.dto.FeedbackDto;
import com.classroomapp.classroombackend.dto.GradeDto;
import com.classroomapp.classroombackend.dto.GradingAnalyticsDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateAssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.GradeSubmissionDto;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;

public interface AssignmentService {
    
    // Get an assignment entity by ID (for internal use)
    Assignment findEntityById(Long id);
    
    // Get an assignment by ID
    AssignmentDto GetAssignmentById(Long id);
    
    // Create a new assignment
    AssignmentDto CreateAssignment(CreateAssignmentDto createAssignmentDto, String teacherUsername);
    
    // Update an existing assignment
    AssignmentDto UpdateAssignment(Long id, CreateAssignmentDto updateAssignmentDto);
    
    // Delete an assignment
    void DeleteAssignment(Long id);
    
    // Get all assignments for a classroom
    List<AssignmentDto> GetAssignmentsByClassroom(Long classroomId);
    
    // Get all assignments (for admin/overview purposes)
    List<AssignmentDto> GetAllAssignments();
      // Get assignments for a specific student (enrolled classrooms)
    List<AssignmentDto> GetAssignmentsByStudent(Long studentId);
    
    // Get assignments for the current teacher
    List<AssignmentDto> getAssignmentsByCurrentTeacher();

    // Get assignments for the current student
    List<AssignmentDto> getAssignmentsByCurrentStudent();

    // Get assignments for a specific teacher (their classrooms)
    List<AssignmentDto> getAssignmentsByTeacher(Long teacherId);
    
    // Additional methods needed by FrontendApiBridgeController
    List<AssignmentDto> findByTeacherId(Long teacherId);
    List<AssignmentDto> findByStudentId(Long studentId);
    List<AssignmentDto> getAllAssignments();
    
    // Get upcoming assignments for a classroom
    List<AssignmentDto> GetUpcomingAssignmentsByClassroom(Long classroomId);
    
    // Get past assignments for a classroom
    List<AssignmentDto> GetPastAssignmentsByClassroom(Long classroomId);
    
    // Search for assignments by title
    List<AssignmentDto> SearchAssignmentsByTitle(String title);
    
    // Additional methods needed by AssignmentController
    
    // Get assignment submissions for grading
    List<AssignmentSubmissionDto> getAssignmentSubmissions(Long assignmentId);
    
    // Grade a submission
    GradeDto gradeSubmission(Long assignmentId, GradeSubmissionDto gradeSubmissionDto);
    
    // Get assignment rubric
    AssignmentRubricDto getAssignmentRubric(Long assignmentId);
    
    // Create assignment rubric
    AssignmentRubricDto createAssignmentRubric(Long assignmentId, CreateRubricDto createRubricDto);
    
    // Bulk grade submissions
    BulkGradingResultDto bulkGradeSubmissions(Long assignmentId, BulkGradingDto bulkGradingDto);
    
    // Get grading analytics
    GradingAnalyticsDto getGradingAnalytics(Long assignmentId);
    
    // Provide feedback
    FeedbackDto provideFeedback(Long assignmentId, CreateFeedbackDto createFeedbackDto);
}