package com.classroomapp.classroombackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseEnrollmentDto {
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String paymentReference;
    
    @NotNull(message = "Amount is required")
    private Double amount;
    
    private String notes;
    
    @NotNull(message = "Terms agreement is required")
    private Boolean agreeTerms = false;
    
    @NotNull(message = "Refund policy agreement is required")
    private Boolean agreeRefund = false;
    
    private String promoCode;
}