package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.LiveSessionPermission;
import com.classroomapp.classroombackend.model.LiveSessionPermission.PermissionType;
import com.classroomapp.classroombackend.service.LiveSessionPermissionService;

/**
 * Controller để quản lý permissions cho live sessions
 */
@RestController
@RequestMapping("/api/live-session-permissions")
public class LiveSessionPermissionController {
    
    @Autowired
    private LiveSessionPermissionService permissionService;
    
    /**
     * Grant permission cho student (chỉ teacher)
     */
    @PostMapping("/grant")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LiveSessionPermission> grantPermission(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long studentId = Long.valueOf(request.get("studentId").toString());
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            PermissionType permissionType = PermissionType.valueOf(request.get("permissionType").toString());
            String reason = request.getOrDefault("reason", "").toString();
            
            LocalDateTime expiresAt = null;
            if (request.containsKey("expiresAt") && request.get("expiresAt") != null) {
                expiresAt = LocalDateTime.parse(request.get("expiresAt").toString());
            }
            
            LiveSessionPermission permission = permissionService.grantPermission(
                    liveStreamId, studentId, teacherId, permissionType, expiresAt, reason);
            
            return ResponseEntity.ok(permission);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Deny permission cho student (chỉ teacher)
     */
    @PostMapping("/deny")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LiveSessionPermission> denyPermission(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long studentId = Long.valueOf(request.get("studentId").toString());
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            PermissionType permissionType = PermissionType.valueOf(request.get("permissionType").toString());
            String reason = request.getOrDefault("reason", "").toString();
            
            LiveSessionPermission permission = permissionService.denyPermission(
                    liveStreamId, studentId, teacherId, permissionType, reason);
            
            return ResponseEntity.ok(permission);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Revoke permission từ student (chỉ teacher)
     */
    @PostMapping("/revoke")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LiveSessionPermission> revokePermission(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long studentId = Long.valueOf(request.get("studentId").toString());
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            PermissionType permissionType = PermissionType.valueOf(request.get("permissionType").toString());
            String reason = request.getOrDefault("reason", "").toString();
            
            LiveSessionPermission permission = permissionService.revokePermission(
                    liveStreamId, studentId, teacherId, permissionType, reason);
            
            return ResponseEntity.ok(permission);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check permission của student
     */
    @GetMapping("/check/{liveStreamId}/{studentId}/{permissionType}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @PathVariable Long liveStreamId,
            @PathVariable Long studentId,
            @PathVariable String permissionType) {
        try {
            PermissionType type = PermissionType.valueOf(permissionType);
            boolean hasPermission = permissionService.hasPermission(liveStreamId, studentId, type);
            
            return ResponseEntity.ok(Map.of(
                    "hasPermission", hasPermission,
                    "liveStreamId", liveStreamId,
                    "studentId", studentId,
                    "permissionType", permissionType
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy tất cả permissions của student
     */
    @GetMapping("/student/{liveStreamId}/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<List<LiveSessionPermission>> getStudentPermissions(
            @PathVariable Long liveStreamId,
            @PathVariable Long studentId) {
        try {
            List<LiveSessionPermission> permissions = permissionService.getStudentPermissions(liveStreamId, studentId);
            return ResponseEntity.ok(permissions);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy tất cả student permissions trong live session (chỉ teacher)
     */
    @GetMapping("/all/{liveStreamId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<Map<String, Object>>> getAllStudentPermissions(@PathVariable Long liveStreamId) {
        try {
            List<Map<String, Object>> permissions = permissionService.getAllStudentPermissions(liveStreamId);
            return ResponseEntity.ok(permissions);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Set default permissions cho student khi join
     */
    @PostMapping("/set-default/{liveStreamId}/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<LiveSessionPermission>> setDefaultPermissions(
            @PathVariable Long liveStreamId,
            @PathVariable Long studentId) {
        try {
            List<LiveSessionPermission> permissions = permissionService.setDefaultPermissions(liveStreamId, studentId);
            return ResponseEntity.ok(permissions);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Grant permission cho tất cả students (chỉ teacher)
     */
    @PostMapping("/grant-all")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> grantPermissionToAll(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            PermissionType permissionType = PermissionType.valueOf(request.get("permissionType").toString());
            String reason = request.getOrDefault("reason", "").toString();
            
            LocalDateTime expiresAt = null;
            if (request.containsKey("expiresAt") && request.get("expiresAt") != null) {
                expiresAt = LocalDateTime.parse(request.get("expiresAt").toString());
            }
            
            int grantedCount = permissionService.grantPermissionToAll(
                    liveStreamId, teacherId, permissionType, expiresAt, reason);
            
            return ResponseEntity.ok(Map.of(
                    "grantedCount", grantedCount,
                    "permissionType", permissionType.toString(),
                    "message", "Permission granted to " + grantedCount + " students"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Revoke permission từ tất cả students (chỉ teacher)
     */
    @PostMapping("/revoke-all")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> revokePermissionFromAll(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            PermissionType permissionType = PermissionType.valueOf(request.get("permissionType").toString());
            String reason = request.getOrDefault("reason", "").toString();
            
            int revokedCount = permissionService.revokePermissionFromAll(
                    liveStreamId, teacherId, permissionType, reason);
            
            return ResponseEntity.ok(Map.of(
                    "revokedCount", revokedCount,
                    "permissionType", permissionType.toString(),
                    "message", "Permission revoked from " + revokedCount + " students"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Student request permission
     */
    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LiveSessionPermission> requestPermission(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long studentId = Long.valueOf(request.get("studentId").toString());
            PermissionType permissionType = PermissionType.valueOf(request.get("permissionType").toString());
            String reason = request.getOrDefault("reason", "").toString();
            
            LiveSessionPermission permission = permissionService.requestPermission(
                    liveStreamId, studentId, permissionType, reason);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(permission);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy pending permission requests (chỉ teacher)
     */
    @GetMapping("/pending/{liveStreamId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<LiveSessionPermission>> getPendingRequests(@PathVariable Long liveStreamId) {
        try {
            List<LiveSessionPermission> requests = permissionService.getPendingPermissionRequests(liveStreamId);
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Approve hoặc deny permission request (chỉ teacher)
     */
    @PostMapping("/handle-request")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LiveSessionPermission> handlePermissionRequest(@RequestBody Map<String, Object> request) {
        try {
            Long permissionId = Long.valueOf(request.get("permissionId").toString());
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            Boolean approve = Boolean.valueOf(request.get("approve").toString());
            String reason = request.getOrDefault("reason", "").toString();
            
            LocalDateTime expiresAt = null;
            if (request.containsKey("expiresAt") && request.get("expiresAt") != null) {
                expiresAt = LocalDateTime.parse(request.get("expiresAt").toString());
            }
            
            LiveSessionPermission permission = permissionService.handlePermissionRequest(
                    permissionId, teacherId, approve, reason, expiresAt);
            
            return ResponseEntity.ok(permission);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy permission statistics (chỉ teacher)
     */
    @GetMapping("/statistics/{liveStreamId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getPermissionStatistics(@PathVariable Long liveStreamId) {
        try {
            Map<String, Object> statistics = permissionService.getPermissionStatistics(liveStreamId);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Cleanup expired permissions (admin task)
     */
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> cleanupExpiredPermissions() {
        try {
            int cleanedCount = permissionService.cleanupExpiredPermissions();
            return ResponseEntity.ok(Map.of(
                    "cleanedCount", cleanedCount,
                    "message", "Cleaned up " + cleanedCount + " expired permissions"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}