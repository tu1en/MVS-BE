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
     * Upload tài liệu cho slot học
     */
    SharedDocument uploadDocument(Long slotId, MultipartFile file, String uploadedBy, 
                                String documentType, boolean isPresentation);
    
    /**
     * Lấy danh sách tài liệu của slot
     */
    List<SharedDocument> getSlotDocuments(Long slotId);
    
    /**
     * Download tài liệu
     */
    Resource downloadDocument(Long documentId, String requestedBy);
    
    /**
     * Xóa tài liệu
     */
    void deleteDocument(Long documentId, String deletedBy);
    
    /**
     * Lấy thông tin tài liệu by ID
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