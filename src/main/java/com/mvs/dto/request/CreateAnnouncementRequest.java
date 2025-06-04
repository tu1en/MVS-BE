package com.mvs.dto.request;

import com.mvs.entity.PriorityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateAnnouncementRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Priority level is required")
    private PriorityLevel priority;

    @NotEmpty(message = "At least one recipient is required")
    private Set<Long> recipientIds;
}
