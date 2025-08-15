package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.requestmanagement.RequestDTO;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.service.AdminRequestService;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRequestServiceImpl implements AdminRequestService {

    private final RequestRepository requestRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<RequestDTO> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(request -> modelMapper.map(request, RequestDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RequestDTO approveRequest(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với id: " + id));
        // Logic to approve request and create user
        request.setStatus("COMPLETED");
        request.setResultStatus("APPROVED");
        request.setProcessedAt(java.time.LocalDateTime.now());
        Request savedRequest = requestRepository.save(request);

        // Xác định roleId từ requestedRole
        int roleId = RoleConstants.STUDENT;
        String requestedRole = request.getRequestedRole();
        if ("TEACHER".equalsIgnoreCase(requestedRole)) roleId = RoleConstants.TEACHER;
        else if ("MANAGER".equalsIgnoreCase(requestedRole)) roleId = RoleConstants.MANAGER;
        else if ("ADMIN".equalsIgnoreCase(requestedRole)) roleId = RoleConstants.ADMIN;
        else if ("PARENT".equalsIgnoreCase(requestedRole)) roleId = RoleConstants.PARENT;

        // Kiểm tra user đã tồn tại chưa
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        String tempPassword = null;
        if (user == null) {
            // Tạo user mới
            tempPassword = generateRandomPassword();
            user = new User();
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setUsername(request.getEmail());
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setRoleId(roleId);
            user.setPhoneNumber(request.getPhoneNumber());
            user.setStatus("active");
            userRepository.save(user);
            
            // Nếu là tài khoản phụ huynh, tạo entity Parent và liên kết với con em
            if (roleId == RoleConstants.PARENT) {
                try {
                    createParentAndLinkChildren(user, request);
                } catch (Exception e) {
                    log.error("Failed to create parent entity and link children for request {}", id, e);
                    // Don't fail the approval if parent creation fails
                }
            }
        } else {
            // Nếu user đã có, cập nhật role nếu khác
            if (user.getRoleId() == null || user.getRoleId() != roleId) {
                user.setRoleId(roleId);
                userRepository.save(user);
                
                // Nếu chuyển thành phụ huynh, tạo entity Parent và liên kết với con em
                if (roleId == RoleConstants.PARENT) {
                    try {
                        createParentAndLinkChildren(user, request);
                    } catch (Exception e) {
                        log.error("Failed to create parent entity and link children for existing user {}", user.getId(), e);
                    }
                }
            }
        }
        // Gửi email thông báo phê duyệt
        if (tempPassword != null) {
            // Convert roleId back to role name for email
            String roleName = convertRoleIdToRoleName(roleId);
            emailService.sendApprovalEmail(user.getEmail(), user.getFullName(), roleName, tempPassword);
        } else {
            String roleName = convertRoleIdToRoleName(roleId);
            emailService.sendRequestStatusNotification(user.getEmail(), user.getFullName(), roleName, "APPROVED", null);
        }
        return modelMapper.map(savedRequest, RequestDTO.class);
    }

    @Override
    public RequestDTO rejectRequest(Long id, String reason) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với id: " + id));
        request.setStatus("COMPLETED");
        request.setResultStatus("REJECTED");
        request.setRejectReason(reason);
        request.setProcessedAt(java.time.LocalDateTime.now());
        Request savedRequest = requestRepository.save(request);
        // Gửi email từ chối
        emailService.sendRequestStatusNotification(request.getEmail(), request.getFullName(), request.getRequestedRole(), "REJECTED", reason);
        return modelMapper.map(savedRequest, RequestDTO.class);
    }

    @Override
    public List<RequestDTO> getPendingRequests() {
        return requestRepository.findByStatus("PENDING").stream()
                .map(request -> modelMapper.map(request, RequestDTO.class))
                .collect(Collectors.toList());
    }

    private String generateRandomPassword() {
        // Sinh mật khẩu ngẫu nhiên 8 ký tự
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

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
                            User childUser = userRepository.findByEmail(childEmail).orElse(null);
                            if (childUser != null) {
                                // Link parent to child through ParentService
                                // For now, we'll log the relationship
                                log.info("Linking parent {} to child {}", parentUser.getId(), childUser.getId());
                                
                                // TODO: Implement actual linking through ParentService
                                // parentService.linkParentToStudent(parentUser.getId(), childUser.getId(), 
                                //     StudentParent.RelationType.GUARDIAN, true, true);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing form responses for parent request", e);
        }
    }

    private String convertRoleIdToRoleName(int roleId) {
        switch (roleId) {
            case RoleConstants.STUDENT:
                return "Student";
            case RoleConstants.TEACHER:
                return "Teacher";
            case RoleConstants.MANAGER:
                return "Manager";
            case RoleConstants.ADMIN:
                return "Admin";
            case RoleConstants.PARENT:
                return "Parent";
            default:
                return "Unknown Role";
        }
    }
} 