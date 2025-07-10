package com.classroomapp.classroombackend.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.dto.RequestDTO;
import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.dto.requestmanagement.CreateRequestDto;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.RequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void createRegistrationRequest(CreateRequestDto dto) {
        if (requestRepository.existsByEmailAndStatusIn(dto.getEmail(), List.of("PENDING", "APPROVED")) || userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessLogicException("An account with this email already exists or is pending approval.");
        }

        Request newRequest = new Request();
        newRequest.setEmail(dto.getEmail());
        newRequest.setFullName(dto.getFullName());
        newRequest.setPhoneNumber(dto.getPhoneNumber());
        newRequest.setRequestedRole(dto.getRequestedRole());
        newRequest.setFormResponses(dto.getFormResponses());
        newRequest.setStatus("PENDING");
        newRequest.setCreatedAt(LocalDateTime.now());

        requestRepository.save(newRequest);
        // Gửi mail xác nhận đã nhận request
        try {
            emailService.sendFormCompletionConfirmation(
                newRequest.getEmail(),
                newRequest.getFullName(),
                newRequest.getRequestedRole()
            );
        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
        }
    }

    @Override
    @Transactional(noRollbackFor = BusinessLogicException.class)
    public RequestResponseDTO createRequest(RequestDTO requestDTO) {
        // Check if there's already an active request
        if (hasActiveRequest(requestDTO.getEmail(), requestDTO.getRequestedRole())) {
            throw new BusinessLogicException("Already has an active request for this role");
        }

        Request request = new Request();
        request.setEmail(requestDTO.getEmail());
        request.setFullName(requestDTO.getFullName());
        request.setPhoneNumber(requestDTO.getPhoneNumber());
        request.setRequestedRole(requestDTO.getRequestedRole());
        request.setFormResponses(requestDTO.getFormResponses());
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());

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
                .orElseThrow(() -> new BusinessLogicException("Request not found with ID: " + requestId));
        log.info("Found request: {}", request);

        if (!"PENDING".equals(request.getStatus())) {
            log.warn("Request {} is not in PENDING status. Current status: {}", requestId, request.getStatus());
            throw new BusinessLogicException("Request is not in PENDING status. Current status: " + request.getStatus());
        }

        // Create User from Request info
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        // Note: The User entity appears to use 'fullName' based on other files.
        // If it uses firstName/lastName, this needs adjustment. Assuming fullName for now.
        newUser.setFullName(request.getFullName());
        newUser.setPhoneNumber(request.getPhoneNumber());

        String randomPassword = generateRandomPassword();
        newUser.setPassword(passwordEncoder.encode(randomPassword));
        newUser.setStatus("active");
        
        // Determine roleId from the requested role string
        int roleId = RoleConstants.STUDENT; // Default to STUDENT
        if ("TEACHER".equalsIgnoreCase(request.getRequestedRole())) {
            roleId = RoleConstants.TEACHER;
        }
        newUser.setRoleId(roleId);

        userRepository.save(newUser);
        log.info("Successfully created user for request {}", requestId);

        // Update Request status
        log.info("Setting request {} status to APPROVED", requestId);
        request.setStatus("APPROVED");
        request.setProcessedAt(LocalDateTime.now());

        Request savedRequest = requestRepository.save(request);

        // Send approval notification with temporary password
        try {
            log.info("Sending approval notification for request {}", requestId);
            // Re-fetch role name for the email
            String roleNameForEmail = "STUDENT";
            if (roleId == RoleConstants.TEACHER) {
                roleNameForEmail = "TEACHER";
            }
            emailService.sendApprovalEmail(newUser.getEmail(), newUser.getFullName(), roleNameForEmail, randomPassword);
            log.info("Successfully sent approval notification for request {}", requestId);
        } catch (Exception e) {
            log.error("Failed to send approval email for request {}: {}", requestId, e.getMessage(), e);
        }

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

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }
} 