package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.requestmanagement.RequestDTO;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.service.AdminRequestService;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.service.ParentService;
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
    private final ParentService parentService;

    @Override
    public List<RequestDTO> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(request -> modelMapper.map(request, RequestDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RequestDTO approveRequest(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessLogicException("Yêu cầu này không thể được phê duyệt");
        }

        // Xác định roleId từ requestedRole
        int roleId = RoleConstants.STUDENT;
        String requestedRole = request.getRequestedRole();
        if ("TEACHER".equalsIgnoreCase(requestedRole)) roleId = RoleConstants.TEACHER;
        else if ("MANAGER".equalsIgnoreCase(requestedRole)) roleId = RoleConstants.MANAGER;
        else if ("ADMIN".equalsIgnoreCase(requestedRole)) roleId = RoleConstants.ADMIN;

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        String tempPassword = null;
        
        if (user == null) {
            // Tạo tài khoản mới
            user = new User();
            user.setUsername(request.getEmail());
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setStatus("active");
            user.setRoleId(roleId);
            user.setCreatedAt(LocalDateTime.now());
            
            // Tạo mật khẩu tạm thời
            tempPassword = generateRandomPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user = userRepository.save(user);
            
            // Nếu là học sinh, tạo tài khoản phụ huynh và liên kết
            if (roleId == RoleConstants.STUDENT) {
                try {
                    createParentAccountAndLinkToStudent(user, request);
                } catch (Exception e) {
                    log.error("Failed to create parent account and link to student for request {}", id, e);
                }
            }
        } else {
            // Cập nhật role cho tài khoản hiện có
            if (user.getRoleId() == null || user.getRoleId() != roleId) {
                user.setRoleId(roleId);
                userRepository.save(user);
                
                // Nếu chuyển thành học sinh, tạo tài khoản phụ huynh và liên kết
                if (roleId == RoleConstants.STUDENT) {
                    try {
                        createParentAccountAndLinkToStudent(user, request);
                    } catch (Exception e) {
                        log.error("Failed to create parent account and link to student for existing user {}", user.getId(), e);
                    }
                }
            }
        }

        // Cập nhật trạng thái yêu cầu
        request.setStatus("APPROVED");
        request.setResultStatus("APPROVED");
        request.setProcessedAt(LocalDateTime.now());
        Request savedRequest = requestRepository.save(request);

        // Gửi email thông báo
        if (tempPassword != null) {
            String roleName = convertRoleIdToRoleName(roleId);
            emailService.sendApprovalEmail(user.getEmail(), user.getFullName(), roleName, tempPassword);
        } else {
            String roleName = convertRoleIdToRoleName(roleId);
            emailService.sendRequestStatusNotification(user.getEmail(), user.getFullName(), roleName, "APPROVED", null);
        }

        // Gửi email thông báo cho phụ huynh nếu có
        if (roleId == RoleConstants.STUDENT) {
            try {
                emailService.sendParentApprovalEmail(request);
            } catch (Exception e) {
                log.error("Failed to send parent approval email for request {}", id, e);
            }
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

    private void createParentAccountAndLinkToStudent(User studentUser, Request request) {
        try {
            String formResponses = request.getFormResponses();
            if (formResponses != null && !formResponses.trim().isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode formData = objectMapper.readTree(formResponses);
                
                if (formData.has("parentEmail") && formData.has("parentFullName")) {
                    String parentEmail = formData.get("parentEmail").asText();
                    String parentFullName = formData.get("parentFullName").asText();
                    String parentPhone = formData.has("parentPhoneNumber") ? 
                        formData.get("parentPhoneNumber").asText() : null;

                    // Kiểm tra xem phụ huynh đã tồn tại chưa
                    Optional<User> existingParent = userRepository.findByEmail(parentEmail);
                    User parentUser;
                    
                    if (existingParent.isPresent()) {
                        parentUser = existingParent.get();
                        // Cập nhật role thành PARENT nếu chưa phải
                        if (parentUser.getRoleId() == null || parentUser.getRoleId() != RoleConstants.PARENT) {
                            parentUser.setRoleId(RoleConstants.PARENT);
                            userRepository.save(parentUser);
                        }
                    } else {
                        // Tạo tài khoản phụ huynh mới
                        parentUser = new User();
                        parentUser.setUsername(parentEmail);
                        parentUser.setEmail(parentEmail);
                        parentUser.setFullName(parentFullName);
                        parentUser.setPhoneNumber(parentPhone);
                        parentUser.setStatus("active");
                        parentUser.setRoleId(RoleConstants.PARENT);
                        parentUser.setCreatedAt(LocalDateTime.now());
                        
                        // Tạo mật khẩu cho phụ huynh
                        String parentPassword = generateRandomPassword();
                        parentUser.setPassword(passwordEncoder.encode(parentPassword));
                        parentUser = userRepository.save(parentUser);

                        // Gửi email thông báo cho phụ huynh
                        emailService.sendApprovalEmail(parentUser.getEmail(), parentUser.getFullName(), 
                            "PARENT", parentPassword);
                    }

                    // Tạo Parent entity nếu chưa có
                    try {
                        parentService.createParentFromUser(parentUser.getId(), parentUser.getFullName(), 
                            parentUser.getPhoneNumber(), parentUser.getEmail());
                    } catch (Exception e) {
                        // Nếu Parent entity đã tồn tại, bỏ qua lỗi
                        log.info("Parent entity may already exist for user {}", parentUser.getId());
                    }

                    // Liên kết phụ huynh với học sinh
                    parentService.linkParentToStudent(parentUser.getId(), studentUser.getId(),
                        StudentParent.RelationType.GUARDIAN, true, true);

                    log.info("Successfully created/linked parent account {} to student {}", 
                        parentUser.getId(), studentUser.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error creating parent account and linking to student", e);
            // Không throw exception để không làm fail việc tạo tài khoản học sinh
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