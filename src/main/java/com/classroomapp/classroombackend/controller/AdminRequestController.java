package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.requestmanagement.RequestDTO;
import com.classroomapp.classroombackend.service.AdminRequestService;

@RestController
@RequestMapping("/api/admin/requests")
@PreAuthorize("hasRole('MANAGER')")
public class AdminRequestController {

    private final AdminRequestService adminRequestService;

    public AdminRequestController(AdminRequestService adminRequestService) {
        this.adminRequestService = adminRequestService;
    }

    @GetMapping
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(adminRequestService.getAllRequests());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RequestDTO>> getPendingRequests() {
        return ResponseEntity.ok(adminRequestService.getPendingRequests());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestDTO> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(adminRequestService.approveRequest(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestDTO> rejectRequest(@PathVariable Long id, @RequestBody String reason) {
        return ResponseEntity.ok(adminRequestService.rejectRequest(id, reason));
    }
} 