package com.classroomapp.classroombackend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.classroomapp.classroombackend.dto.assignmentmanagement.SubmissionDto;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configure ModelMapper to be more lenient and handle ambiguity
        modelMapper.getConfiguration()
            .setSkipNullEnabled(true)
            .setAmbiguityIgnored(true)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Custom mapping for Submission to SubmissionDto
        modelMapper.addMappings(new PropertyMap<Submission, SubmissionDto>() {
            @Override
            protected void configure() {
                // Fix: Properly map isGraded based on score existence
                using(ctx -> {
                    Integer score = (Integer) ctx.getSource();
                    return score != null;
                }).map(source.getScore(), destination.getIsGraded());

                // Map assignment title
                map().setAssignmentTitle(source.getAssignment().getTitle());

                // Map student name
                map().setStudentName(source.getStudent().getFullName());

                // Map graded by name
                map().setGradedByName(source.getGradedBy() != null ? source.getGradedBy().getFullName() : null);

                // Map graded by ID
                map().setGradedById(source.getGradedBy() != null ? source.getGradedBy().getId() : null);

                // Map assignment ID
                map().setAssignmentId(source.getAssignment().getId());

                // Map student ID
                map().setStudentId(source.getStudent().getId());
            }
        });

        return modelMapper;
    }
}