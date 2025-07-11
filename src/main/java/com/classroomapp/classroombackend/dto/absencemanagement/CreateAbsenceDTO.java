package com.classroomapp.classroombackend.dto.absencemanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Data
public class CreateAbsenceDTO {
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @NotNull(message = "Number of days is required")
    @Positive(message = "Number of days must be positive")
    private Integer numberOfDays;
    
    @NotBlank(message = "Description is required")
    private String description;
} 