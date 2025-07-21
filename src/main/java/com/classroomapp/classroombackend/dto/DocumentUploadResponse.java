package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho document upload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private boolean success;
    private String message;
    private SharedDocumentDTO document;
    
    public static DocumentUploadResponse success(String message, SharedDocumentDTO document) {
        return new DocumentUploadResponse(true, message, document);
    }
    
    public static DocumentUploadResponse error(String message) {
        return new DocumentUploadResponse(false, message, null);
    }
}