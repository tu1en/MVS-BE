package com.classroomapp.classroombackend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.classroomapp.classroombackend.dto.CourseTemplateDto;
import com.classroomapp.classroombackend.dto.LessonTemplateDto;
import com.classroomapp.classroombackend.entity.LessonTemplate;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Mapper(componentModel = "spring")
public interface CourseTemplateMapper {
    
    // Main mapping methods
    @Mapping(target = "createdBy", ignore = true)
    CourseTemplateDto toDto(CourseTemplate courseTemplate);
    
    @Mapping(source = "courseTemplate.id", target = "courseTemplateId")
    @Mapping(target = "materials", ignore = true) // Temporarily ignore materials
    LessonTemplateDto lessonToDto(LessonTemplate lessonTemplate);
    
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lessonTemplates", ignore = true) // Avoid circular reference
    CourseTemplate toEntity(CourseTemplateDto courseTemplateDto);
    
    List<CourseTemplateDto> toDtoList(List<CourseTemplate> courseTemplates);
    
    List<LessonTemplateDto> lessonToDtoList(List<LessonTemplate> lessonTemplates);
    
    // Custom mapping methods to handle User <-> Long conversion
    @org.mapstruct.Named("userToLong")
    default Long mapUserToLong(User user) {
        return user != null ? user.getId() : null;
    }
    
    @org.mapstruct.Named("longToUser")
    default User mapLongToUser(Long userId) {
        if (userId == null) return null;
        User user = new User();
        user.setId(userId);
        return user;
    }
}