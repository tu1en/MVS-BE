package com.classroomapp.classroombackend.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.model.usermanagement.User;

import lombok.RequiredArgsConstructor;

/**
 * Component for mapping between entities and DTOs
 */
@Component
@RequiredArgsConstructor
public class ModelMapper {

    /**
     * Map Classroom entity to ClassroomDto
     * @param classroom Classroom entity
     * @return ClassroomDto
     */
    public ClassroomDto MapToClassroomDto(Classroom classroom) {
        if (classroom == null) {
            return null;
        }
        
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
        }
        
        // Map student information
        if (classroom.getStudents() != null && !classroom.getStudents().isEmpty()) {
            dto.setStudentIds(classroom.getStudents().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet()));
            dto.setStudentCount(classroom.getStudents().size());
        } else {
            dto.setStudentCount(0);
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
            return null;
        }
        
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
            return null;
        }
        
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
            }
        }
        
        // Map student information if available
        if (submission.getStudent() != null) {
            dto.setStudentId(submission.getStudent().getId());
            dto.setStudentName(submission.getStudent().getFullName());
        }
        
        // Map grader information if available
        if (submission.getGradedBy() != null) {
            dto.setGradedById(submission.getGradedBy().getId());
            dto.setGradedByName(submission.getGradedBy().getFullName());
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
        return UserMapper.toDto(user);
    }
    
    /**
     * Map UserDto to User entity
     * @param userDto UserDto
     * @return User entity
     */
    public User MapToUserEntity(UserDto userDto) {
        return UserMapper.toEntity(userDto);
    }
    
    /**
     * Map Classroom entity to ClassroomDetailsDto with syllabus and schedule
     * @param classroom Classroom entity
     * @param syllabus Syllabus entity
     * @param schedules List of Schedule entities
     * @return ClassroomDetailsDto
     */
    public ClassroomDetailsDto MapToClassroomDetailsDto(Classroom classroom, Syllabus syllabus, List<Schedule> schedules) {
        if (classroom == null) {
            return null;
        }
        
        ClassroomDetailsDto dto = new ClassroomDetailsDto();
        dto.setId(classroom.getId());
        dto.setName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setSection(classroom.getSection());
        dto.setSubject(classroom.getSubject());
        
        // Map teacher detailed information if available
        if (classroom.getTeacher() != null) {
            dto.setTeacher(MapToUserDetailsDto(classroom.getTeacher()));
        }
        
        // Map student information
        if (classroom.getStudents() != null && !classroom.getStudents().isEmpty()) {
            dto.setStudentIds(classroom.getStudents().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet()));
            dto.setStudentCount(classroom.getStudents().size());
        } else {
            dto.setStudentCount(0);
        }
        
        // Map syllabus information if available
        if (syllabus != null) {
            dto.setSyllabus(MapToSyllabusDto(syllabus));
        }
        
        // Map schedules information if available
        if (schedules != null && !schedules.isEmpty()) {
            dto.setSchedules(schedules.stream()
                    .map(this::MapToScheduleDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Map Syllabus entity to SyllabusDto
     * @param syllabus Syllabus entity
     * @return SyllabusDto
     */
    public SyllabusDto MapToSyllabusDto(Syllabus syllabus) {
        if (syllabus == null) {
            return null;
        }
        
        SyllabusDto dto = new SyllabusDto();
        dto.setId(syllabus.getId());
        dto.setTitle(syllabus.getTitle());
        dto.setContent(syllabus.getContent());
        dto.setLearningObjectives(syllabus.getLearningObjectives());
        dto.setRequiredMaterials(syllabus.getRequiredMaterials());
        dto.setGradingCriteria(syllabus.getGradingCriteria());
        
        if (syllabus.getClassroom() != null) {
            dto.setClassroomId(syllabus.getClassroom().getId());
        }
        
        return dto;
    }
    
    /**
     * Map Schedule entity to ScheduleDto
     * @param schedule Schedule entity
     * @return ScheduleDto
     */
    public ScheduleDto MapToScheduleDto(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setLocation(schedule.getLocation());
        dto.setNotes(schedule.getNotes());
        dto.setRecurring(schedule.isRecurring());
        
        if (schedule.getClassroom() != null) {
            dto.setClassroomId(schedule.getClassroom().getId());
        }
        
        return dto;
    }
    
    /**
     * Map User entity to UserDetailsDto
     * @param user User entity
     * @return UserDetailsDto
     */
    public UserDetailsDto MapToUserDetailsDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDetailsDto dto = new UserDetailsDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setRoleId(user.getRoleId());
        dto.setHireDate(user.getHireDate());
        dto.setDepartment(user.getDepartment());
        dto.setStatus(user.getStatus());
        
        return dto;
    }
}