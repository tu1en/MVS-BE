package com.classroomapp.classroombackend.service.firebase;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import lombok.extern.slf4j.Slf4j;

/**
 * Service cho Firebase real-time sync của Shift Management data
 * Đồng bộ hóa shift assignments, schedules, swap requests với Firebase Realtime Database
 */
@Service
@Slf4j
@ConditionalOnBean(name = "classroomFirebaseDatabase")
public class FirebaseShiftService {

    private final FirebaseDatabase firebaseDatabase;
    private final ObjectMapper objectMapper;

    public FirebaseShiftService(@Qualifier("classroomFirebaseDatabase") FirebaseDatabase firebaseDatabase, ObjectMapper objectMapper) {
        this.firebaseDatabase = firebaseDatabase;
        this.objectMapper = objectMapper;
        
        // Check if FirebaseDatabase is available
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase is null. Firebase sync features will be disabled.");
        } else {
            log.info("FirebaseShiftService initialized successfully with FirebaseDatabase");
        }
    }

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
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping sync for assignment ID: {}", assignment.getId());
            return CompletableFuture.completedFuture(null);
        }
        
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
            log.error("Lỗi đồng bộ assignment ID: {} lên Firebase: {}", assignment.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Sync shift schedule to Firebase
     */
    public CompletableFuture<Void> syncShiftSchedule(ShiftSchedule schedule) {
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping sync for schedule ID: {}", schedule.getId());
            return CompletableFuture.completedFuture(null);
        }
        
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
            log.error("Lỗi đồng bộ schedule ID: {} lên Firebase: {}", schedule.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Sync shift swap request to Firebase
     */
    public CompletableFuture<Void> syncShiftSwapRequest(ShiftSwapRequest swapRequest) {
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping sync for swap request ID: {}", swapRequest.getId());
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Syncing shift swap request ID: {} to Firebase", swapRequest.getId());
        
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
            log.error("Lỗi đồng bộ swap request ID: {} lên Firebase: {}", swapRequest.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Sync shift template to Firebase
     */
    public CompletableFuture<Void> syncShiftTemplate(ShiftTemplate template) {
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping sync for template ID: {}", template.getId());
            return CompletableFuture.completedFuture(null);
        }
        
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
            log.error("Lỗi đồng bộ template ID: {} lên Firebase: {}", template.getId(), e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Send shift notification to Firebase
     */
    public CompletableFuture<Void> sendShiftNotification(Long recipientId, String title, String message, 
                                                         String type, Map<String, Object> data) {
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping notification for user: {}", recipientId);
            return CompletableFuture.completedFuture(null);
        }
        
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
            log.error("Lỗi gửi thông báo tới người dùng: {}: {}", recipientId, e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Remove shift assignment from Firebase
     */
    public CompletableFuture<Void> removeShiftAssignment(Long assignmentId) {
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping removal for assignment ID: {}", assignmentId);
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Removing shift assignment ID: {} from Firebase", assignmentId);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            DatabaseReference assignmentRef = firebaseDatabase.getReference(SHIFT_ASSIGNMENTS_PATH)
                .child(assignmentId.toString());
            
            assignmentRef.removeValueAsync().addListener(() -> {
                log.debug("Successfully removed assignment ID: {} from Firebase", assignmentId);
                
                // Also remove from employee-specific path
                removeFromEmployeeShifts(assignmentId);
                
                future.complete(null);
            }, Runnable::run);
            
        } catch (Exception e) {
            log.error("Lỗi xóa assignment ID: {} khỏi Firebase: {}", assignmentId, e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Get shift assignments for an employee from Firebase
     */
    public CompletableFuture<List<ShiftAssignment>> getEmployeeShifts(Long employeeId, String startDate, String endDate) {
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping getEmployeeShifts for employee: {}", employeeId);
            return CompletableFuture.completedFuture(List.of());
        }
        
        log.info("Getting shifts for employee: {} from {} to {}", employeeId, startDate, endDate);
        
        CompletableFuture<List<ShiftAssignment>> future = new CompletableFuture<>();
        
        try {
            DatabaseReference employeeShiftsRef = firebaseDatabase.getReference(EMPLOYEE_SHIFTS_PATH)
                .child(employeeId.toString());
            
            // Query by date range
            employeeShiftsRef.orderByChild("assignmentDate")
                .startAt(startDate)
                .endAt(endDate)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        try {
                            List<ShiftAssignment> assignments = convertFirebaseDataToAssignments(dataSnapshot);
                            log.debug("Successfully retrieved {} shifts for employee: {}", assignments.size(), employeeId);
                            future.complete(assignments);
                        } catch (Exception e) {
                            log.error("Error processing Firebase data for employee: {}", employeeId, e);
                            future.completeExceptionally(e);
                        }
                    }
                    
                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        log.error("Failed to get shifts for employee: {}", employeeId);
                        future.completeExceptionally(new RuntimeException("Firebase query cancelled: " + databaseError.getMessage()));
                    }
                });
            
        } catch (Exception e) {
            log.error("Lỗi lấy shifts cho employee: {}: {}", employeeId, e.getMessage());
            future.completeExceptionally(e);
        }
        
        return future;
    }

    // Helper methods for data conversion
    private Map<String, Object> convertAssignmentToFirebaseData(ShiftAssignment assignment) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", assignment.getId());
        data.put("assignedUserId", assignment.getAssignedUser().getId());
        data.put("shiftTemplateId", assignment.getShiftTemplate().getId());
        data.put("assignmentDate", assignment.getAssignmentDate().toString());
        data.put("plannedStartTime", assignment.getPlannedStartTime().toString());
        data.put("plannedEndTime", assignment.getPlannedEndTime().toString());
        data.put("status", assignment.getStatus().toString());
        data.put("attendanceStatus", assignment.getAttendanceStatus().toString());
        data.put("createdAt", assignment.getCreatedAt().toString());
        data.put("updatedAt", assignment.getUpdatedAt().toString());
        return data;
    }

    private Map<String, Object> convertScheduleToFirebaseData(ShiftSchedule schedule) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", schedule.getId());
        data.put("scheduleName", schedule.getScheduleName());
        data.put("startDate", schedule.getStartDate().toString());
        data.put("endDate", schedule.getEndDate().toString());
        data.put("scheduleType", schedule.getScheduleType().toString());
        data.put("status", schedule.getStatus().toString());
        data.put("createdBy", schedule.getCreatedBy().getId());
        data.put("createdAt", schedule.getCreatedAt().toString());
        return data;
    }

    private Map<String, Object> convertSwapRequestToFirebaseData(ShiftSwapRequest swapRequest) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", swapRequest.getId());
        data.put("requesterId", swapRequest.getRequester().getId());
        data.put("targetUserId", swapRequest.getTargetEmployee().getId());
        data.put("requesterAssignmentId", swapRequest.getRequesterAssignment().getId());
        data.put("targetAssignmentId", swapRequest.getTargetAssignment().getId());
        data.put("status", swapRequest.getStatus().toString());
        data.put("createdAt", swapRequest.getCreatedAt().toString());
        return data;
    }

    private Map<String, Object> convertTemplateToFirebaseData(ShiftTemplate template) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", template.getId());
        data.put("templateName", template.getTemplateName());
        data.put("startTime", template.getStartTime().toString());
        data.put("endTime", template.getEndTime().toString());
        data.put("totalHours", template.getTotalHours().toString());
        data.put("isActive", template.getIsActive());
        return data;
    }

    private List<ShiftAssignment> convertFirebaseDataToAssignments(com.google.firebase.database.DataSnapshot snapshot) {
        // Implementation for converting Firebase data back to ShiftAssignment objects
        // This would depend on your specific data structure
        return List.of(); // Placeholder
    }

    private void syncToEmployeeShifts(ShiftAssignment assignment) {
        // Sync assignment to employee-specific path for easier querying
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping syncToEmployeeShifts for assignment ID: {}", assignment.getId());
            return;
        }
        
        try {
            Map<String, Object> employeeShiftData = new HashMap<>();
            employeeShiftData.put("assignmentId", assignment.getId());
            employeeShiftData.put("date", assignment.getAssignmentDate().toString());
            employeeShiftData.put("startTime", assignment.getPlannedStartTime().toString());
            employeeShiftData.put("endTime", assignment.getPlannedEndTime().toString());
            employeeShiftData.put("status", assignment.getStatus().toString());
            employeeShiftData.put("templateName", assignment.getShiftTemplate().getTemplateName());
            
            DatabaseReference employeeShiftRef = firebaseDatabase.getReference(EMPLOYEE_SHIFTS_PATH)
                .child(assignment.getAssignedUser().getId().toString())
                .child(assignment.getAssignmentDate().toString())
                .child(assignment.getId().toString());
            
            employeeShiftRef.setValueAsync(employeeShiftData);
            
        } catch (Exception e) {
            log.error("Lỗi đồng bộ tới employee shifts: {}", e.getMessage());
        }
    }

    private void removeFromEmployeeShifts(Long assignmentId) {
        // Remove assignment from employee-specific path
        if (firebaseDatabase == null) {
            log.warn("FirebaseDatabase not available, skipping removeFromEmployeeShifts for assignment ID: {}", assignmentId);
            return;
        }
        
        try {
            // This would need to find the employee ID first
            // For now, we'll just log the action
            log.debug("Removing assignment ID: {} from employee shifts", assignmentId);
            
        } catch (Exception e) {
            log.error("Lỗi xóa assignment khỏi employee shifts: {}", e.getMessage());
        }
    }
}
