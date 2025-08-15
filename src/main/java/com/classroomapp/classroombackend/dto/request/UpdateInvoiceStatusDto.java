package com.classroomapp.classroombackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateInvoiceStatusDto {

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "PENDING|PAID|PARTIAL|OVERDUE|CANCELLED", 
             message = "Trạng thái không hợp lệ")
    private String status;

    private BigDecimal paidAmount;

    private String paymentMethod;

    private String paymentReference;

    private String note;
}