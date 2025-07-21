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
 * Hỗ trợ CRUD operations và JSON validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {
    
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Lấy tất cả workflows
     */
    public List<WorkflowResponse> getAllWorkflows() {
        log.info("Fetching all workflows");
        
        List<Workflow> workflows = workflowRepository.findAll();
        
        return workflows.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy workflow theo ID
     */
    public Optional<WorkflowResponse> getWorkflowById(Long id) {
        log.info("Fetching workflow with id: {}", id);
        
        return workflowRepository.findById(id)
                .map(this::convertToResponse);
    }
    
    /**
     * Tạo workflow mới
     */
    @Transactional
    public WorkflowResponse createWorkflow(WorkflowRequest request) {
        log.info("Creating new workflow: {}", request.getName());
        
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
    }
    
    /**
     * Cập nhật workflow
     */
    @Transactional
    public WorkflowResponse updateWorkflow(Long id, WorkflowRequest request) {
        log.info("Updating workflow with id: {}", id);
        
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
    }
    
    /**
     * Xóa workflow
     */
    @Transactional
    public boolean deleteWorkflow(Long id) {
        log.info("Deleting workflow with id: {}", id);
        
        return workflowRepository.findById(id)
                .map(workflow -> {
                    workflowRepository.delete(workflow);
                    log.info("Deleted workflow with id: {}", id);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Lấy workflows active
     */
    public List<WorkflowResponse> getActiveWorkflows() {
        log.info("Fetching active workflows");
        
        List<Workflow> workflows = workflowRepository.findByIsActiveTrue();
        
        return workflows.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy workflow theo tên
     */
    public Optional<WorkflowResponse> getWorkflowByName(String name) {
        log.info("Fetching workflow with name: {}", name);
        
        return workflowRepository.findByName(name)
                .map(this::convertToResponse);
    }
    
    /**
     * Duplicate workflow
     */
    @Transactional
    public WorkflowResponse duplicateWorkflow(Long id, String newName) {
        log.info("Duplicating workflow with id: {} to new name: {}", id, newName);
        
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
    }
    
    /**
     * Export workflow as JSON
     */
    public String exportWorkflowAsJSON(Long id) {
        log.info("Exporting workflow with id: {}", id);
        
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found with id: " + id));
        
        return workflow.getJsonData();
    }
    
    /**
     * Validate JSON format
     */
    private boolean isValidJSON(String jsonData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonData);
            
            // Check if it has required structure for React Flow
            return jsonNode.has("nodes") && jsonNode.has("edges");
            
        } catch (Exception e) {
            log.error("Invalid JSON format: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Convert Entity to Response DTO
     */
    private WorkflowResponse convertToResponse(Workflow workflow) {
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
    }
}
