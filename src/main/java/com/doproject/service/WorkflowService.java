package com.doproject.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doproject.dto.request.WorkflowRequest;
import com.doproject.dto.response.WorkflowResponse;
import com.doproject.entity.Workflow;
import com.doproject.repository.WorkflowRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service xử lý logic cho Workflow
 * ✅ ENHANCED: Added getWorkflowCount method and improved error handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {
    
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * ✅ NEW: Get total workflow count for health check
     */
    public long getWorkflowCount() {
        try {
            long count = workflowRepository.count();
            log.info("Total workflow count: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Error counting workflows", e);
            return 0;
        }
    }
    
    /**
     * ✅ ENHANCED: Lấy tất cả workflows với better error handling
     */
    public List<WorkflowResponse> getAllWorkflows() {
        log.info("=== WorkflowService.getAllWorkflows() - Starting ===");
        
        try {
            List<Workflow> workflows = workflowRepository.findAll();
            log.info("Found {} workflows in database", workflows.size());
            
            List<WorkflowResponse> responses = workflows.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            log.info("Successfully converted {} workflows to responses", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("❌ Error in getAllWorkflows()", e);
            throw new RuntimeException("Failed to retrieve workflows: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy workflow theo ID
     */
    public Optional<WorkflowResponse> getWorkflowById(Long id) {
        log.info("Fetching workflow with id: {}", id);
        
        try {
            return workflowRepository.findById(id)
                    .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error getting workflow by id: {}", id, e);
            throw new RuntimeException("Failed to get workflow by id: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tạo workflow mới
     */
    @Transactional
    public WorkflowResponse createWorkflow(WorkflowRequest request) {
        log.info("Creating new workflow: {}", request.getName());
        
        try {
            // Validate JSON data
            if (!isValidJSON(request.getJsonData())) {
                throw new IllegalArgumentException("Invalid JSON data format");
            }
            
            // Check if name already exists
            if (workflowRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Workflow with name '" + request.getName() + "' already exists");
            }
            
            Workflow workflow = Workflow.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .jsonData(request.getJsonData())
                    .version(1)
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .createdBy(request.getCreatedBy())
                    .build();
            
            Workflow savedWorkflow = workflowRepository.save(workflow);
            
            log.info("Created workflow with id: {}", savedWorkflow.getId());
            return convertToResponse(savedWorkflow);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating workflow: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating workflow", e);
            throw new RuntimeException("Failed to create workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cập nhật workflow
     */
    @Transactional
    public WorkflowResponse updateWorkflow(Long id, WorkflowRequest request) {
        log.info("Updating workflow with id: {}", id);
        
        try {
            Workflow workflow = workflowRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
            
            // Validate JSON data
            if (!isValidJSON(request.getJsonData())) {
                throw new IllegalArgumentException("Invalid JSON data format");
            }
            
            // Check name uniqueness (except current workflow)
            if (!workflow.getName().equals(request.getName()) && 
                workflowRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Workflow with name '" + request.getName() + "' already exists");
            }
            
            // Update fields
            workflow.setName(request.getName());
            workflow.setDescription(request.getDescription());
            workflow.setJsonData(request.getJsonData());
            workflow.setVersion(workflow.getVersion() + 1); // Increment version
            
            if (request.getIsActive() != null) {
                workflow.setIsActive(request.getIsActive());
            }
            
            Workflow savedWorkflow = workflowRepository.save(workflow);
            
            log.info("Updated workflow with id: {}", savedWorkflow.getId());
            return convertToResponse(savedWorkflow);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating workflow: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating workflow", e);
            throw new RuntimeException("Failed to update workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Xóa workflow
     */
    @Transactional
    public boolean deleteWorkflow(Long id) {
        log.info("Deleting workflow with id: {}", id);
        
        try {
            return workflowRepository.findById(id)
                    .map(workflow -> {
                        workflowRepository.delete(workflow);
                        log.info("Deleted workflow with id: {}", id);
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error deleting workflow", e);
            throw new RuntimeException("Failed to delete workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy workflows active
     */
    public List<WorkflowResponse> getActiveWorkflows() {
        log.info("Fetching active workflows");
        
        try {
            List<Workflow> workflows = workflowRepository.findByIsActiveTrue();
            
            return workflows.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting active workflows", e);
            throw new RuntimeException("Failed to get active workflows: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy workflow theo tên
     */
    public Optional<WorkflowResponse> getWorkflowByName(String name) {
        log.info("Fetching workflow with name: {}", name);
        
        try {
            return workflowRepository.findByName(name)
                    .map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Error getting workflow by name", e);
            throw new RuntimeException("Failed to get workflow by name: " + e.getMessage(), e);
        }
    }
    
    /**
     * Duplicate workflow
     */
    @Transactional
    public WorkflowResponse duplicateWorkflow(Long id, String newName) {
        log.info("Duplicating workflow with id: {} to new name: {}", id, newName);
        
        try {
            Workflow originalWorkflow = workflowRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
            
            if (workflowRepository.existsByName(newName)) {
                throw new IllegalArgumentException("Workflow with name '" + newName + "' already exists");
            }
            
            Workflow duplicatedWorkflow = Workflow.builder()
                    .name(newName)
                    .description("Copy of " + originalWorkflow.getDescription())
                    .jsonData(originalWorkflow.getJsonData())
                    .version(1)
                    .isActive(true)
                    .createdBy(originalWorkflow.getCreatedBy())
                    .build();
            
            Workflow savedWorkflow = workflowRepository.save(duplicatedWorkflow);
            
            log.info("Duplicated workflow with new id: {}", savedWorkflow.getId());
            return convertToResponse(savedWorkflow);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error duplicating workflow: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error duplicating workflow", e);
            throw new RuntimeException("Failed to duplicate workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Export workflow as JSON
     */
    public String exportWorkflowAsJSON(Long id) {
        log.info("Exporting workflow with id: {}", id);
        
        try {
            Workflow workflow = workflowRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
            
            return workflow.getJsonData();
        } catch (IllegalArgumentException e) {
            log.error("Workflow not found for export: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error exporting workflow", e);
            throw new RuntimeException("Failed to export workflow: " + e.getMessage(), e);
        }
    }
    
    /**
     * ✅ ENHANCED: Validate JSON format với better error handling
     */
    private boolean isValidJSON(String jsonData) {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            log.warn("JSON data is null or empty");
            return false;
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonData);
            
            // Check if it has required structure for React Flow
            boolean hasNodes = jsonNode.has("nodes");
            boolean hasEdges = jsonNode.has("edges");
            
            if (!hasNodes || !hasEdges) {
                log.warn("JSON missing required 'nodes' or 'edges' fields");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Invalid JSON format: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * ✅ ENHANCED: Convert Entity to Response DTO với null safety
     */
    private WorkflowResponse convertToResponse(Workflow workflow) {
        if (workflow == null) {
            log.warn("Cannot convert null workflow to response");
            return null;
        }
        
        try {
            return WorkflowResponse.builder()
                    .id(workflow.getId())
                    .name(workflow.getName())
                    .description(workflow.getDescription())
                    .jsonData(workflow.getJsonData())
                    .version(workflow.getVersion())
                    .isActive(workflow.getIsActive())
                    .createdBy(workflow.getCreatedBy())
                    .createdAt(workflow.getCreatedAt())
                    .updatedAt(workflow.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting workflow to response", e);
            throw new RuntimeException("Failed to convert workflow to response: " + e.getMessage(), e);
        }
    }
}