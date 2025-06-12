package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.AssignmentDto;
import com.classroomapp.classroombackend.dto.AssignmentRubricDto;
import com.classroomapp.classroombackend.dto.AssignmentSubmissionDto;
import com.classroomapp.classroombackend.dto.BulkGradingDto;
import com.classroomapp.classroombackend.dto.BulkGradingResultDto;
import com.classroomapp.classroombackend.dto.CreateAssignmentDto;
import com.classroomapp.classroombackend.dto.CreateFeedbackDto;
import com.classroomapp.classroombackend.dto.CreateRubricDto;
import com.classroomapp.classroombackend.dto.FeedbackDto;
import com.classroomapp.classroombackend.dto.GradeDto;
import com.classroomapp.classroombackend.dto.GradeSubmissionDto;
import com.classroomapp.classroombackend.dto.GradingAnalyticsDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Assignment;
import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.AssignmentRepository;
import com.classroomapp.classroombackend.repository.ClassroomRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.util.ModelMapper;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AssignmentServiceImpl(
            AssignmentRepository assignmentRepository,
            ClassroomRepository classroomRepository,
            UserRepository userRepository,
            ModelMapper modelMapper) {
        this.assignmentRepository = assignmentRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Find Assignment entity by ID
     */
    @Override
    public Assignment findEntityById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }

    /**
     * Get assignment by ID
     */
    @Override
    public AssignmentDto GetAssignmentById(Long id) {
        Assignment assignment = findEntityById(id);
        return modelMapper.MapToAssignmentDto(assignment);
    }

    @Override
    @Transactional
    public AssignmentDto CreateAssignment(CreateAssignmentDto createAssignmentDto) {
        // Get classroom
        Classroom classroom = classroomRepository.findById(createAssignmentDto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", createAssignmentDto.getClassroomId()));
        
        // Create assignment
        Assignment assignment = new Assignment();
        assignment.setTitle(createAssignmentDto.getTitle());
        assignment.setDescription(createAssignmentDto.getDescription());
        assignment.setDueDate(createAssignmentDto.getDueDate());
        assignment.setPoints(createAssignmentDto.getPoints());
        assignment.setFileAttachmentUrl(createAssignmentDto.getFileAttachmentUrl());
        assignment.setClassroom(classroom);
        
        // Save and return
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return modelMapper.MapToAssignmentDto(savedAssignment);
    }

    @Override
    @Transactional
    public AssignmentDto UpdateAssignment(Long id, CreateAssignmentDto updateAssignmentDto) {
        // Get assignment
        Assignment assignment = findEntityById(id);
        
        // If classroom ID has changed, get the new classroom
        if (!assignment.getClassroom().getId().equals(updateAssignmentDto.getClassroomId())) {
            Classroom classroom = classroomRepository.findById(updateAssignmentDto.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", updateAssignmentDto.getClassroomId()));
            assignment.setClassroom(classroom);
        }
        
        // Update fields
        assignment.setTitle(updateAssignmentDto.getTitle());
        assignment.setDescription(updateAssignmentDto.getDescription());
        assignment.setDueDate(updateAssignmentDto.getDueDate());
        assignment.setPoints(updateAssignmentDto.getPoints());
        assignment.setFileAttachmentUrl(updateAssignmentDto.getFileAttachmentUrl());
        
        // Save and return
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return modelMapper.MapToAssignmentDto(updatedAssignment);
    }

    @Override
    @Transactional
    public void DeleteAssignment(Long id) {
        Assignment assignment = findEntityById(id);
        assignmentRepository.delete(assignment);
    }

    @Override
    public List<AssignmentDto> GetAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        List<Assignment> assignments = assignmentRepository.findByClassroomOrderByDueDateAsc(classroom);
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> GetUpcomingAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        List<Assignment> assignments = assignmentRepository.findByClassroomAndDueDateAfterOrderByDueDateAsc(
                classroom, LocalDateTime.now());
        
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> GetPastAssignmentsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        List<Assignment> assignments = assignmentRepository.findByClassroomAndDueDateBeforeOrderByDueDateDesc(
                classroom, LocalDateTime.now());
        
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDto> SearchAssignmentsByTitle(String title) {
        List<Assignment> assignments = assignmentRepository.findByTitleContainingIgnoreCase(title);
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }
    
    // New methods to implement missing interface methods
    @Override
    public List<AssignmentSubmissionDto> getAssignmentSubmissions(Long assignmentId) {
        // Implementation for getting assignment submissions
        // This would typically query a SubmissionRepository
        // For now, return empty list to prevent compilation error
        return java.util.Arrays.asList();
    }
      @Override
    public GradeDto gradeSubmission(Long assignmentId, GradeSubmissionDto gradeSubmissionDto) {
        // Implementation for grading a submission
        // This would typically update grade in database
        // For now, return a mock GradeDto to prevent compilation error
        GradeDto gradeDto = new GradeDto();
        return gradeDto;
    }
    
    @Override
    public AssignmentRubricDto getAssignmentRubric(Long assignmentId) {
        // Implementation for getting assignment rubric
        // This would typically query a RubricRepository
        // For now, return null to prevent compilation error
        return null;
    }
    
    @Override
    public AssignmentRubricDto createAssignmentRubric(Long assignmentId, CreateRubricDto rubricDto) {
        // Implementation for creating assignment rubric
        // This would typically save to database
        // For now, return null to prevent compilation error
        return null;
    }
      @Override
    public BulkGradingResultDto bulkGradeSubmissions(Long assignmentId, BulkGradingDto bulkGradingDto) {
        // Implementation for bulk grading
        // This would typically update multiple grades
        // For now, return null to prevent compilation error
        return null;
    }
    
    @Override
    public GradingAnalyticsDto getGradingAnalytics(Long assignmentId) {
        // Implementation for grading analytics
        // This would typically calculate statistics
        // For now, return null to prevent compilation error
        return null;
    }    @Override
    public FeedbackDto provideFeedback(Long assignmentId, CreateFeedbackDto feedbackDto) {
        // Check if assignment exists
        Assignment assignment = findEntityById(assignmentId);
        
        // For now, return a basic feedback DTO with mock data
        FeedbackDto result = new FeedbackDto();
        result.setId(1L);
        result.setSubmissionId(feedbackDto.getSubmissionId());
        result.setFeedbackText(feedbackDto.getFeedbackText());
        result.setFeedbackType(feedbackDto.getFeedbackType());
        result.setAttachments(feedbackDto.getAttachments());
        result.setCreatedDate(LocalDateTime.now());
        result.setCreatedBy("System"); // Set a default created by value
        
        // In a real implementation, this would:
        // 1. Save feedback to a feedback repository
        // 2. Associate it with the assignment/submission
        // 3. Return the actual saved feedback DTO
        
        return result;
    }
    
    @Override
    public List<AssignmentDto> GetAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }
      @Override
    public List<AssignmentDto> GetAssignmentsByStudent(Long studentId) {
        // Get all classrooms the student is enrolled in
        List<Classroom> studentClassrooms = classroomRepository.findClassroomsByStudentId(studentId);
        
        // Get all assignments from those classrooms
        List<Assignment> assignments = new ArrayList<>();
        for (Classroom classroom : studentClassrooms) {
            assignments.addAll(assignmentRepository.findByClassroomOrderByDueDateAsc(classroom));
        }
        
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }
      @Override
    public List<AssignmentDto> GetAssignmentsByTeacher(Long teacherId) {
        // Get the teacher user entity
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
        
        // Get all classrooms where the user is the teacher
        List<Classroom> teacherClassrooms = classroomRepository.findByTeacher(teacher);
        
        // Get all assignments from those classrooms
        List<Assignment> assignments = new ArrayList<>();
        for (Classroom classroom : teacherClassrooms) {
            assignments.addAll(assignmentRepository.findByClassroomOrderByDueDateAsc(classroom));
        }
        
        return assignments.stream()
                .map(modelMapper::MapToAssignmentDto)
                .collect(Collectors.toList());
    }
}