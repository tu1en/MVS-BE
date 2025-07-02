package com.classroomapp.classroombackend.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.dto.AttendanceDto;
import com.classroomapp.classroombackend.dto.AttendanceSessionDto;
// Import với tên đầy đủ để tránh xung đột
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.model.usermanagement.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    
    /**
     * Map Classroom entity to ClassroomDetailsDto with syllabus and schedule
     * @param classroom Classroom entity
     * @param syllabus Syllabus entity
     * @param schedules List of Schedule entities
     * @return ClassroomDetailsDto
     */
    public ClassroomDetailsDto MapToClassroomDetailsDto(Classroom classroom, Syllabus syllabus, List<com.classroomapp.classroombackend.model.classroommanagement.ClassroomSchedule> schedules) {
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
        
        // Map schedules information if available (using full qualified name)
        if (schedules != null && !schedules.isEmpty()) {
            List<com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto> scheduleDtos = 
                schedules.stream()
                    .map(schedule -> {
                        // Manual mapping for each Schedule entity
                        com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto scheduleDto = new com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto();
                        scheduleDto.setId(schedule.getId());
                        scheduleDto.setDayOfWeek(schedule.getDayOfWeek());
                        scheduleDto.setStartTime(schedule.getStartTime());
                        scheduleDto.setEndTime(schedule.getEndTime());
                        scheduleDto.setLocation(schedule.getLocation());
                        scheduleDto.setNotes(schedule.getNotes());
                        scheduleDto.setRecurring(schedule.isRecurring());
                        
                        if (schedule.getClassroom() != null) {
                            scheduleDto.setClassroomId(schedule.getClassroom().getId());
                        }
                        
                        return scheduleDto;
                    })
                    .collect(Collectors.toList());
            
            dto.setSchedules(scheduleDtos);
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
     * Map Schedule entity from model package to ScheduleDto
     * @param schedule Schedule entity from model package
     * @return ScheduleDto
     */
    public ScheduleDto MapToScheduleDto(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        
        int studentCount = 0;
        try {
            studentCount = schedule.getClassroom().getStudents().size();
        } catch (Exception e) {
            // In case students collection is null or empty
            studentCount = 0;
        }
        
        return new ScheduleDto(
            schedule.getId(),
            schedule.getTeacher().getId(),
            schedule.getTeacher().getFullName(),
            schedule.getClassroom().getId(),
            schedule.getClassroom().getName(),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.getRoom(),
            schedule.getSubject(),
            schedule.getMaterialsUrl(),
            schedule.getMeetUrl(),
            studentCount
        );
    }
      /**
     * Maps classroommanagement.ClassroomSchedule to classroommanagement.ScheduleDto
     */public com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto MapToClassroomScheduleDto(
            com.classroomapp.classroombackend.model.classroommanagement.ClassroomSchedule schedule) {
        
        if (schedule == null) {
            return null;
        }
        
        com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto dto = 
            new com.classroomapp.classroombackend.dto.classroommanagement.ScheduleDto();
        
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
        dto.setDepartment(user.getDepartment());        dto.setStatus(user.getStatus());
        
        return dto;
    }

    /**
     * Map Attendance entity to AttendanceDto
     * @param attendance Attendance entity
     * @return AttendanceDto
     */
    public AttendanceDto MapToAttendanceDto(Attendance attendance) {
        if (attendance == null) {
            log.debug("Attendance is null, returning null DTO");
            return null;
        }
        
        log.debug("Mapping attendance entity to DTO. Entity ID: {}", attendance.getId());
          return AttendanceDto.builder()
                .id(attendance.getId())                .userId(attendance.getStudent().getId())                .userName(attendance.getStudent().getFullName() != null ? attendance.getStudent().getFullName() : attendance.getStudent().getUsername())
                .sessionId(attendance.getSession().getId())
                .status(attendance.getStatus())
                .markedAt(attendance.getCheckInTime())
                .latitude(attendance.getLatitude())
                .longitude(attendance.getLongitude())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
    
    /**
     * Map AttendanceSession entity to AttendanceSessionDto
     * @param session AttendanceSession entity
     * @return AttendanceSessionDto
     */
    public AttendanceSessionDto MapToAttendanceSessionDto(AttendanceSession session) {
        if (session == null) {
            log.debug("AttendanceSession is null, returning null DTO");
            return null;
        }        
        log.debug("Mapping attendance session entity to DTO. Entity ID: {}", session.getId());
          return AttendanceSessionDto.builder()
                .id(session.getId())
                .classroomId(session.getClassroom().getId())
                .classroomName(session.getClassroom().getName())
                .teacherId(session.getTeacher().getId())
                .teacherName(session.getTeacher().getFullName() != null ? session.getTeacher().getFullName() : session.getTeacher().getUsername())
                .sessionName(session.getSessionName())
                .sessionDate(session.getSessionDate())
                .description(session.getDescription())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus().name())
                .locationRequired(session.getLocationLatitude() != null && session.getLocationLongitude() != null)
                .locationLatitude(session.getLocationLatitude())
                .locationLongitude(session.getLocationLongitude())
                .locationRadiusMeters(session.getLocationRadiusMeters())
                .autoMarkTeacherAttendance(session.isAutoMarkTeacherAttendance())
                .attendanceRecords(null) // AttendanceSession doesn't have attendanceRecords field
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getCreatedAt()) // AttendanceSession doesn't have updatedAt field, using createdAt
                .build();
    }
}