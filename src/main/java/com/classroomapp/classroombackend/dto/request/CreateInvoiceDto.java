package com.classroomapp.classroombackend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateInvoiceDto {

    @NotBlank(message = "Số hóa đơn không được để trống")
    private String invoiceNumber;

    @NotNull(message = "ID học sinh không được để trống")
    private Long studentId;

    @NotNull(message = "Ngày phát hành không được để trống")
    private LocalDate issueDate;

    @NotNull(message = "Hạn thanh toán không được để trống")
    private LocalDate dueDate;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;

    @Valid
    @NotEmpty(message = "Phải có ít nhất một khoản phí")
    private List<InvoiceItemDto> items;

    @NotBlank(message = "Trạng thái hóa đơn không được để trống")
    @Pattern(regexp = "^(PENDING|PAID|PARTIAL|OVERDUE|CANCELLED)$", 
             message = "Trạng thái phải là PENDING, PAID, PARTIAL, OVERDUE hoặc CANCELLED")
    private String status;

    @Data
    public static class InvoiceItemDto {
        
        @NotBlank(message = "Mô tả không được để trống")
        @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
        private String description;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer quantity;

        @NotNull(message = "Đơn giá không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
        private BigDecimal unitPrice;
    }
}