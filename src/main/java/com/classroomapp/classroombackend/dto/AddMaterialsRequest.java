package com.classroomapp.classroombackend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddMaterialsRequest {
    @NotEmpty
    private List<FileUploadResponse> files;
} 