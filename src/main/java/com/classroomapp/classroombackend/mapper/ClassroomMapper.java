package com.classroomapp.classroombackend.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

@Component
public class ClassroomMapper {

    public ClassroomDto toDto(Classroom classroom) {
        ClassroomDto dto = new ClassroomDto();
        dto.setId(classroom.getId());
        dto.setName(classroom.getName());
        dto.setDescription(classroom.getDescription());
        dto.setSection(classroom.getSection());
        dto.setSubject(classroom.getSubject());
        dto.setTeacherId(classroom.getTeacher() != null ? classroom.getTeacher().getId() : null);
        dto.setTeacherName(classroom.getTeacher() != null ? classroom.getTeacher().getFullName() : null);
        dto.setCourseId(classroom.getCourseId());

        // Timestamps
        dto.setCreatedAt(classroom.getCreatedAt());
        dto.setUpdatedAt(classroom.getUpdatedAt());

        // Map enrolled students
        if (classroom.getStudents() != null) {
            dto.setEnrolledStudents(classroom.getStudents().stream()
                    .map(student -> new UserDto(student.getId(), student.getFullName(), student.getEmail()))
                    .collect(Collectors.toList()));
        }

        // Map assignments
        if (classroom.getAssignments() != null) {
            dto.setAssignments(classroom.getAssignments().stream()
                    .map(a -> new AssignmentDto(a.getId(), a.getTitle(), a.getDueDate()))
                    .collect(Collectors.toList()));
        }

        // Derived fields
        dto.setTotalStudents(dto.getEnrolledStudents() != null ? dto.getEnrolledStudents().size() : 0);
        dto.setAssignmentCount(dto.getAssignments() != null ? dto.getAssignments().size() : 0);
        dto.setHasStudents(dto.getTotalStudents() > 0);

        return dto;
    }

    public Classroom toEntity(ClassroomDto dto) {
        Classroom classroom = new Classroom();
        classroom.setId(dto.getId());
        classroom.setName(dto.getName());
        classroom.setDescription(dto.getDescription());
        classroom.setSection(dto.getSection());
        classroom.setSubject(dto.getSubject());
        classroom.setCourseId(dto.getCourseId());
        
        return classroom;
    }
}
