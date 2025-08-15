package com.classroomapp.classroombackend.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
import com.classroomapp.classroombackend.service.ParentService;
import com.classroomapp.classroombackend.service.RequestService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ParentService parentService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void createRegistrationRequest(CreateRequestDto dto) {
        if (requestRepository.existsByEmailAndStatusIn(dto.getEmail(), List.of("PENDING", "APPROVED")) || userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessLogicException("Đã tồn tại tài khoản với email này hoặc đang chờ phê duyệt.");
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
            log.error("Gửi email xác nhận thất bại", e);
        }
    }

    @Override
    @Transactional(noRollbackFor = BusinessLogicException.class)
    public RequestResponseDTO createRequest(RequestDTO requestDTO) {
        // Check if there's already an active request
        if (hasActiveRequest(requestDTO.getEmail(), requestDTO.getRequestedRole())) {
            throw new BusinessLogicException("Đã có một yêu cầu đang hoạt động cho vai trò này");
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
            log.error("Gửi email xác nhận thất bại", e);
            // Don't fail the request if email fails
        }

        return convertToDTO(savedRequest);
    }

    @Override
    @Transactional
    public RequestResponseDTO approveRequest(Long requestId) {
        log.info("Starting approval process for request ID: {}", requestId);
        
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessLogicException("Không tìm thấy yêu cầu với ID: " + requestId));
        log.info("Found request: {}", request);

        if (!"PENDING".equals(request.getStatus())) {
            log.warn("Yêu cầu {} không ở trạng thái PENDING. Trạng thái hiện tại: {}", requestId, request.getStatus());
            throw new BusinessLogicException("Yêu cầu không ở trạng thái PENDING. Trạng thái hiện tại: " + request.getStatus());
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
        } else if ("PARENT".equalsIgnoreCase(request.getRequestedRole())) {
            roleId = RoleConstants.PARENT;
        }
        newUser.setRoleId(roleId);

        userRepository.save(newUser);
        log.info("Successfully created user for request {}", requestId);

        // If this is a parent request, create parent entity and link to children
        if (roleId == RoleConstants.PARENT) {
            try {
                createParentAndLinkChildren(newUser, request);
            } catch (Exception e) {
                log.error("Failed to create parent entity and link children for request {}", requestId, e);
                // Don't fail the approval if parent creation fails
            }
        }

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
            } else if (roleId == RoleConstants.PARENT) {
                roleNameForEmail = "PARENT";
            }
            emailService.sendApprovalEmail(newUser.getEmail(), newUser.getFullName(), roleNameForEmail, randomPassword);
            log.info("Successfully sent approval notification for request {}", requestId);
        } catch (Exception e) {
            log.error("Gửi email phê duyệt cho yêu cầu {} thất bại: {}", requestId, e.getMessage(), e);
        }

        RequestResponseDTO responseDTO = convertToDTO(savedRequest);
        log.info("Returning response DTO: {}", responseDTO);
        return responseDTO;
    }

    @Override
    @Transactional
    public RequestResponseDTO rejectRequest(Long requestId, String reason) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Yêu cầu không ở trạng thái PENDING");
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
            log.error("Gửi email từ chối thất bại", e);
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
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
            log.warn("Yêu cầu {} không ở trạng thái PENDING. Trạng thái hiện tại: {}", requestId, request.getStatus());
            throw new RuntimeException("Yêu cầu không ở trạng thái PENDING. Trạng thái hiện tại: " + request.getStatus());
        }

        log.info("Setting request {} status to APPROVED", requestId);
        request.setStatus("APPROVED");
        request.setProcessedAt(LocalDateTime.now());
        
        // Skip user creation/update
        log.info("BỎ QUA tạo/cập nhật người dùng cho mục đích kiểm thử");
        
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
            log.error("Gửi email phê duyệt cho yêu cầu {} thất bại: {}", requestId, e.getMessage(), e);
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

    /**
     * Create parent entity and link to children based on request data
     */
    private void createParentAndLinkChildren(User parentUser, Request request) {
        try {
            // Parse form responses to get children emails
            String formResponses = request.getFormResponses();
            if (formResponses != null && !formResponses.trim().isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode formData = objectMapper.readTree(formResponses);
                
                if (formData.has("childrenEmails") && formData.get("childrenEmails").isArray()) {
                    JsonNode childrenEmails = formData.get("childrenEmails");
                    
                    for (JsonNode emailNode : childrenEmails) {
                        String childEmail = emailNode.asText();
                        if (childEmail != null && !childEmail.trim().isEmpty()) {
                            // Find child user by email
                            Optional<User> childUser = userRepository.findByEmail(childEmail);
                            if (childUser.isPresent()) {
                                // Link parent to child
                                linkParentToChild(parentUser.getId(), childUser.get().getId());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing form responses for parent request", e);
        }
    }

    /**
     * Link parent to child through StudentParent relationship
     */
    private void linkParentToChild(Long parentId, Long childId) {
        try {
            // Use ParentService to link parent to child
            log.info("Linking parent {} to child {}", parentId, childId);
            
            // Create parent entity first
            parentService.createParentFromUser(parentId, "", "", "");
            
            // Link parent to student
            parentService.linkParentToStudent(parentId, childId, 
                com.classroomapp.classroombackend.model.StudentParent.RelationType.GUARDIAN, true, true);
            
            log.info("Successfully linked parent {} to child {}", parentId, childId);
            
        } catch (Exception e) {
            log.error("Failed to link parent {} to child {}", parentId, childId, e);
        }
    }
} 