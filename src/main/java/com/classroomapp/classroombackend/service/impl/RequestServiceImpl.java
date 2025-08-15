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
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.ParentService;
import com.classroomapp.classroombackend.service.RequestService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

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

        // Validate formResponses nếu là yêu cầu học sinh
        if ("STUDENT".equals(dto.getRequestedRole()) && dto.getFormResponses() != null) {
            validateStudentFormData(dto.getFormResponses());
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
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessLogicException("Yêu cầu này không thể được phê duyệt");
        }

        // Tạo tài khoản học sinh
        User newUser = new User();
        newUser.setUsername(request.getEmail());
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setStatus("active");
        newUser.setCreatedAt(LocalDateTime.now());

        // Set role cho học sinh
        int roleId = RoleConstants.STUDENT;
        if ("TEACHER".equalsIgnoreCase(request.getRequestedRole())) {
            roleId = RoleConstants.TEACHER;
        }
        newUser.setRoleId(roleId);
        userRepository.save(newUser);

        // Nếu là yêu cầu học sinh, tạo tài khoản phụ huynh và liên kết
        if (roleId == RoleConstants.STUDENT) {
            try {
                createParentAccountAndLinkToStudent(newUser, request);
            } catch (Exception e) {
                log.error("Failed to create parent account and link to student for request {}", requestId, e);
            }
        }

        // Cập nhật trạng thái yêu cầu
        request.setStatus("APPROVED");
        request.setResultStatus("APPROVED");
        request.setProcessedAt(LocalDateTime.now());
        requestRepository.save(request);

        // Gửi email thông báo phê duyệt
        String randomPassword = generateRandomPassword();
        newUser.setPassword(passwordEncoder.encode(randomPassword));
        userRepository.save(newUser);

        // Gửi email cho học sinh
        String roleNameForEmail = "STUDENT";
        if (roleId == RoleConstants.TEACHER) {
            roleNameForEmail = "TEACHER";
        }
        emailService.sendApprovalEmail(newUser.getEmail(), newUser.getFullName(), roleNameForEmail, randomPassword);

        // Gửi email cho phụ huynh nếu có
        if (roleId == RoleConstants.STUDENT) {
            try {
                emailService.sendParentApprovalEmail(request);
            } catch (Exception e) {
                log.error("Failed to send parent approval email for request {}", requestId, e);
            }
        }

        return convertToDTO(request);
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

                    // Tạo tài khoản phụ huynh
                    User parentUser = new User();
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

                    // Tạo Parent entity
                    parentService.createParentFromUser(parentUser.getId(), parentUser.getFullName(), 
                        parentUser.getPhoneNumber(), parentUser.getEmail());

                    // Liên kết phụ huynh với học sinh
                    parentService.linkParentToStudent(parentUser.getId(), studentUser.getId(),
                        StudentParent.RelationType.GUARDIAN, true, true);

                    log.info("Successfully created parent account {} and linked to student {}", 
                        parentUser.getId(), studentUser.getId());

                    // Gửi email thông báo cho phụ huynh
                    emailService.sendApprovalEmail(parentUser.getEmail(), parentUser.getFullName(), 
                        "PARENT", parentPassword);
                }
            }
        } catch (Exception e) {
            log.error("Error creating parent account and linking to student", e);
            // Không throw exception để không làm fail việc tạo tài khoản học sinh
        }
    }

    private void validateStudentFormData(String formResponses) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode formData = objectMapper.readTree(formResponses);

            // Validate các trường bắt buộc
            if (!formData.has("parentEmail") || formData.get("parentEmail").asText().trim().isEmpty()) {
                throw new BusinessLogicException("Email phụ huynh không được để trống");
            }

            if (!formData.has("parentFullName") || formData.get("parentFullName").asText().trim().isEmpty()) {
                throw new BusinessLogicException("Họ và tên phụ huynh không được để trống");
            }

            if (!formData.has("parentPhoneNumber") || formData.get("parentPhoneNumber").asText().trim().isEmpty()) {
                throw new BusinessLogicException("Số điện thoại phụ huynh không được để trống");
            }

            // Validate độ dài các trường
            String parentEmail = formData.get("parentEmail").asText();
            if (parentEmail.length() > 50) {
                throw new BusinessLogicException("Email phụ huynh không được quá 50 ký tự");
            }

            String parentFullName = formData.get("parentFullName").asText();
            if (parentFullName.length() > 50) {
                throw new BusinessLogicException("Họ và tên phụ huynh không được quá 50 ký tự");
            }

            String parentPhoneNumber = formData.get("parentPhoneNumber").asText();
            if (parentPhoneNumber.length() < 10 || parentPhoneNumber.length() > 11) {
                throw new BusinessLogicException("Số điện thoại phụ huynh phải có từ 10 đến 11 chữ số");
            }
            if (!parentPhoneNumber.startsWith("0")) {
                throw new BusinessLogicException("Số điện thoại phụ huynh phải bắt đầu bằng số 0");
            }

            // Validate trường thông tin thêm (nếu có)
            if (formData.has("additionalInfo")) {
                String additionalInfo = formData.get("additionalInfo").asText();
                if (additionalInfo != null && additionalInfo.length() > 200) {
                    throw new BusinessLogicException("Thông tin thêm không được quá 200 ký tự");
                }
            }

        } catch (JsonProcessingException e) {
            throw new BusinessLogicException("Dữ liệu form không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof BusinessLogicException) {
                throw e;
            }
            throw new BusinessLogicException("Dữ liệu form không hợp lệ: " + e.getMessage());
        }
    }
} 