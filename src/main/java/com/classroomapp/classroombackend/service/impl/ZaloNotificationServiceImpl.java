package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.notification.ZaloNotificationDto;
import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.ParentNotificationPrefs;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ZaloNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// import org.springframework.retry.annotation.Backoff;
// import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloNotificationServiceImpl implements ZaloNotificationService {
    
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final ClassroomRepository classroomRepository;
    // private final LectureRepository lectureRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.n8n.webhook.url:}")
    private String n8nWebhookUrl;
    
    @Value("${app.zalo.notification.enabled:false}")
    private boolean zaloNotificationEnabled;
    
    @Override
    public void sendAttendanceNotification(AttendanceSubmitDto submitDto, Long teacherId) {
        if (!isZaloNotificationEnabled()) {
            log.debug("Zalo notification is disabled, skipping notification");
            return;
        }
        
        if (n8nWebhookUrl == null || n8nWebhookUrl.trim().isEmpty()) {
            log.warn("n8n webhook URL is not configured, skipping Zalo notification");
            return;
        }
        
        try {
            log.info("Building notification data for attendance submission - classroomId: {}, lectureId: {}, teacherId: {}", 
                    submitDto.getClassroomId(), submitDto.getLectureId(), teacherId);
            
            ZaloNotificationDto notificationData = buildNotificationData(submitDto, teacherId);
            
            if (notificationData.getStudentNotifications().isEmpty()) {
                log.info("No students with parent contact information found, skipping notification");
                return;
            }
            
            boolean success = sendToN8nWebhook(notificationData);
            
            if (success) {
                log.info("Successfully sent attendance notification to n8n webhook for {} students", 
                        notificationData.getStudentNotifications().size());
            } else {
                log.error("Failed to send attendance notification to n8n webhook");
            }
            
        } catch (Exception e) {
            log.error("Error sending attendance notification: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public ZaloNotificationDto buildNotificationData(AttendanceSubmitDto submitDto, Long teacherId) {
        // Get teacher information
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId));
        
        // Get classroom information
        Classroom classroom = classroomRepository.findById(submitDto.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found: " + submitDto.getClassroomId()));
        
        // Get lecture information (optional) - commented out for now
        // Lecture lecture = null;
        // if (submitDto.getLectureId() != null) {
        //     lecture = lectureRepository.findById(submitDto.getLectureId()).orElse(null);
        // }
        
        // Build classroom info
        ZaloNotificationDto.ClassroomInfo classroomInfo = ZaloNotificationDto.ClassroomInfo.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .code(classroom.getSection()) // Use section as code
                .subject(classroom.getDescription()) // Use description as subject
                .build();
        
        // Build session info
        ZaloNotificationDto.AttendanceSessionInfo sessionInfo = ZaloNotificationDto.AttendanceSessionInfo.builder()
                .sessionDate(LocalDateTime.now().toLocalDate())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .sessionType("REGULAR")
                .lectureId(submitDto.getLectureId())
                .build();
        
        // Build teacher info
        ZaloNotificationDto.TeacherInfo teacherInfo = ZaloNotificationDto.TeacherInfo.builder()
                .id(teacher.getId())
                .name(teacher.getFullName())
                .email(teacher.getEmail())
                .phoneNumber(teacher.getPhoneNumber())
                .build();
        
        // Build student notifications
        List<ZaloNotificationDto.StudentAttendanceNotification> studentNotifications = new ArrayList<>();
        
        for (AttendanceSubmitDto.AttendanceRecord record : submitDto.getRecords()) {
            Optional<User> studentOpt = userRepository.findById(record.getStudentId());
            if (studentOpt.isEmpty()) {
                log.warn("Student not found: {}", record.getStudentId());
                continue;
            }
            
            User student = studentOpt.get();
            
            // Get parents for this student
            List<Parent> parents = parentRepository.findByStudentId(student.getId());
            
            if (parents.isEmpty()) {
                log.debug("No parents found for student: {}", student.getId());
                continue;
            }
            
            // Build student info
            ZaloNotificationDto.StudentInfo studentInfo = ZaloNotificationDto.StudentInfo.builder()
                    .id(student.getId())
                    .name(student.getFullName())
                    .studentCode(student.getUsername())
                    .email(student.getEmail())
                    .build();
            
            // Build attendance info
            ZaloNotificationDto.AttendanceInfo attendanceInfo = ZaloNotificationDto.AttendanceInfo.builder()
                    .status(record.getStatus())
                    .note(record.getNote())
                    .markedAt(LocalDateTime.now())
                    .build();
            
            // Build parent contact info
            List<ZaloNotificationDto.ParentContactInfo> parentContacts = new ArrayList<>();
            for (Parent parent : parents) {
                // Get relationship info
                Optional<StudentParent> relationshipOpt = studentParentRepository
                        .findActiveRelationship(parent.getId(), student.getId());
                
                if (relationshipOpt.isEmpty()) {
                    continue;
                }
                
                StudentParent relationship = relationshipOpt.get();
                
                // Check if parent wants to receive notifications
                boolean notificationEnabled = true; // Default to true
                if (parent.getNotificationPrefs() != null) {
                    ParentNotificationPrefs prefs = parent.getNotificationPrefs();
                    notificationEnabled = prefs.isEventEnabled("attendance_flagged");
                }
                
                ZaloNotificationDto.ParentContactInfo parentContact = ZaloNotificationDto.ParentContactInfo.builder()
                        .parentId(parent.getId())
                        .parentName(parent.getName())
                        .phoneNumber(parent.getPhone())
                        .email(parent.getEmail())
                        .relationType(relationship.getRelationType().toString())
                        .isPrimary(relationship.getIsPrimary())
                        .notificationEnabled(notificationEnabled)
                        .build();
                
                parentContacts.add(parentContact);
            }
            
            if (!parentContacts.isEmpty()) {
                ZaloNotificationDto.StudentAttendanceNotification studentNotification = 
                        ZaloNotificationDto.StudentAttendanceNotification.builder()
                                .student(studentInfo)
                                .attendance(attendanceInfo)
                                .parents(parentContacts)
                                .build();
                
                studentNotifications.add(studentNotification);
            }
        }
        
        return ZaloNotificationDto.builder()
                .notificationType("ATTENDANCE_REPORT")
                .classroom(classroomInfo)
                .session(sessionInfo)
                .teacher(teacherInfo)
                .studentNotifications(studentNotifications)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    // @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean sendToN8nWebhook(ZaloNotificationDto notificationData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String jsonPayload = objectMapper.writeValueAsString(notificationData);
            log.debug("Sending payload to n8n webhook: {}", jsonPayload);
            
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    n8nWebhookUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent notification to n8n webhook. Response: {}", response.getBody());
                return true;
            } else {
                log.error("Failed to send notification to n8n webhook. Status: {}, Response: {}", 
                        response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing notification data: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error sending notification to n8n webhook: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isZaloNotificationEnabled() {
        return zaloNotificationEnabled;
    }
}
