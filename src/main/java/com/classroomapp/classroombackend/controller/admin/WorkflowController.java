// package com.classroomapp.classroombackend.controller.admin;

// import java.util.List;

// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.classroomapp.classroombackend.dto.ApiResponse;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// /**
//  * Controller xá»­ lÃ½ Workflow cho Admin
//  * YÃªu cáº§u quyá»n TEACHER Ä‘á»ƒ truy cáº­p
//  */
// @RestController
// @RequestMapping("/api/admin/workflows")
// @RequiredArgsConstructor
// @Slf4j
// @CrossOrigin(origins = "*")
// @PreAuthorize("hasRole('ADMIN')")
// public class WorkflowController {
    
//     private final WorkflowService workflowService;
    
//     /**
//      * Láº¥y táº¥t cáº£ workflows
//      */
//     @GetMapping
//     public ResponseEntity<ApiResponse<List<WorkflowResponse>>> getAllWorkflows() {
//         log.info("Getting all workflows");
        
//         try {
//             List<WorkflowResponse> workflows = workflowService.getAllWorkflows();
            
//             return ResponseEntity.ok(
//                 ApiResponse.<List<WorkflowResponse>>builder()
//                     .success(true)
//                     .message("Láº¥y danh sÃ¡ch workflow thÃ nh cÃ´ng")
//                     .data(workflows)
//                     .build()
//             );
            
//         } catch (Exception e) {
//             log.error("Error getting all workflows", e);
            
//             return ResponseEntity.internalServerError().body(
//                 ApiResponse.<List<WorkflowResponse>>builder()
//                     .success(false)
//                     .message("Lá»—i khi láº¥y danh sÃ¡ch workflow: " + e.getMessage())
//                     .build()
//             );
//         }
//     }
    
//     /**
//      * Láº¥y workflow theo ID
//      */
//     @GetMapping("/{id}")
//     public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflowById(@PathVariable Long id) {
//         log.info("Getting workflow with id: {}", id);
        
//         try {
//             return workflowService.getWorkflowById(id)
//                     .map(workflow -> ResponseEntity.ok(
//                         ApiResponse.<WorkflowResponse>builder()
//                             .success(true)
//                             .message("Láº¥y workflow thÃ nh cÃ´ng")
//                             .data(workflow)
//                             .build()
//                     ))
//                     .orElse(ResponseEntity.notFound().build());
                    
//         } catch (Exception e) {
//             log.error("Error getting workflow by id", e);
            
//             return ResponseEntity.internalServerError().body(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(false)
//                     .message("Lá»—i khi láº¥y workflow: " + e.getMessage())
//                     .build()
//             );
//         }
//     }
    
//     /**
//      * Táº¡o workflow má»›i
//      */
//     @PostMapping
//     public ResponseEntity<ApiResponse<WorkflowResponse>> createWorkflow(
//             @Valid @RequestBody WorkflowRequest request) {
//         log.info("Creating new workflow: {}", request.getName());
        
//         try {
//             WorkflowResponse workflow = workflowService.createWorkflow(request);
            
//             return ResponseEntity.ok(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(true)
//                     .message("Táº¡o workflow thÃ nh cÃ´ng")
//                     .data(workflow)
//                     .build()
//             );
            
//         } catch (IllegalArgumentException e) {
//             log.error("Validation error creating workflow", e);
            
//             return ResponseEntity.badRequest().body(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(false)
//                     .message(e.getMessage())
//                     .build()
//             );
            
//         } catch (Exception e) {
//             log.error("Error creating workflow", e);
            
//             return ResponseEntity.internalServerError().body(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(false)
//                     .message("Lá»—i khi táº¡o workflow: " + e.getMessage())
//                     .build()
//             );
//         }
//     }
    
//     /**
//      * Cáº­p nháº­t workflow
//      */
//     @PutMapping("/{id}")
//     public ResponseEntity<ApiResponse<WorkflowResponse>> updateWorkflow(
//             @PathVariable Long id,
//             @Valid @RequestBody WorkflowRequest request) {
//         log.info("Updating workflow with id: {}", id);
        
//         try {
//             WorkflowResponse workflow = workflowService.updateWorkflow(id, request);
            
//             return ResponseEntity.ok(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(true)
//                     .message("Cáº­p nháº­t workflow thÃ nh cÃ´ng")
//                     .data(workflow)
//                     .build()
//             );
            
//         } catch (IllegalArgumentException e) {
//             log.error("Validation error updating workflow", e);
            
