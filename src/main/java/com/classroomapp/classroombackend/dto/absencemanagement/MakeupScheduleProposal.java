package com.classroomapp.classroombackend.dto.absencemanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor  
@AllArgsConstructor
public class MakeupScheduleProposal {
    @NotNull(message = "Makeup date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate makeupDate;
    
    @NotNull(message = "Start time is required") 
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm") 
    private LocalTime endTime;
    
    @NotBlank(message = "Reason is required")
    private String reason; // Lý do học bù (VD: "Học bù buổi ngày 10/01/2024")
    
    private String location; // Địa điểm học bù (tùy chọn)
}