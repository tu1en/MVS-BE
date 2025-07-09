package com.classroomapp.classroombackend.dto.exammangement;

import java.time.Instant;

import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;

import lombok.Data;

@Data
public class ExamSubmissionDto {
    private Long id;
    private Long examId;
    private UserDetailsDto student;
    private Instant startedAt;
    private Instant submittedAt;
    private String content;
    private Integer score;
    private String feedback;
    private Instant gradedAt;
    private UserDetailsDto gradedBy;
} 