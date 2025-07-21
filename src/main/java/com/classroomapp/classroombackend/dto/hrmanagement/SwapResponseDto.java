package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho phản hồi swap request (từ target employee hoặc manager)
 */
@Data
public class SwapResponseDto {

    @NotBlank(message = "Phản hồi không được để trống")
    private String response; // ACCEPT, REJECT, APPROVE, DECLINE

    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String reason;

    public String getResponse() {
        return response;
    }

    public String getManagerResponse() {
        return response;
    }
}
