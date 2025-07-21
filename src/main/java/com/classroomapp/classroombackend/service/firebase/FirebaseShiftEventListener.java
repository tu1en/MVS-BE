package com.classroomapp.classroombackend.service.firebase;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftAssignmentService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftScheduleService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftSwapService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event Listener cho Shift Management events
 * Xử lý các events và trigger Firebase sync + notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseShiftEventListener {

    private final FirebaseShiftService firebaseShiftService;
    private final ShiftNotificationService shiftNotificationService;

    /**
     * Event: Shift Assignment được tạo
     */
    @EventListener
    @Async
    public void handleShiftAssignmentCreated(ShiftAssignmentCreatedEvent event) {
        log.info("Handling ShiftAssignmentCreated event for assignment ID: {}", event.getAssignment().getId());

        try {
            ShiftAssignment assignment = event.getAssignment();

            // Sync to Firebase
            firebaseShiftService.syncShiftAssignment(assignment)
                .thenRun(() -> log.debug("Successfully synced new assignment to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing new assignment to Firebase: {}", throwable.getMessage());
                    return null;
                });

            // Send notification to employee
            shiftNotificationService.sendShiftAssignedNotification(assignment)
                .thenRun(() -> log.debug("Successfully sent assignment notification"))
                .exceptionally(throwable -> {
                    log.error("Error sending assignment notification: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling ShiftAssignmentCreated event: {}", e.getMessage());
        }
    }

    /**
     * Event: Shift Assignment được cập nhật
     */
    @EventListener
    @Async
    public void handleShiftAssignmentUpdated(ShiftAssignmentUpdatedEvent event) {
        log.info("Handling ShiftAssignmentUpdated event for assignment ID: {}", event.getAssignment().getId());

        try {
            ShiftAssignment assignment = event.getAssignment();

            // Sync to Firebase
            firebaseShiftService.syncShiftAssignment(assignment)
                .thenRun(() -> log.debug("Successfully synced updated assignment to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing updated assignment to Firebase: {}", throwable.getMessage());
                    return null;
                });

            // Send notification if significant changes
            if (event.isSignificantChange()) {
                shiftNotificationService.sendShiftUpdatedNotification(assignment)
                    .thenRun(() -> log.debug("Successfully sent assignment update notification"))
                    .exceptionally(throwable -> {
                        log.error("Error sending assignment update notification: {}", throwable.getMessage());
                        return null;
                    });
            }

        } catch (Exception e) {
            log.error("Error handling ShiftAssignmentUpdated event: {}", e.getMessage());
        }
    }

    /**
     * Event: Shift Assignment bị hủy
     */
    @EventListener
    @Async
    public void handleShiftAssignmentCancelled(ShiftAssignmentCancelledEvent event) {
        log.info("Handling ShiftAssignmentCancelled event for assignment ID: {}", event.getAssignment().getId());

        try {
            ShiftAssignment assignment = event.getAssignment();
            String reason = event.getReason();

            // Sync to Firebase
            firebaseShiftService.syncShiftAssignment(assignment)
                .thenRun(() -> log.debug("Successfully synced cancelled assignment to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing cancelled assignment to Firebase: {}", throwable.getMessage());
                    return null;
                });

            // Send cancellation notification
            shiftNotificationService.sendShiftCancelledNotification(assignment, reason)
                .thenRun(() -> log.debug("Successfully sent assignment cancellation notification"))
                .exceptionally(throwable -> {
                    log.error("Error sending assignment cancellation notification: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling ShiftAssignmentCancelled event: {}", e.getMessage());
        }
    }

    /**
     * Event: Employee check-in
     */
    @EventListener
    @Async
    public void handleEmployeeCheckedIn(EmployeeCheckedInEvent event) {
        log.info("Handling EmployeeCheckedIn event for assignment ID: {}", event.getAssignment().getId());

        try {
            ShiftAssignment assignment = event.getAssignment();

            // Sync to Firebase
            firebaseShiftService.syncShiftAssignment(assignment)
                .thenRun(() -> log.debug("Successfully synced check-in to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing check-in to Firebase: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling EmployeeCheckedIn event: {}", e.getMessage());
        }
    }

    /**
     * Event: Employee check-out
     */
    @EventListener
    @Async
    public void handleEmployeeCheckedOut(EmployeeCheckedOutEvent event) {
        log.info("Handling EmployeeCheckedOut event for assignment ID: {}", event.getAssignment().getId());

        try {
            ShiftAssignment assignment = event.getAssignment();

            // Sync to Firebase
            firebaseShiftService.syncShiftAssignment(assignment)
                .thenRun(() -> log.debug("Successfully synced check-out to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing check-out to Firebase: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling EmployeeCheckedOut event: {}", e.getMessage());
        }
    }

    /**
     * Event: Swap Request được tạo
     */
    @EventListener
    @Async
    public void handleSwapRequestCreated(SwapRequestCreatedEvent event) {
        log.info("Handling SwapRequestCreated event for request ID: {}", event.getSwapRequest().getId());

        try {
            ShiftSwapRequest swapRequest = event.getSwapRequest();

            // Sync to Firebase
            firebaseShiftService.syncShiftSwapRequest(swapRequest)
                .thenRun(() -> log.debug("Successfully synced new swap request to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing new swap request to Firebase: {}", throwable.getMessage());
                    return null;
                });

            // Send notification to target employee
            shiftNotificationService.sendSwapRequestReceivedNotification(swapRequest)
                .thenRun(() -> log.debug("Successfully sent swap request notification"))
                .exceptionally(throwable -> {
                    log.error("Error sending swap request notification: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling SwapRequestCreated event: {}", e.getMessage());
        }
    }

    /**
     * Event: Swap Request được phê duyệt
     */
    @EventListener
    @Async
    public void handleSwapRequestApproved(SwapRequestApprovedEvent event) {
        log.info("Handling SwapRequestApproved event for request ID: {}", event.getSwapRequest().getId());

        try {
            ShiftSwapRequest swapRequest = event.getSwapRequest();

            // Sync to Firebase
            firebaseShiftService.syncShiftSwapRequest(swapRequest)
                .thenRun(() -> log.debug("Successfully synced approved swap request to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing approved swap request to Firebase: {}", throwable.getMessage());
                    return null;
                });

            // Send approval notification
            shiftNotificationService.sendSwapRequestApprovedNotification(swapRequest)
                .thenRun(() -> log.debug("Successfully sent swap request approval notification"))
                .exceptionally(throwable -> {
                    log.error("Error sending swap request approval notification: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling SwapRequestApproved event: {}", e.getMessage());
        }
    }

    /**
     * Event: Swap Request bị từ chối
     */
    @EventListener
    @Async
    public void handleSwapRequestRejected(SwapRequestRejectedEvent event) {
        log.info("Handling SwapRequestRejected event for request ID: {}", event.getSwapRequest().getId());

        try {
            ShiftSwapRequest swapRequest = event.getSwapRequest();
            String reason = event.getReason();

            // Sync to Firebase
            firebaseShiftService.syncShiftSwapRequest(swapRequest)
                .thenRun(() -> log.debug("Successfully synced rejected swap request to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing rejected swap request to Firebase: {}", throwable.getMessage());
                    return null;
                });

            // Send rejection notification
            shiftNotificationService.sendSwapRequestRejectedNotification(swapRequest, reason)
                .thenRun(() -> log.debug("Successfully sent swap request rejection notification"))
                .exceptionally(throwable -> {
                    log.error("Error sending swap request rejection notification: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling SwapRequestRejected event: {}", e.getMessage());
        }
    }

    /**
     * Event: Schedule được publish
     */
    @EventListener
    @Async
    public void handleSchedulePublished(SchedulePublishedEvent event) {
        log.info("Handling SchedulePublished event for schedule ID: {}", event.getSchedule().getId());

        try {
            ShiftSchedule schedule = event.getSchedule();

            // Sync to Firebase
            firebaseShiftService.syncShiftSchedule(schedule)
                .thenRun(() -> log.debug("Successfully synced published schedule to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing published schedule to Firebase: {}", throwable.getMessage());
                    return null;
                });

            // Send notification to affected employees
            if (event.getAffectedEmployees() != null && !event.getAffectedEmployees().isEmpty()) {
                shiftNotificationService.sendSchedulePublishedNotification(schedule, event.getAffectedEmployees())
                    .thenRun(() -> log.debug("Successfully sent schedule published notification"))
                    .exceptionally(throwable -> {
                        log.error("Error sending schedule published notification: {}", throwable.getMessage());
                        return null;
                    });
            }

        } catch (Exception e) {
            log.error("Error handling SchedulePublished event: {}", e.getMessage());
        }
    }

    /**
     * Event: Template được cập nhật
     */
    @EventListener
    @Async
    public void handleShiftTemplateUpdated(ShiftTemplateUpdatedEvent event) {
        log.info("Handling ShiftTemplateUpdated event for template ID: {}", event.getTemplate().getId());

        try {
            ShiftTemplate template = event.getTemplate();

            // Sync to Firebase
            firebaseShiftService.syncShiftTemplate(template)
                .thenRun(() -> log.debug("Successfully synced updated template to Firebase"))
                .exceptionally(throwable -> {
                    log.error("Error syncing updated template to Firebase: {}", throwable.getMessage());
                    return null;
                });

        } catch (Exception e) {
            log.error("Error handling ShiftTemplateUpdated event: {}", e.getMessage());
        }
    }

    // Event classes (inner classes for simplicity)
    
    public static class ShiftAssignmentCreatedEvent {
        private final ShiftAssignment assignment;
        
        public ShiftAssignmentCreatedEvent(ShiftAssignment assignment) {
            this.assignment = assignment;
        }
        
        public ShiftAssignment getAssignment() { return assignment; }
    }

    public static class ShiftAssignmentUpdatedEvent {
        private final ShiftAssignment assignment;
        private final boolean significantChange;
        
        public ShiftAssignmentUpdatedEvent(ShiftAssignment assignment, boolean significantChange) {
            this.assignment = assignment;
            this.significantChange = significantChange;
        }
        
        public ShiftAssignment getAssignment() { return assignment; }
        public boolean isSignificantChange() { return significantChange; }
    }

    public static class ShiftAssignmentCancelledEvent {
        private final ShiftAssignment assignment;
        private final String reason;
        
        public ShiftAssignmentCancelledEvent(ShiftAssignment assignment, String reason) {
            this.assignment = assignment;
            this.reason = reason;
        }
        
        public ShiftAssignment getAssignment() { return assignment; }
        public String getReason() { return reason; }
    }

    public static class EmployeeCheckedInEvent {
        private final ShiftAssignment assignment;
        
        public EmployeeCheckedInEvent(ShiftAssignment assignment) {
            this.assignment = assignment;
        }
        
        public ShiftAssignment getAssignment() { return assignment; }
    }

    public static class EmployeeCheckedOutEvent {
        private final ShiftAssignment assignment;
        
        public EmployeeCheckedOutEvent(ShiftAssignment assignment) {
            this.assignment = assignment;
        }
        
        public ShiftAssignment getAssignment() { return assignment; }
    }

    public static class SwapRequestCreatedEvent {
        private final ShiftSwapRequest swapRequest;
        
        public SwapRequestCreatedEvent(ShiftSwapRequest swapRequest) {
            this.swapRequest = swapRequest;
        }
        
        public ShiftSwapRequest getSwapRequest() { return swapRequest; }
    }

    public static class SwapRequestApprovedEvent {
        private final ShiftSwapRequest swapRequest;
        
        public SwapRequestApprovedEvent(ShiftSwapRequest swapRequest) {
            this.swapRequest = swapRequest;
        }
        
        public ShiftSwapRequest getSwapRequest() { return swapRequest; }
    }

    public static class SwapRequestRejectedEvent {
        private final ShiftSwapRequest swapRequest;
        private final String reason;
        
        public SwapRequestRejectedEvent(ShiftSwapRequest swapRequest, String reason) {
            this.swapRequest = swapRequest;
            this.reason = reason;
        }
        
        public ShiftSwapRequest getSwapRequest() { return swapRequest; }
        public String getReason() { return reason; }
    }

    public static class SchedulePublishedEvent {
        private final ShiftSchedule schedule;
        private final java.util.List<com.classroomapp.classroombackend.model.usermanagement.User> affectedEmployees;
        
        public SchedulePublishedEvent(ShiftSchedule schedule, java.util.List<com.classroomapp.classroombackend.model.usermanagement.User> affectedEmployees) {
            this.schedule = schedule;
            this.affectedEmployees = affectedEmployees;
        }
        
        public ShiftSchedule getSchedule() { return schedule; }
        public java.util.List<com.classroomapp.classroombackend.model.usermanagement.User> getAffectedEmployees() { return affectedEmployees; }
    }

    public static class ShiftTemplateUpdatedEvent {
        private final ShiftTemplate template;
        
        public ShiftTemplateUpdatedEvent(ShiftTemplate template) {
            this.template = template;
        }
        
        public ShiftTemplate getTemplate() { return template; }
    }
}
