package com.classroomapp.classroombackend.util;

import com.classroomapp.classroombackend.dto.AssignmentDto;
import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.SubmissionDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.model.Assignment;
import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.Submission;
import com.classroomapp.classroombackend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Component for mapping between entities and DTOs
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelMapper {

    /**
     * Map Classroom entity to ClassroomDto
     * @param classroom Classroom entity
     * @return ClassroomDto
     */
    public ClassroomDto MapToClassroomDto(Classroom classroom) {
        if (classroom == null) {
            log.debug("Classroom entity is null, returning null DTO");
            return null;
        }
        
        log.debug("Mapping classroom with ID: {} to DTO", classroom.getId());
        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setSection(classroom.getSection());
        dto.setSubject(classroom.getSubject());
        
        // Map teacher information if available
        if (classroom.getTeacher() != null) {
            dto.setTeacherId(classroom.getTeacher().getId());
            dto.setTeacherName(classroom.getTeacher().getFullName());
            log.debug("Mapped teacher: {} (ID: {}) to classroom DTO", classroom.getTeacher().getFullName(), classroom.getTeacher().getId());
        }
        
        // Map student information
        if (classroom.getStudents() != null && !classroom.getStudents().isEmpty()) {
            dto.setStudentIds(classroom.getStudents().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet()));
            dto.setStudentCount(classroom.getStudents().size());
            log.debug("Mapped {} students to classroom DTO", classroom.getStudents().size());
        } else {
            dto.setStudentCount(0);
            log.debug("No students to map for classroom DTO");
        }
        
        return dto;
    }
    
    /**
     * Map Assignment entity to AssignmentDto
     * @param assignment Assignment entity
     * @return AssignmentDto
     */
    public AssignmentDto MapToAssignmentDto(Assignment assignment) {
        if (assignment == null) {
            log.debug("Assignment entity is null, returning null DTO");
            return null;
        }
        
        log.debug("Mapping assignment with ID: {} to DTO", assignment.getId());
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setPoints(assignment.getPoints());
        dto.setFileAttachmentUrl(assignment.getFileAttachmentUrl());
        
        // Map classroom information if available
        if (assignment.getClassroom() != null) {
            dto.setClassroomId(assignment.getClassroom().getId());
            dto.setClassroomName(assignment.getClassroom().getName());
            log.debug("Mapped classroom: {} (ID: {}) to assignment DTO", assignment.getClassroom().getName(), assignment.getClassroom().getId());
        }
        
        return dto;
    }
    
    /**
     * Map Submission entity to SubmissionDto
     * @param submission Submission entity
     * @return SubmissionDto
     */
    public SubmissionDto MapToSubmissionDto(Submission submission) {
        if (submission == null) {
            log.debug("Submission entity is null, returning null DTO");
            return null;
        }
        
        log.debug("Mapping submission with ID: {} to DTO", submission.getId());
        SubmissionDto dto = new SubmissionDto();
        dto.setId(submission.getId());
        dto.setComment(submission.getComment());
        dto.setFileSubmissionUrl(submission.getFileSubmissionUrl());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setScore(submission.getScore());
        dto.setFeedback(submission.getFeedback());
        dto.setGradedAt(submission.getGradedAt());
        
        // Map assignment information if available
        if (submission.getAssignment() != null) {
            dto.setAssignmentId(submission.getAssignment().getId());
            dto.setAssignmentTitle(submission.getAssignment().getTitle());
            
            // Check if submission is late
            LocalDateTime dueDate = submission.getAssignment().getDueDate();
            LocalDateTime submittedAt = submission.getSubmittedAt();
            if (dueDate != null && submittedAt != null) {
                dto.setIsLate(submittedAt.isAfter(dueDate));
                if (submittedAt.isAfter(dueDate)) {
                    log.debug("Submission ID: {} is late. Due: {}, Submitted: {}", submission.getId(), dueDate, submittedAt);
                }
            }
            
            log.debug("Mapped assignment: {} (ID: {}) to submission DTO", submission.getAssignment().getTitle(), submission.getAssignment().getId());
        }
        
        // Map student information if available
        if (submission.getStudent() != null) {
            dto.setStudentId(submission.getStudent().getId());
            dto.setStudentName(submission.getStudent().getFullName());
            log.debug("Mapped student: {} (ID: {}) to submission DTO", submission.getStudent().getFullName(), submission.getStudent().getId());
        }
        
        // Map grader information if available
        if (submission.getGradedBy() != null) {
            dto.setGradedById(submission.getGradedBy().getId());
            dto.setGradedByName(submission.getGradedBy().getFullName());
            log.debug("Mapped grader: {} (ID: {}) to submission DTO", submission.getGradedBy().getFullName(), submission.getGradedBy().getId());
        }
        
        // Set if submission is graded
        dto.setIsGraded(submission.getScore() != null);
        
        return dto;
    }
    
    /**
     * Map User entity to UserDto
     * @param user User entity
     * @return UserDto
     */
    public UserDto MapToUserDto(User user) {
        if (user == null) {
            log.debug("User entity is null, returning null DTO");
            return null;
        }
        
        log.debug("Mapping user with ID: {} to DTO", user.getId());
        return UserMapper.toDto(user);
    }
    
    /**
     * Map UserDto to User entity
     * @param userDto UserDto
     * @return User entity
     */
    public User MapToUserEntity(UserDto userDto) {
        if (userDto == null) {
            log.debug("UserDto is null, returning null entity");
            return null;
        }
        
        log.debug("Mapping userDto to entity. DTO ID: {}", userDto.getId());
        return UserMapper.toEntity(userDto);
    }
} 