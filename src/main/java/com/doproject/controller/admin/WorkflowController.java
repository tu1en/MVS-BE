package com.doproject.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doproject.common.ApiResponse;
import com.doproject.dto.request.WorkflowRequest;
import com.doproject.dto.response.WorkflowResponse;
import com.doproject.service.WorkflowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller xử lý Workflow cho Admin
 * Yêu cầu quyền TEACHER để truy cập
 */
@RestController
@RequestMapping("/api/admin/workflows")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class WorkflowController {
    
    private final WorkflowService workflowService;
    
    /**
     * Lấy tất cả workflows
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowResponse>>> getAllWorkflows() {
        log.info("Getting all workflows");
        
        try {
            List<WorkflowResponse> workflows = workflowService.getAllWorkflows();
            
            return ResponseEntity.ok(
                ApiResponse.<List<WorkflowResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách workflow thành công")
                    .data(workflows)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error getting all workflows", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<List<WorkflowResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách workflow: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Lấy workflow theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflowById(@PathVariable Long id) {
        log.info("Getting workflow with id: {}", id);
        
        try {
            return workflowService.getWorkflowById(id)
                    .map(workflow -> ResponseEntity.ok(
                        ApiResponse.<WorkflowResponse>builder()
                            .success(true)
                            .message("Lấy workflow thành công")
                            .data(workflow)
                            .build()
                    ))
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (Exception e) {
            log.error("Error getting workflow by id", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<WorkflowResponse>builder()
                    .success(false)
                    .message("Lỗi khi lấy workflow: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Tạo workflow mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowResponse>> createWorkflow(
            @Valid @RequestBody WorkflowRequest request) {
        log.info("Creating new workflow: {}", request.getName());
        
        try {
            WorkflowResponse workflow = workflowService.createWorkflow(request);
            
            return ResponseEntity.ok(
                ApiResponse.<WorkflowResponse>builder()
                    .success(true)
                    .message("Tạo workflow thành công")
                    .data(workflow)
                    .build()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating workflow", e);
            
            return ResponseEntity.badRequest().body(
                ApiResponse.<WorkflowResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error creating workflow", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<WorkflowResponse>builder()
                    .success(false)
                    .message("Lỗi khi tạo workflow: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Cập nhật workflow
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowRequest request) {
        log.info("Updating workflow with id: {}", id);
        
        try {
            WorkflowResponse workflow = workflowService.updateWorkflow(id, request);
            
            return ResponseEntity.ok(
                ApiResponse.<WorkflowResponse>builder()
                    .success(true)
                    .message("Cập nhật workflow thành công")
                    .data(workflow)
                    .build()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating workflow", e);
            
            return ResponseEntity.badRequest().body(
                ApiResponse.<WorkflowResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error updating workflow", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<WorkflowResponse>builder()
                    .success(false)
                    .message("Lỗi khi cập nhật workflow: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Xóa workflow
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWorkflow(@PathVariable Long id) {
        log.info("Deleting workflow with id: {}", id);
        
        try {
            boolean deleted = workflowService.deleteWorkflow(id);
            
            if (deleted) {
                return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                        .success(true)
                        .message("Xóa workflow thành công")
                        .data("Workflow deleted successfully")
                        .build()
                );
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting workflow", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lỗi khi xóa workflow: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Lấy workflows active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<WorkflowResponse>>> getActiveWorkflows() {
        log.info("Getting active workflows");
        
        try {
            List<WorkflowResponse> workflows = workflowService.getActiveWorkflows();
            
            return ResponseEntity.ok(
                ApiResponse.<List<WorkflowResponse>>builder()
                    .success(true)
                    .message("Lấy workflow active thành công")
                    .data(workflows)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error getting active workflows", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<List<WorkflowResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy workflow active: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Duplicate workflow
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ApiResponse<WorkflowResponse>> duplicateWorkflow(
            @PathVariable Long id,
            @RequestParam String newName) {
        log.info("Duplicating workflow with id: {} to new name: {}", id, newName);
        
        try {
            WorkflowResponse workflow = workflowService.duplicateWorkflow(id, newName);
            
            return ResponseEntity.ok(
                ApiResponse.<WorkflowResponse>builder()
                    .success(true)
                    .message("Duplicate workflow thành công")
                    .data(workflow)
                    .build()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error duplicating workflow", e);
            
            return ResponseEntity.badRequest().body(
                ApiResponse.<WorkflowResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error duplicating workflow", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<WorkflowResponse>builder()
                    .success(false)
                    .message("Lỗi khi duplicate workflow: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Export workflow as JSON
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportWorkflow(@PathVariable Long id) {
        log.info("Exporting workflow with id: {}", id);
        
        try {
            String jsonData = workflowService.exportWorkflowAsJSON(id);
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=workflow_" + id + ".json")
                    .header("Content-Type", "application/json")
                    .body(jsonData);
                    
        } catch (IllegalArgumentException e) {
            log.error("Workflow not found for export", e);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error exporting workflow", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