//             return ResponseEntity.badRequest().body(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(false)
//                     .message(e.getMessage())
//                     .build()
//             );
            
//         } catch (Exception e) {
//             log.error("Error updating workflow", e);
            
//             return ResponseEntity.internalServerError().body(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(false)
//                     .message("Lá»—i khi cáº­p nháº­t workflow: " + e.getMessage())
//                     .build()
//             );
//         }
//     }
    
//     /**
//      * XÃ³a workflow
//      */
//     @DeleteMapping("/{id}")
//     public ResponseEntity<ApiResponse<String>> deleteWorkflow(@PathVariable Long id) {
//         log.info("Deleting workflow with id: {}", id);
        
//         try {
//             boolean deleted = workflowService.deleteWorkflow(id);
            
//             if (deleted) {
//                 return ResponseEntity.ok(
//                     ApiResponse.<String>builder()
//                         .success(true)
//                         .message("XÃ³a workflow thÃ nh cÃ´ng")
//                         .data("Workflow deleted successfully")
//                         .build()
//                 );
//             } else {
//                 return ResponseEntity.notFound().build();
//             }
            
//         } catch (Exception e) {
//             log.error("Error deleting workflow", e);
            
//             return ResponseEntity.internalServerError().body(
//                 ApiResponse.<String>builder()
//                     .success(false)
//                     .message("Lá»—i khi xÃ³a workflow: " + e.getMessage())
//                     .build()
//             );
//         }
//     }
    
//     /**
//      * Láº¥y workflows active
//      */
//     @GetMapping("/active")
//     public ResponseEntity<ApiResponse<List<WorkflowResponse>>> getActiveWorkflows() {
//         log.info("Getting active workflows");
        
//         try {
//             List<WorkflowResponse> workflows = workflowService.getActiveWorkflows();
            
//             return ResponseEntity.ok(
//                 ApiResponse.<List<WorkflowResponse>>builder()
//                     .success(true)
//                     .message("Láº¥y workflow active thÃ nh cÃ´ng")
//                     .data(workflows)
//                     .build()
//             );
            
//         } catch (Exception e) {
//             log.error("Error getting active workflows", e);
            
//             return ResponseEntity.internalServerError().body(
//                 ApiResponse.<List<WorkflowResponse>>builder()
//                     .success(false)
//                     .message("Lá»—i khi láº¥y workflow active: " + e.getMessage())
//                     .build()
//             );
//         }
//     }
    
//     /**
//      * Duplicate workflow
//      */
//     @PostMapping("/{id}/duplicate")
//     public ResponseEntity<ApiResponse<WorkflowResponse>> duplicateWorkflow(
//             @PathVariable Long id,
//             @RequestParam String newName) {
//         log.info("Duplicating workflow with id: {} to new name: {}", id, newName);
        
//         try {
//             WorkflowResponse workflow = workflowService.duplicateWorkflow(id, newName);
            
//             return ResponseEntity.ok(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(true)
//                     .message("Duplicate workflow thÃ nh cÃ´ng")
//                     .data(workflow)
//                     .build()
//             );
            
//         } catch (IllegalArgumentException e) {
//             log.error("Validation error duplicating workflow", e);
            
//             return ResponseEntity.badRequest().body(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(false)
//                     .message(e.getMessage())
//                     .build()
//             );
            
//         } catch (Exception e) {
//             log.error("Error duplicating workflow", e);
            
//             return ResponseEntity.internalServerError().body(
//                 ApiResponse.<WorkflowResponse>builder()
//                     .success(false)
//                     .message("Lá»—i khi duplicate workflow: " + e.getMessage())
//                     .build()
//             );
//         }
//     }
    
//     /**
//      * Export workflow as JSON
//      */
//     @GetMapping("/{id}/export")
//     public ResponseEntity<String> exportWorkflow(@PathVariable Long id) {
//         log.info("Exporting workflow with id: {}", id);
        
//         try {
//             String jsonData = workflowService.exportWorkflowAsJSON(id);
            
//             return ResponseEntity.ok()
//                     .header("Content-Disposition", "attachment; filename=workflow_" + id + ".json")
//                     .header("Content-Type", "application/json")
//                     .body(jsonData);
                    
//         } catch (IllegalArgumentException e) {
//             log.error("Workflow not found for export", e);
//             return ResponseEntity.notFound().build();
            
//         } catch (Exception e) {
//             log.error("Error exporting workflow", e);
//             return ResponseEntity.internalServerError().build();
//         }
//     }
// }
