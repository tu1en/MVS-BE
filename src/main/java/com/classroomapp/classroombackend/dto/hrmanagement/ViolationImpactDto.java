package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationImpactDto {
    private Long staffId;
    private String staffName;
    private Integer violationCount;
    private BigDecimal totalDeduction;
    private String violationTypes;
}