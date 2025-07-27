package com.classroomapp.classroombackend.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.DocumentUploadResponse;
import com.classroomapp.classroombackend.dto.SharedDocumentDTO;
import com.classroomapp.classroombackend.model.SharedDocument;
import com.classroomapp.classroombackend.service.DocumentSharingService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller xá»­ lÃ½ Document Sharing cho live session
 * Há»— trá»£ upload, download, delete tÃ i liá»‡u trong slot há»c
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://yourdomain.com"})
public class DocumentSharingController {

    private final DocumentSharingService documentSharingService;

    /**
     * Upload tÃ i liá»‡u cho slot há»c (Teacher/Manager only)
     */
    @PostMapping("/slots/{slotId}/upload")
    @PreAuthorize("hasRole('TEACHER') or hasRole('MANAGER')")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @PathVariable Long slotId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", defaultValue = "PRESENTATION") String documentType,
            @RequestParam(value = "isPresentation", defaultValue = "false") boolean isPresentation,
            Principal principal) {
        
        try {
            log.info("Uploading document for slot {} by user {}", slotId, principal.getName());
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new DocumentUploadResponse(false, "File khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng", null));
            }

            SharedDocument document = documentSharingService.uploadDocument(slotId, file, principal.getName(), documentType, isPresentation);
            
            DocumentUploadResponse response = new DocumentUploadResponse(
                true, 
                "Upload thÃ nh cÃ´ng", 
                SharedDocumentDTO.fromEntity(document)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file upload attempt: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new DocumentUploadResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error uploading document for slot {}: {}", slotId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DocumentUploadResponse(false, "Lá»—i server khi upload file", null));
        }
    }

    /**
     * Láº¥y danh sÃ¡ch tÃ i liá»‡u cá»§a slot
     */
    @GetMapping("/slots/{slotId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('MANAGER')")
    public ResponseEntity<List<SharedDocumentDTO>> getSlotDocuments(@PathVariable Long slotId) {
        try {
            List<SharedDocument> documents = documentSharingService.getSlotDocuments(slotId);
            List<SharedDocumentDTO> documentDTOs = documents.stream()
                .map(SharedDocumentDTO::fromEntity)
                .toList();
            
            return ResponseEntity.ok(documentDTOs);
        } catch (Exception e) {
            log.error("Error getting documents for slot {}: {}", slotId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download tÃ i liá»‡u
     */
    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('MANAGER')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            HttpServletRequest request,
            Principal principal) {
        
        try {
            Resource resource = documentSharingService.downloadDocument(documentId, principal.getName());
            SharedDocument document = documentSharingService.getDocumentById(documentId);
            
            // Determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            // Fallback to the default content type if type could not be determined
            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid download attempt for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error downloading document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * XÃ³a tÃ i liá»‡u (Teacher/Manager only)
     */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('MANAGER')")
    public ResponseEntity<String> deleteDocument(
            @PathVariable Long documentId,
            Principal principal) {
        
        try {
            documentSharingService.deleteDocument(documentId, principal.getName());
            return ResponseEntity.ok("XÃ³a tÃ i liá»‡u thÃ nh cÃ´ng");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid delete attempt for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lá»—i server khi xÃ³a tÃ i liá»‡u");
        }
    }

    /**
     * Update presentation controls (Teacher only)
     */
    @PostMapping("/{documentId}/presentation/navigate")
    @PreAuthorize("hasRole('TEACHER') or hasRole('MANAGER')")
    public ResponseEntity<String> navigatePresentation(
            @PathVariable Long documentId,
            @RequestParam int currentPage,
            @RequestParam(required = false) String action,
            Principal principal) {
        
        try {
            documentSharingService.updatePresentationNavigation(documentId, currentPage, action, principal.getName());
            return ResponseEntity.ok("Navigation updated successfully");
        } catch (Exception e) {
            log.error("Error updating presentation navigation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating presentation");
        }
    }

    /**
     * Get current presentation state
     */
    @GetMapping("/{documentId}/presentation/state")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('MANAGER')")
    public ResponseEntity<SharedDocumentDTO> getPresentationState(@PathVariable Long documentId) {
        try {
            SharedDocument document = documentSharingService.getDocumentById(documentId);
            return ResponseEntity.ok(SharedDocumentDTO.fromEntity(document));
        } catch (Exception e) {
            log.error("Error getting presentation state: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
