package com.classroomapp.classroombackend.mapper;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;

@Component
public class AssignmentMapper {

    public static AssignmentDto toDto(Assignment assignment) {
        if (assignment == null) {
            return null;
        }

        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setPoints(assignment.getPoints());
        
        // Map classroom information
        if (assignment.getClassroom() != null) {
            dto.setClassroomId(assignment.getClassroom().getId());
            dto.setClassroomName(assignment.getClassroom().getName());
            dto.setSubject(assignment.getClassroom().getSubject());
        }
        
        // Map file attachment if exists
        if (assignment.getAttachments() != null && !assignment.getAttachments().isEmpty()) {
            dto.setFileAttachmentUrl(assignment.getAttachments().get(0).getFileUrl());
        }

        return dto;
    }

    public static Assignment toEntity(AssignmentDto dto) {
        if (dto == null) {
            return null;
        }

        Assignment assignment = new Assignment();
        assignment.setId(dto.getId());
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setPoints(dto.getPoints());

        return assignment;
    }
}