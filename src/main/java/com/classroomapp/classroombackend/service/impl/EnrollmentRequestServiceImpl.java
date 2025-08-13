package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.request.CreateEnrollmentRequestDto;
import com.classroomapp.classroombackend.dto.response.EnrollmentRequestDto;
import com.classroomapp.classroombackend.entity.EnrollmentRequest;
import com.classroomapp.classroombackend.entity.EnrollmentRequest.EnrollmentStatus;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.UnauthorizedException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.CourseTemplateRepository;
import com.classroomapp.classroombackend.repository.EnrollmentRequestRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.EnrollmentRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EnrollmentRequestServiceImpl implements EnrollmentRequestService {
    
    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final CourseTemplateRepository courseTemplateRepository;
    private final UserRepository userRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final EmailService emailService;
    
    @Override
    public EnrollmentRequestDto createEnrollmentRequest(CreateEnrollmentRequestDto dto, Long studentId) {
        log.info("Creating enrollment request for student {} and course template {}", studentId, dto.getCourseTemplateId());
        
        // Validate course template exists and is public
        CourseTemplate courseTemplate = courseTemplateRepository.findById(dto.getCourseTemplateId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mẫu khóa học"));
            
        if (!Boolean.TRUE.equals(courseTemplate.getIsPublic())) {
            throw new BusinessLogicException("Mẫu khóa học không mở đăng ký công khai");
        }
        
        // Validate user is student
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh"));
            
        if (student.getRoleId() != 1) { // 1 = STUDENT role
            throw new UnauthorizedException("Chỉ học sinh mới được đăng ký khóa học");
        }
        
        // Check for existing request
        if (enrollmentRequestRepository.existsByStudentAndCourseTemplate(student, courseTemplate)) {
            throw new BusinessLogicException("Bạn đã gửi yêu cầu đăng ký cho khóa học này rồi");
        }
        
        // Check if already enrolled in any classroom for this course template
        if (isAlreadyEnrolled(student, courseTemplate)) {
            throw new BusinessLogicException("Bạn đã được ghi danh vào một lớp của khóa học này");
        }
        
        // Create enrollment request
        EnrollmentRequest request = EnrollmentRequest.builder()
            .courseTemplate(courseTemplate)
            .student(student)
            .status(EnrollmentStatus.PENDING)
            .message(dto.getMessage())
            .build();
            
        request = enrollmentRequestRepository.save(request);
        
        // Send notifications
        sendNotificationsForNewRequest(request);
        
        log.info("Created enrollment request with ID: {}", request.getId());
        return convertToDto(request);
    }
    
    @Override
    public EnrollmentRequestDto approveRequest(Long requestId, Long managerId) {
        log.info("Approving enrollment request {} by manager {}", requestId, managerId);
        
        EnrollmentRequest request = findRequestById(requestId);
        
        if (request.getStatus() != EnrollmentStatus.PENDING) {
            throw new BusinessLogicException("Yêu cầu đã được xử lý");
        }
        
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quản lý"));
        
        // Update request status
        request.setStatus(EnrollmentStatus.APPROVED);
        request.setProcessedBy(manager);
        request.setProcessedAt(LocalDateTime.now());
        
        enrollmentRequestRepository.save(request);
        
        // Find an active classroom for this course template
        addStudentToActiveClassroom(request.getStudent(), request.getCourseTemplate());
        
        // Send approval notification
        sendApprovalNotification(request);
        
        log.info("Approved enrollment request with ID: {}", requestId);
        return convertToDto(request);
    }
    
    @Override
    public EnrollmentRequestDto rejectRequest(Long requestId, Long managerId, String reason) {
        log.info("Rejecting enrollment request {} by manager {}", requestId, managerId);
        
        EnrollmentRequest request = findRequestById(requestId);
        
        if (request.getStatus() != EnrollmentStatus.PENDING) {
            throw new BusinessLogicException("Yêu cầu đã được xử lý");
        }
        
        User manager = userRepository.findById(managerId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quản lý"));
        
        request.setStatus(EnrollmentStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setProcessedBy(manager);
        request.setProcessedAt(LocalDateTime.now());
        
        enrollmentRequestRepository.save(request);
        
        // Send rejection notification
        sendRejectionNotification(request);
        
        log.info("Rejected enrollment request with ID: {}", requestId);
        return convertToDto(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentRequestDto> getRequestsByStatus(EnrollmentStatus status) {
        List<EnrollmentRequest> requests = enrollmentRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentRequestDto> getStudentRequests(Long studentId) {
        List<EnrollmentRequest> requests = enrollmentRequestRepository.findByStudentId(studentId);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public EnrollmentRequestDto getRequestById(Long requestId) {
        EnrollmentRequest request = findRequestById(requestId);
        return convertToDto(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasExistingRequest(Long studentId, Long courseTemplateId) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh"));
        CourseTemplate courseTemplate = courseTemplateRepository.findById(courseTemplateId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mẫu khóa học"));
            
        return enrollmentRequestRepository.existsByStudentAndCourseTemplate(student, courseTemplate);
    }
    
    // Helper methods
    
    private EnrollmentRequest findRequestById(Long requestId) {
        return enrollmentRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu ghi danh"));
    }
    
    private boolean isAlreadyEnrolled(User student, CourseTemplate courseTemplate) {
        // Find all classrooms for this course template using courseId
        List<Classroom> classrooms = classroomRepository.findByCourseId(courseTemplate.getId());
        
        // Check if student is enrolled in any of these classrooms
        for (Classroom classroom : classrooms) {
            ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroom.getId(), student.getId());
            if (classroomEnrollmentRepository.existsById(enrollmentId)) {
                return true;
            }
        }
        return false;
    }
    
    private void addStudentToActiveClassroom(User student, CourseTemplate courseTemplate) {
        // Find active classrooms for this course template with available spots
        List<Classroom> classrooms = classroomRepository.findActiveByCourseId(courseTemplate.getId());
        
        Classroom targetClassroom = null;
        for (Classroom classroom : classrooms) {
            // Check if classroom has available spots
            int currentEnrollments = classroomEnrollmentRepository.findByClassroomId(classroom.getId()).size();
            // For now, assume max students is 30 if not set
            int maxStudents = 30;
            if (currentEnrollments < maxStudents) {
                targetClassroom = classroom;
                break;
            }
        }
        
        if (targetClassroom == null) {
            throw new BusinessLogicException("Không tìm thấy lớp học còn chỗ cho khóa này");
        }
        
        // Create enrollment
        ClassroomEnrollment enrollment = new ClassroomEnrollment();
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(targetClassroom.getId(), student.getId());
        enrollment.setId(enrollmentId);
        enrollment.setClassroom(targetClassroom);
        enrollment.setUser(student);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        
        classroomEnrollmentRepository.save(enrollment);
        
        log.info("Added student {} to classroom {} for course template {}", 
                student.getId(), targetClassroom.getId(), courseTemplate.getId());
    }
    
    private void sendNotificationsForNewRequest(EnrollmentRequest request) {
        try {
            // Send confirmation to student
            emailService.sendEmail(
                request.getStudent().getEmail(),
                "Enrollment Request Received - " + request.getCourseTemplate().getName(),
                buildStudentConfirmationEmail(request)
            );
            
            // Send notification to managers
            List<User> managers = userRepository.findByRoleId(3); // 3 = MANAGER role
            for (User manager : managers) {
                emailService.sendEmail(
                    manager.getEmail(),
                    "New Enrollment Request - " + request.getCourseTemplate().getName(),
                    buildManagerNotificationEmail(request)
                );
            }
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo yêu cầu ghi danh", e);
        }
    }
    
    private void sendApprovalNotification(EnrollmentRequest request) {
        try {
            emailService.sendEmail(
                request.getStudent().getEmail(),
                "Enrollment Approved - " + request.getCourseTemplate().getName(),
                buildApprovalEmail(request)
            );
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo phê duyệt", e);
        }
    }
    
    private void sendRejectionNotification(EnrollmentRequest request) {
        try {
            emailService.sendEmail(
                request.getStudent().getEmail(),
                "Enrollment Request Update - " + request.getCourseTemplate().getName(),
                buildRejectionEmail(request)
            );
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo từ chối", e);
        }
    }
    
    private EnrollmentRequestDto convertToDto(EnrollmentRequest request) {
        return EnrollmentRequestDto.builder()
            .id(request.getId())
            .courseTemplateId(request.getCourseTemplate().getId())
            .courseTemplateName(request.getCourseTemplate().getName())
            .courseTemplateDescription(request.getCourseTemplate().getDescription())
            .studentId(request.getStudent().getId())
            .studentName(request.getStudent().getFullName())
            .studentEmail(request.getStudent().getEmail())
            .status(request.getStatus())
            .message(request.getMessage())
            .rejectionReason(request.getRejectionReason())
            .createdAt(request.getCreatedAt())
            .processedAt(request.getProcessedAt())
            .processedByName(request.getProcessedBy() != null ? request.getProcessedBy().getFullName() : null)
            .build();
    }
    
    // Email template builders (simplified versions)
    private String buildStudentConfirmationEmail(EnrollmentRequest request) {
        return String.format(
            "<h2>Enrollment Request Received</h2>" +
            "<p>Dear %s,</p>" +
            "<p>We have received your enrollment request for <strong>%s</strong>.</p>" +
            "<p>Your request is being reviewed and you will be notified once a decision is made.</p>" +
            "<p>Thank you for your interest!</p>",
            request.getStudent().getFullName(),
            request.getCourseTemplate().getName()
        );
    }
    
    private String buildManagerNotificationEmail(EnrollmentRequest request) {
        return String.format(
            "<h2>New Enrollment Request</h2>" +
            "<p>A new student has requested enrollment:</p>" +
            "<ul>" +
            "<li><strong>Student:</strong> %s (%s)</li>" +
            "<li><strong>Course:</strong> %s</li>" +
            "<li><strong>Message:</strong> %s</li>" +
            "</ul>" +
            "<p>Please review and process this request in the management dashboard.</p>",
            request.getStudent().getFullName(),
            request.getStudent().getEmail(),
            request.getCourseTemplate().getName(),
            request.getMessage() != null ? request.getMessage() : "No message provided"
        );
    }
    
    private String buildApprovalEmail(EnrollmentRequest request) {
        return String.format(
            "<h2 style=\"color: #28a745;\">Enrollment Approved! 🎉</h2>" +
            "<p>Dear %s,</p>" +
            "<p>Congratulations! Your enrollment request for <strong>%s</strong> has been approved.</p>" +
            "<p>You will receive further instructions about course access and payment (if applicable) soon.</p>" +
            "<p>Welcome to the course!</p>",
            request.getStudent().getFullName(),
            request.getCourseTemplate().getName()
        );
    }
    
    private String buildRejectionEmail(EnrollmentRequest request) {
        return String.format(
            "<h2>Enrollment Request Update</h2>" +
            "<p>Dear %s,</p>" +
            "<p>Thank you for your interest in <strong>%s</strong>.</p>" +
            "<p>Unfortunately, we are unable to approve your enrollment request at this time.</p>" +
            "<p><strong>Reason:</strong> %s</p>" +
            "<p>We encourage you to check our other available courses or reapply in the future.</p>",
            request.getStudent().getFullName(),
            request.getCourseTemplate().getName(),
            request.getRejectionReason() != null ? request.getRejectionReason() : "Not specified"
        );
    }
}