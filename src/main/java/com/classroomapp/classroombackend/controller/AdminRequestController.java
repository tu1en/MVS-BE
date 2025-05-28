package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/requests")
@RequiredArgsConstructor
public class AdminRequestController {
    private final RequestService requestService;

    /**
     * Lấy tất cả các yêu cầu đang chờ phê duyệt
     */
    @GetMapping("/pending")
    public ResponseEntity<List<RequestResponseDTO>> getPendingRequests() {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    /**
     * Lấy chi tiết của một yêu cầu theo ID
     */
    @GetMapping("/{requestId}/details")
    public ResponseEntity<RequestResponseDTO> getRequestDetails(@PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.getRequestDetails(requestId));
    }

    /**
     * Phê duyệt một yêu cầu
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<RequestResponseDTO> approveRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.approveRequest(requestId));
    }

    /**
     * Từ chối một yêu cầu
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<RequestResponseDTO> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> payload) {
        String reason = payload.get("reason");
        return ResponseEntity.ok(requestService.rejectRequest(requestId, reason));
    }
} 