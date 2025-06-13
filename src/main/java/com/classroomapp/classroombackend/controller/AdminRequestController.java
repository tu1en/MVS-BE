package com.classroomapp.classroombackend.controller;

<<<<<<< HEAD
import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.service.RequestService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

=======
>>>>>>> master
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.service.RequestService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple DTO for approval requests
 */
@Data
class ApproveRequestDTO {
    private String comment;
}

@RestController
@RequestMapping("/api/admin/requests")
@RequiredArgsConstructor
@Slf4j
public class AdminRequestController {
    private final RequestService requestService;

    /**
     * Lấy tất cả các yêu cầu đang chờ phê duyệt
     */
    @GetMapping("/pending")
    public ResponseEntity<List<RequestResponseDTO>> getPendingRequests() {
        log.info("Getting pending requests");
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    /**
     * Lấy tất cả các yêu cầu (bao gồm đã phê duyệt hoặc từ chối)
     */
    @GetMapping("/all")
    public ResponseEntity<List<RequestResponseDTO>> getAllRequests() {
        log.info("Getting all requests");
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    /**
     * Lấy chi tiết của một yêu cầu theo ID
     */
    @GetMapping("/{requestId}/details")
    public ResponseEntity<RequestResponseDTO> getRequestDetails(@PathVariable Long requestId) {
        log.info("Getting details for request ID: {}", requestId);
        return ResponseEntity.ok(requestService.getRequestDetails(requestId));
    }

    /**
     * Phê duyệt một yêu cầu
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long requestId, 
            @RequestBody(required = false) ApproveRequestDTO requestDTO) {
        log.info("BEGIN APPROVAL PROCESS: Request ID: {}, DTO: {}", requestId, requestDTO);
        
        try {
            log.info("Calling approveRequest service method for request ID: {}", requestId);
            RequestResponseDTO result = requestService.approveRequest(requestId);
            log.info("Successfully approved request ID: {}, result: {}", requestId, result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error approving request ID: {}, error type: {}, message: {}", 
                requestId, e.getClass().getName(), e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "Failed to approve request",
                    "message", e.getMessage(),
                    "requestId", requestId,
                    "errorType", e.getClass().getName()
                ));
        }
    }

    /**
     * Từ chối một yêu cầu
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> payload) {
        log.info("Rejecting request ID: {}", requestId);
        
        String reason = payload.get("reason");
        log.info("Rejection reason: {}", reason);
        
        try {
            RequestResponseDTO result = requestService.rejectRequest(requestId, reason);
            log.info("Successfully rejected request ID: {}", requestId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error rejecting request ID: {}", requestId, e);
            return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "Failed to reject request",
                    "message", e.getMessage(),
                    "requestId", requestId,
                    "errorType", e.getClass().getName()
                ));
        }
    }
    
    /**
     * Phê duyệt đơn giản - vẫn tạo user trong database
     */
    @PostMapping("/{requestId}/approve-simple")
    public ResponseEntity<?> approveRequestSimple(@PathVariable Long requestId) {
        log.info("Simple approval endpoint called for request ID: {}", requestId);
        try {
            // Gọi phương thức approveRequest đầy đủ để đảm bảo user được tạo trong database
            RequestResponseDTO result = requestService.approveRequest(requestId);
            log.info("Successfully completed approval for request ID: {}", requestId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in approval for request ID: {}", requestId, e);
            return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "Failed in approval",
                    "message", e.getMessage(),
                    "requestId", requestId
                ));
        }
    }

    /**
     * Diagnostic endpoint to test API connectivity and authentication
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, String>> testConnection() {
        log.info("Test connection endpoint called");
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "API connection successful",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
} 