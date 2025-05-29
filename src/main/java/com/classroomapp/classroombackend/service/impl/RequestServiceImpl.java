package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.RequestDTO;
import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.repository.RequestRepository;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.RequestService;
import com.classroomapp.classroombackend.service.UserServiceExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EmailService emailService;
    private final UserServiceExtension userService;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    @Transactional
    public RequestResponseDTO createRequest(RequestDTO requestDTO) {
        // Check if there's already an active request
        if (hasActiveRequest(requestDTO.getEmail(), requestDTO.getRequestedRole())) {
            throw new RuntimeException("Already has an active request for this role");
        }

        Request request = new Request();
        request.setEmail(requestDTO.getEmail());
        request.setFullName(requestDTO.getFullName());
        request.setPhoneNumber(requestDTO.getPhoneNumber());
        request.setRequestedRole(requestDTO.getRequestedRole());
        request.setFormResponses(requestDTO.getFormResponses());
        request.setStatus("PENDING");

        Request savedRequest = requestRepository.save(request);
        
        // Send confirmation email
        try {
            emailService.sendFormCompletionConfirmation(
                request.getEmail(), 
                request.getFullName(), 
                request.getRequestedRole()
            );
        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
            // Don't fail the request if email fails
        }

        return convertToDTO(savedRequest);
    }

    @Override
    @Transactional
    public RequestResponseDTO approveRequest(Long requestId) {
        log.info("Starting approval process for request ID: {}", requestId);
        
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));
        log.info("Found request: {}", request);

        if (!"PENDING".equals(request.getStatus())) {
            log.warn("Request {} is not in PENDING status. Current status: {}", requestId, request.getStatus());
            throw new RuntimeException("Request is not in PENDING status. Current status: " + request.getStatus());
        }

        log.info("Setting request {} status to APPROVED", requestId);
        request.setStatus("APPROVED");
        request.setProcessedAt(LocalDateTime.now());

        // Update or create user with the requested role
        try {
            log.info("Attempting to create/update user for request {}: email={}, name={}, role={}", 
                    requestId, request.getEmail(), request.getFullName(), request.getRequestedRole());
                    
            boolean success = userService.createOrUpdateUser(
                request.getEmail(), 
                request.getFullName(), 
                request.getRequestedRole()
            );
            
            if (!success) {
                log.warn("Failed to update user role, but request was approved. Request ID: {}", requestId);
            } else {
                log.info("Successfully created/updated user for request {}", requestId);
            }
        } catch (Exception e) {
            log.error("Error updating user role for request {}: {}", requestId, e.getMessage(), e);
            // Don't fail the approval if user update fails
        }
        
        // Send approval notification
        try {
            log.info("Sending approval notification for request {}", requestId);
            emailService.sendRequestStatusNotification(
                request.getEmail(),
                request.getFullName(),
                request.getRequestedRole(),
                "APPROVED", 
                null
            );
            log.info("Successfully sent approval notification for request {}", requestId);
        } catch (Exception e) {
            log.error("Failed to send approval email for request {}: {}", requestId, e.getMessage(), e);
            // Don't fail the approval if email fails
        }

        log.info("Saving approved request to database: {}", request);
        Request savedRequest = requestRepository.save(request);
        log.info("Request successfully saved with ID: {}", savedRequest.getId());
        
        RequestResponseDTO responseDTO = convertToDTO(savedRequest);
        log.info("Returning response DTO: {}", responseDTO);
        return responseDTO;
    }

    @Override
    @Transactional
    public RequestResponseDTO rejectRequest(Long requestId, String reason) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request is not in PENDING status");
        }

        request.setStatus("REJECTED");
        request.setRejectReason(reason);
        request.setProcessedAt(LocalDateTime.now());
        
        // Send rejection notification
        try {
            emailService.sendRequestStatusNotification(
                request.getEmail(),
                request.getFullName(),
                request.getRequestedRole(),
                "REJECTED", 
                reason
            );
        } catch (Exception e) {
            log.error("Failed to send rejection email", e);
            // Don't fail the rejection if email fails
        }

        return convertToDTO(requestRepository.save(request));
    }

    @Override
    public List<RequestResponseDTO> getPendingRequests() {
        return requestRepository.findByStatus("PENDING").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestResponseDTO> getRequestsByEmail(String email) {
        return requestRepository.findByEmail(email).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasActiveRequest(String email, String role) {
        return requestRepository.existsByEmailAndStatusAndRequestedRole(email, "PENDING", role);
    }

    @Override
    public RequestResponseDTO getRequestDetails(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return convertToDTO(request);
    }

    @Override
    public List<RequestResponseDTO> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Alternative approval method that skips user creation/update
     * This helps isolate if the user service is causing issues
     */
    public RequestResponseDTO approveRequestSkipUserCreation(Long requestId) {
        log.info("Starting simplified approval process for request ID: {} (skipping user creation)", requestId);
        
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));
        log.info("Found request: {}", request);

        if (!"PENDING".equals(request.getStatus())) {
            log.warn("Request {} is not in PENDING status. Current status: {}", requestId, request.getStatus());
            throw new RuntimeException("Request is not in PENDING status. Current status: " + request.getStatus());
        }

        log.info("Setting request {} status to APPROVED", requestId);
        request.setStatus("APPROVED");
        request.setProcessedAt(LocalDateTime.now());
        
        // Skip user creation/update
        log.info("SKIPPING user creation/update for testing purposes");
        
        // Send approval notification
        try {
            log.info("Sending approval notification for request {}", requestId);
            emailService.sendRequestStatusNotification(
                request.getEmail(),
                request.getFullName(),
                request.getRequestedRole(),
                "APPROVED", 
                null
            );
            log.info("Successfully sent approval notification for request {}", requestId);
        } catch (Exception e) {
            log.error("Failed to send approval email for request {}: {}", requestId, e.getMessage(), e);
            // Don't fail the approval if email fails
        }

        log.info("Saving approved request to database: {}", request);
        Request savedRequest = requestRepository.save(request);
        log.info("Request successfully saved with ID: {}", savedRequest.getId());
        
        RequestResponseDTO responseDTO = convertToDTO(savedRequest);
        log.info("Returning response DTO: {}", responseDTO);
        return responseDTO;
    }

    private RequestResponseDTO convertToDTO(Request request) {
        RequestResponseDTO dto = new RequestResponseDTO();
        dto.setId(request.getId());
        dto.setEmail(request.getEmail());
        dto.setFullName(request.getFullName());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setRequestedRole(request.getRequestedRole());
        dto.setStatus(request.getStatus());
        dto.setRejectReason(request.getRejectReason());
        dto.setCreatedAt(request.getCreatedAt().format(formatter));
        dto.setProcessedAt(request.getProcessedAt() != null ? 
                request.getProcessedAt().format(formatter) : null);
        dto.setFormResponses(request.getFormResponses());
        return dto;
    }
} 