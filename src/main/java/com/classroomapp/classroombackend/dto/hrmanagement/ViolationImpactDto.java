package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationImpactDto {
    private Long userId;
    private String userName;
    private Integer violationCount;
    private BigDecimal totalDeduction;
    private String violationTypes;
}