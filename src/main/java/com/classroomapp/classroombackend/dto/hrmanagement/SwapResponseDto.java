package com.classroomapp.classroombackend.dto.hrmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho pháº£n há»“i swap request (tá»« target employee hoáº·c manager)
 */
@Data
public class SwapResponseDto {

    @NotBlank(message = "Pháº£n há»“i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String response; // ACCEPT, REJECT, APPROVE, DECLINE

    @Size(max = 500, message = "LÃ½ do khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String reason;

    public String getResponse() {
        return response;
    }

    public String getManagerResponse() {
        return response;
    }
}
