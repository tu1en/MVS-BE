package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.SharedDocument;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface cho Document Sharing trong live session
 */
public interface DocumentSharingService {
    
    /**
     * Upload tÃ i liá»‡u cho slot há»c
     */
    SharedDocument uploadDocument(Long slotId, MultipartFile file, String uploadedBy, 
                                String documentType, boolean isPresentation);
    
    /**
     * Láº¥y danh sÃ¡ch tÃ i liá»‡u cá»§a slot
     */
    List<SharedDocument> getSlotDocuments(Long slotId);
    
    /**
     * Download tÃ i liá»‡u
     */
    Resource downloadDocument(Long documentId, String requestedBy);
    
    /**
     * XÃ³a tÃ i liá»‡u
     */
    void deleteDocument(Long documentId, String deletedBy);
    
    /**
     * Láº¥y thÃ´ng tin tÃ i liá»‡u by ID
     */
    SharedDocument getDocumentById(Long documentId);
    
    /**
     * Update presentation navigation
     */
    void updatePresentationNavigation(Long documentId, int currentPage, String action, String controlledBy);
    
    /**
     * Validate file upload
     */
    void validateFile(MultipartFile file);
}
