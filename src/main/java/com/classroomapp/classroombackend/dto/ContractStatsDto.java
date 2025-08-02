package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractStatsDto {
    private Long totalContracts;
    private Long teacherContracts;
    private Long staffContracts;
    private Long activeContracts;
    private Long expiredContracts;
}
