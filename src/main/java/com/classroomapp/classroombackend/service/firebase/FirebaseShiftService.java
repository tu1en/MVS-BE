package com.classroomapp.classroombackend.service.firebase;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service cho Firebase real-time sync của Shift Management data
 * Đồng bộ hóa shift assignments, schedules, swap requests với Firebase Realtime Database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseShiftService {

    private final FirebaseDatabase firebaseDatabase;
    private final ObjectMapper objectMapper;

    // Firebase paths
    private static final String SHIFT_ASSIGNMENTS_PATH = "shift-assignments";
    private static final String SHIFT_SCHEDULES_PATH = "shift-schedules";
    private static final String SHIFT_SWAP_REQUESTS_PATH = "shift-swap-requests";
    private static final String SHIFT_TEMPLATES_PATH = "shift-templates";
    private static final String SHIFT_NOTIFICATIONS_PATH = "shift-notifications";
    private static final String EMPLOYEE_SHIFTS_PATH = "employee-shifts";

    /**
     * Sync shift assignment to Firebase
     */
    public CompletableFuture<Void> syncShiftAssignment(ShiftAssignment assignment) {
        log.info("Syncing shift assignment ID: {} to Firebase", assignment.getId());
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> assignmentData = convertAssignmentToFirebaseData(assignment);
            
            DatabaseReference assignmentRef = firebaseDatabase.getReference(SHIFT_ASSIGNMENTS_PATH)
                .child(assignment.getId().toString());
            
            assignmentRef.setValueAsync(assignmentData).addListener(() -> {
                log.debug("Successfully synced assignment ID: {} to Firebase", assignment.getId());
                
                // Also sync to employee-specific path for easier querying
                syncToEmployeeShifts(assignment);
                
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Error syncing assignment ID: {} to Firebase: {}", assignment.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Sync shift schedule to Firebase
     */
    public CompletableFuture<Void> syncShiftSchedule(ShiftSchedule schedule) {
        log.info("Syncing shift schedule ID: {} to Firebase", schedule.getId());
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> scheduleData = convertScheduleToFirebaseData(schedule);
            
            DatabaseReference scheduleRef = firebaseDatabase.getReference(SHIFT_SCHEDULES_PATH)
                .child(schedule.getId().toString());
            
            scheduleRef.setValueAsync(scheduleData).addListener(() -> {
                log.debug("Successfully synced schedule ID: {} to Firebase", schedule.getId());
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Error syncing schedule ID: {} to Firebase: {}", schedule.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Sync shift swap request to Firebase
     */
    public CompletableFuture<Void> syncShiftSwapRequest(ShiftSwapRequest swapRequest) {
        log.info("Syncing swap request ID: {} to Firebase", swapRequest.getId());
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> swapData = convertSwapRequestToFirebaseData(swapRequest);
            
            DatabaseReference swapRef = firebaseDatabase.getReference(SHIFT_SWAP_REQUESTS_PATH)
                .child(swapRequest.getId().toString());
            
            swapRef.setValueAsync(swapData).addListener(() -> {
                log.debug("Successfully synced swap request ID: {} to Firebase", swapRequest.getId());
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Error syncing swap request ID: {} to Firebase: {}", swapRequest.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Sync shift template to Firebase
     */
    public CompletableFuture<Void> syncShiftTemplate(ShiftTemplate template) {
        log.info("Syncing shift template ID: {} to Firebase", template.getId());
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> templateData = convertTemplateToFirebaseData(template);
            
            DatabaseReference templateRef = firebaseDatabase.getReference(SHIFT_TEMPLATES_PATH)
                .child(template.getId().toString());
            
            templateRef.setValueAsync(templateData).addListener(() -> {
                log.debug("Successfully synced template ID: {} to Firebase", template.getId());
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Error syncing template ID: {} to Firebase: {}", template.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Send shift notification to Firebase
     */
    public CompletableFuture<Void> sendShiftNotification(Long recipientId, String title, String message, 
                                                         String type, Map<String, Object> data) {
        log.info("Sending shift notification to user: {} with type: {}", recipientId, type);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("type", type);
            notification.put("data", data);
            notification.put("timestamp", System.currentTimeMillis());
            notification.put("read", false);
            
            DatabaseReference notificationRef = firebaseDatabase.getReference(SHIFT_NOTIFICATIONS_PATH)
                .child(recipientId.toString())
                .push();
            
            notificationRef.setValueAsync(notification).addListener(() -> {
                log.debug("Successfully sent notification to user: {}", recipientId);
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Error sending notification to user: {}: {}", recipientId, e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Remove shift assignment from Firebase
     */
    public CompletableFuture<Void> removeShiftAssignment(Long assignmentId) {
        log.info("Removing shift assignment ID: {} from Firebase", assignmentId);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            DatabaseReference assignmentRef = firebaseDatabase.getReference(SHIFT_ASSIGNMENTS_PATH)
                .child(assignmentId.toString());
            
            assignmentRef.removeValueAsync().addListener(() -> {
                log.debug("Successfully removed assignment ID: {} from Firebase", assignmentId);
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Error removing assignment ID: {} from Firebase: {}", assignmentId, e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Sync employee's shifts for easier mobile app querying
     */
    private void syncToEmployeeShifts(ShiftAssignment assignment) {
        try {
            Map<String, Object> employeeShiftData = new HashMap<>();
            employeeShiftData.put("assignmentId", assignment.getId());
            employeeShiftData.put("date", assignment.getAssignmentDate().toString());
            employeeShiftData.put("startTime", assignment.getPlannedStartTime().toString());
            employeeShiftData.put("endTime", assignment.getPlannedEndTime().toString());
            employeeShiftData.put("status", assignment.getStatus().toString());
            employeeShiftData.put("templateName", assignment.getShiftTemplate().getTemplateName());
            employeeShiftData.put("templateColor", assignment.getShiftTemplate().getColorCode());
            
            DatabaseReference employeeShiftRef = firebaseDatabase.getReference(EMPLOYEE_SHIFTS_PATH)
                .child(assignment.getEmployee().getId().toString())
                .child(assignment.getAssignmentDate().toString())
                .child(assignment.getId().toString());
            
            employeeShiftRef.setValueAsync(employeeShiftData);
            
        } catch (Exception e) {
            log.error("Error syncing to employee shifts: {}", e.getMessage());
        }
    }

    /**
     * Convert ShiftAssignment to Firebase-compatible data
     */
    private Map<String, Object> convertAssignmentToFirebaseData(ShiftAssignment assignment) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", assignment.getId());
        data.put("employeeId", assignment.getEmployee().getId());
        data.put("employeeName", assignment.getEmployee().getFullName());
        data.put("shiftTemplateId", assignment.getShiftTemplate().getId());
        data.put("templateName", assignment.getShiftTemplate().getTemplateName());
        data.put("templateColor", assignment.getShiftTemplate().getColorCode());
        data.put("assignmentDate", assignment.getAssignmentDate().toString());
        data.put("plannedStartTime", assignment.getPlannedStartTime().toString());
        data.put("plannedEndTime", assignment.getPlannedEndTime().toString());
        data.put("plannedHours", assignment.getPlannedHours().toString());
        data.put("status", assignment.getStatus().toString());
        data.put("attendanceStatus", assignment.getAttendanceStatus().toString());
        
        if (assignment.getActualStartTime() != null) {
            data.put("actualStartTime", assignment.getActualStartTime().toString());
        }
        if (assignment.getActualEndTime() != null) {
            data.put("actualEndTime", assignment.getActualEndTime().toString());
        }
        if (assignment.getActualHours() != null) {
            data.put("actualHours", assignment.getActualHours().toString());
        }
        if (assignment.getOvertimeHours() != null) {
            data.put("overtimeHours", assignment.getOvertimeHours().toString());
        }
        if (assignment.getNotes() != null) {
            data.put("notes", assignment.getNotes());
        }
        
        data.put("updatedAt", System.currentTimeMillis());
        
        return data;
    }

    /**
     * Convert ShiftSchedule to Firebase-compatible data
     */
    private Map<String, Object> convertScheduleToFirebaseData(ShiftSchedule schedule) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", schedule.getId());
        data.put("scheduleName", schedule.getScheduleName());
        data.put("description", schedule.getDescription());
        data.put("startDate", schedule.getStartDate().toString());
        data.put("endDate", schedule.getEndDate().toString());
        data.put("scheduleType", schedule.getScheduleType().toString());
        data.put("status", schedule.getStatus().toString());
        data.put("totalAssignments", schedule.getTotalAssignments());
        data.put("createdById", schedule.getCreatedBy().getId());
        data.put("createdByName", schedule.getCreatedBy().getFullName());
        
        if (schedule.getPublishedBy() != null) {
            data.put("publishedById", schedule.getPublishedBy().getId());
            data.put("publishedByName", schedule.getPublishedBy().getFullName());
        }
        if (schedule.getPublishedAt() != null) {
            data.put("publishedAt", schedule.getPublishedAt().toString());
        }
        
        data.put("updatedAt", System.currentTimeMillis());
        
        return data;
    }

    /**
     * Convert ShiftSwapRequest to Firebase-compatible data
     */
    private Map<String, Object> convertSwapRequestToFirebaseData(ShiftSwapRequest swapRequest) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", swapRequest.getId());
        data.put("requesterId", swapRequest.getRequester().getId());
        data.put("requesterName", swapRequest.getRequester().getFullName());
        data.put("targetEmployeeId", swapRequest.getTargetEmployee().getId());
        data.put("targetEmployeeName", swapRequest.getTargetEmployee().getFullName());
        data.put("requesterAssignmentId", swapRequest.getRequesterAssignment().getId());
        data.put("targetAssignmentId", swapRequest.getTargetAssignment().getId());
        data.put("requestReason", swapRequest.getRequestReason());
        data.put("requestType", swapRequest.getRequestType().toString());
        data.put("status", swapRequest.getStatus().toString());
        data.put("priority", swapRequest.getPriority().toString());
        data.put("isEmergency", swapRequest.getIsEmergency());
        
        if (swapRequest.getTargetResponse() != null) {
            data.put("targetResponse", swapRequest.getTargetResponse().toString());
            data.put("targetResponseReason", swapRequest.getTargetResponseReason());
            data.put("targetRespondedAt", swapRequest.getTargetRespondedAt().toString());
        }
        
        if (swapRequest.getManagerResponse() != null) {
            data.put("managerResponse", swapRequest.getManagerResponse().toString());
            data.put("managerResponseReason", swapRequest.getManagerResponseReason());
        }
        
        if (swapRequest.getApprovedBy() != null) {
            data.put("approvedById", swapRequest.getApprovedBy().getId());
            data.put("approvedByName", swapRequest.getApprovedBy().getFullName());
            data.put("approvedAt", swapRequest.getApprovedAt().toString());
        }
        
        if (swapRequest.getExpiresAt() != null) {
            data.put("expiresAt", swapRequest.getExpiresAt().toString());
        }
        
        data.put("createdAt", swapRequest.getCreatedAt().toString());
        data.put("updatedAt", System.currentTimeMillis());
        
        return data;
    }

    /**
     * Convert ShiftTemplate to Firebase-compatible data
     */
    private Map<String, Object> convertTemplateToFirebaseData(ShiftTemplate template) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", template.getId());
        data.put("templateName", template.getTemplateName());
        data.put("templateCode", template.getTemplateCode());
        data.put("description", template.getDescription());
        data.put("startTime", template.getStartTime().toString());
        data.put("endTime", template.getEndTime().toString());
        data.put("totalHours", template.getTotalHours().toString());
        data.put("isActive", template.getIsActive());
        data.put("isOvertimeEligible", template.getIsOvertimeEligible());
        data.put("colorCode", template.getColorCode());
        data.put("sortOrder", template.getSortOrder());
        
        if (template.getBreakStartTime() != null) {
            data.put("breakStartTime", template.getBreakStartTime().toString());
        }
        if (template.getBreakEndTime() != null) {
            data.put("breakEndTime", template.getBreakEndTime().toString());
        }
        if (template.getBreakDurationMinutes() != null) {
            data.put("breakDurationMinutes", template.getBreakDurationMinutes());
        }
        
        data.put("updatedAt", System.currentTimeMillis());
        
        return data;
    }

    /**
     * Bulk sync multiple assignments
     */
    public CompletableFuture<Void> bulkSyncAssignments(List<ShiftAssignment> assignments) {
        log.info("Bulk syncing {} assignments to Firebase", assignments.size());
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> updates = new HashMap<>();
            
            for (ShiftAssignment assignment : assignments) {
                Map<String, Object> assignmentData = convertAssignmentToFirebaseData(assignment);
                updates.put(SHIFT_ASSIGNMENTS_PATH + "/" + assignment.getId(), assignmentData);
                
                // Also add to employee-specific path
                Map<String, Object> employeeShiftData = new HashMap<>();
                employeeShiftData.put("assignmentId", assignment.getId());
                employeeShiftData.put("date", assignment.getAssignmentDate().toString());
                employeeShiftData.put("startTime", assignment.getPlannedStartTime().toString());
                employeeShiftData.put("endTime", assignment.getPlannedEndTime().toString());
                employeeShiftData.put("status", assignment.getStatus().toString());
                employeeShiftData.put("templateName", assignment.getShiftTemplate().getTemplateName());
                employeeShiftData.put("templateColor", assignment.getShiftTemplate().getColorCode());
                
                updates.put(EMPLOYEE_SHIFTS_PATH + "/" + assignment.getEmployee().getId() + "/" + 
                           assignment.getAssignmentDate().toString() + "/" + assignment.getId(), employeeShiftData);
            }
            
            firebaseDatabase.getReference().updateChildrenAsync(updates).addListener(() -> {
                log.debug("Successfully bulk synced {} assignments to Firebase", assignments.size());
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Error bulk syncing assignments to Firebase: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }
}
