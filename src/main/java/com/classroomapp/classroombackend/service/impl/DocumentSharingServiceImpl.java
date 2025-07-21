package com.classroomapp.classroombackend.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.model.SharedDocument;
import com.classroomapp.classroombackend.repository.SharedDocumentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.DocumentSharingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation của Document Sharing Service
 * Xử lý upload, download, delete và realtime sync
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentSharingServiceImpl implements DocumentSharingService {

    private final SharedDocumentRepository sharedDocumentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.file.upload-dir:uploads/documents}")
    private String uploadDir;

    // Allowed MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
        "application/vnd.ms-powerpoint", // .ppt
        "application/msword", // .doc
        "text/plain",
        "image/jpeg",
        "image/png",
        "image/gif"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public SharedDocument uploadDocument(Long slotId, MultipartFile file, String uploadedBy, 
                                       String documentType, boolean isPresentation) {
        
        log.info("Starting document upload for slot {}, type: {}, isPresentation: {}", 
                slotId, documentType, isPresentation);
        
        // Validate file
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "unknown";
        
        try {
            // Create unique filename
            String fileExtension = getFileExtension(originalFilename);
            String storedFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + "." + fileExtension;
            
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            
            // Save file to disk
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Create SharedDocument entity
            SharedDocument document = SharedDocument.builder()
                    .slotId(slotId)
                    .fileName(storedFileName)
                    .originalFileName(originalFilename)
                    .filePath(targetLocation.toString())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .uploadedBy(userRepository.findByUsername(uploadedBy).orElse(null))
                    .documentType(SharedDocument.DocumentType.valueOf(documentType.toUpperCase()))
                    .currentPage(isPresentation ? 1 : null)
                    .totalPages(isPresentation ? 1 : null) // Will be updated when parsed
                    .accessLevel(SharedDocument.AccessLevel.VIEW_AND_DOWNLOAD)
                    .documentStatus(SharedDocument.DocumentStatus.AVAILABLE)
                    .isPublic(true)
                    .build();
            
            // Save to database
            SharedDocument savedDocument = sharedDocumentRepository.save(document);
            
            // Send realtime notification via WebSocket
            sendDocumentNotification(slotId, "DOCUMENT_UPLOADED", savedDocument);
            
            log.info("Document uploaded successfully: {} (ID: {})", originalFilename, savedDocument.getId());
            return savedDocument;
            
        } catch (IOException e) {
            log.error("Error saving file to disk: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lưu file: " + e.getMessage());
        }
    }

    @Override
    public List<SharedDocument> getSlotDocuments(Long slotId) {
        return sharedDocumentRepository.findBySlotIdAndDocumentStatusOrderByUploadedAtDesc(
                slotId, SharedDocument.DocumentStatus.AVAILABLE);
    }

    @Override
    public Resource downloadDocument(Long documentId, String requestedBy) {
        SharedDocument document = getDocumentById(documentId);
        
        try {
            Path filePath = Paths.get(document.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                // Update download count
                document.incrementDownloadCount();
                sharedDocumentRepository.save(document);
                
                log.info("Document downloaded: {} by {}", document.getOriginalFileName(), requestedBy);
                return resource;
            } else {
                throw new RuntimeException("File không tồn tại: " + document.getOriginalFileName());
            }
        } catch (MalformedURLException e) {
            log.error("Error creating file resource: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo file resource: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocument(Long documentId, String deletedBy) {
        SharedDocument document = getDocumentById(documentId);
        
        // Soft delete by changing status
        document.setDocumentStatus(SharedDocument.DocumentStatus.DELETED);
        document.setUpdatedAt(LocalDateTime.now());
        sharedDocumentRepository.save(document);
        
        // Send realtime notification
        sendDocumentNotification(document.getSlotId(), "DOCUMENT_DELETED", document);
        
        // Delete physical file (optional - can be done by cleanup job)
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Physical file deleted: {}", document.getFilePath());
        } catch (IOException e) {
            log.warn("Could not delete physical file: {}", e.getMessage());
        }
        
        log.info("Document deleted: {} by {}", document.getOriginalFileName(), deletedBy);
    }

    @Override
    public SharedDocument getDocumentById(Long documentId) {
        return sharedDocumentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document không tồn tại: " + documentId));
    }

    @Override
    public void updatePresentationNavigation(Long documentId, int currentPage, String action, String controlledBy) {
        SharedDocument document = getDocumentById(documentId);
        
        if (!document.canBePresented()) {
            throw new IllegalArgumentException("Document này không thể present");
        }
        
        // Update current page and presentation control
        document.changePage(currentPage);
        if (!document.getIsCurrentlyPresenting()) {
            document.startPresentation(Long.valueOf(controlledBy));
        }
        
        sharedDocumentRepository.save(document);
        
        // Send realtime navigation update via WebSocket
        Map<String, Object> navigationData = Map.of(
            "action", action != null ? action : "NAVIGATE",
            "documentId", documentId,
            "currentPage", currentPage,
            "controlledBy", controlledBy,
            "timestamp", LocalDateTime.now().toString()
        );
        
        messagingTemplate.convertAndSend("/topic/document/" + document.getSlotId(), navigationData);
        
        log.info("Presentation navigation updated: document {}, page {}, action: {}", 
                documentId, currentPage, action);
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size không được vượt quá 10MB");
        }
        
        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("File type không được hỗ trợ: " + mimeType);
        }
        
        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new IllegalArgumentException("Filename không hợp lệ");
        }
        
        log.info("File validation passed: {} ({})", filename, mimeType);
    }

    /**
     * Send realtime notification via WebSocket
     */
    private void sendDocumentNotification(Long slotId, String action, SharedDocument document) {
        Map<String, Object> notification = Map.of(
            "action", action,
            "slotId", slotId,
            "documentId", document.getId(),
            "fileName", document.getOriginalFileName(),
            "uploadedBy", document.getUploadedBy(),
            "timestamp", LocalDateTime.now().toString()
        );
        
        messagingTemplate.convertAndSend("/topic/documents/" + slotId, notification);
        log.info("Document notification sent: {} for slot {}", action, slotId);
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}