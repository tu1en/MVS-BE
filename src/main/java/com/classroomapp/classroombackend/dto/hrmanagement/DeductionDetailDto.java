package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductionDetailDto {
    private String type;
    private Integer count;
    private BigDecimal amount;
    private String description;
}