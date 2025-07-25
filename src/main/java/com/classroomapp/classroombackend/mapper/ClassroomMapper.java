package com.classroomapp.classroombackend.mapper;

import java.util.List;
import java.util.Set; // Add this import
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDTO;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

@Component
public class ClassroomMapper {

    public ClassroomDto toDto(Classroom classroom) {
        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setClassroomName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setTeacherId(classroom.getTeacher() != null ? classroom.getTeacher().getId() : null);
        dto.setTeacherName(classroom.getTeacher() != null ? classroom.getTeacher().getFullName() : null);
        dto.setSection(classroom.getSection());
        dto.setSubject(classroom.getSubject());
        dto.setCreatedAt(classroom.getCreatedAt());
        dto.setUpdatedAt(classroom.getUpdatedAt());

        // If there are relationships with Students and Assignments
        dto.setEnrolledStudents(classroom.getStudents() != null ?
            classroom.getStudents()
                .stream()
                .map(student -> new UserDTO(student.getId(), student.getFullName(), student.getEmail()))
                .collect(Collectors.toList()) : List.of());

        dto.setAssignments(classroom.getAssignments() != null ?
            ((Set<com.classroomapp.classroombackend.model.assignmentmanagement.Assignment>) classroom.getAssignments())
                .stream()
                .map(a -> new AssignmentDto(a.getId(), a.getTitle(), a.getDueDate()))
                .collect(Collectors.toList()) : List.of());

        dto.setTotalStudents(dto.getEnrolledStudents().size());
        dto.setAssignmentCount(dto.getAssignments().size());

        // Default initially
        dto.setHasStudents(!dto.getEnrolledStudents().isEmpty());

        return dto;
    }
}
