package com.classroomapp.classroombackend.service.firebase;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service cho Push Notifications cá»§a Shift Management
 * Gá»­i notifications cho shift assignments, swap requests, schedule changes
 */
@Service
@Slf4j
@RequiredArgsConstructor

public class ShiftNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final FirebaseShiftService firebaseShiftService;

    // Notification types
    public enum NotificationType {
        SHIFT_ASSIGNED("shift_assigned", "Ca lÃ m viá»‡c má»›i", "ðŸ•"),
        SHIFT_UPDATED("shift_updated", "Ca lÃ m viá»‡c cáº­p nháº­t", "ðŸ“"),
        SHIFT_CANCELLED("shift_cancelled", "Ca lÃ m viá»‡c bá»‹ há»§y", "âŒ"),
        SHIFT_REMINDER("shift_reminder", "Nháº¯c nhá»Ÿ ca lÃ m viá»‡c", "â°"),
        CHECK_IN_REMINDER("check_in_reminder", "Nháº¯c nhá»Ÿ check-in", "ðŸ“"),
        CHECK_OUT_REMINDER("check_out_reminder", "Nháº¯c nhá»Ÿ check-out", "ðŸ"),
        SWAP_REQUEST_RECEIVED("swap_request_received", "YÃªu cáº§u Ä‘á»•i ca", "ðŸ”„"),
        SWAP_REQUEST_APPROVED("swap_request_approved", "Äá»•i ca Ä‘Æ°á»£c phÃª duyá»‡t", "âœ…"),
        SWAP_REQUEST_REJECTED("swap_request_rejected", "Äá»•i ca bá»‹ tá»« chá»‘i", "âŒ"),
        SCHEDULE_PUBLISHED("schedule_published", "Lá»‹ch lÃ m viá»‡c má»›i", "ðŸ“…"),
        SCHEDULE_UPDATED("schedule_updated", "Lá»‹ch lÃ m viá»‡c cáº­p nháº­t", "ðŸ“");

        private final String code;
        private final String title;
        private final String icon;

        NotificationType(String code, String title, String icon) {
            this.code = code;
            this.title = title;
            this.icon = icon;
        }

        public String getCode() { return code; }
        public String getTitle() { return title; }
        public String getIcon() { return icon; }
    }

    /**
     * Gá»­i notification khi cÃ³ shift assignment má»›i
     */
    public CompletableFuture<Void> sendShiftAssignedNotification(ShiftAssignment assignment) {
        log.info("Sending shift assigned notification for assignment ID: {}", assignment.getId());

        String title = NotificationType.SHIFT_ASSIGNED.getTitle();
        String body = String.format("Báº¡n Ä‘Æ°á»£c phÃ¢n cÃ´ng ca %s vÃ o ngÃ y %s tá»« %s Ä‘áº¿n %s",
                                   assignment.getShiftTemplate().getTemplateName(),
                                   assignment.getAssignmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                   assignment.getPlannedStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                   assignment.getPlannedEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SHIFT_ASSIGNED.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());
        data.put("date", assignment.getAssignmentDate().toString());
        data.put("startTime", assignment.getPlannedStartTime().toString());
        data.put("endTime", assignment.getPlannedEndTime().toString());

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gá»­i notification khi shift bá»‹ cáº­p nháº­t
     */
    public CompletableFuture<Void> sendShiftUpdatedNotification(ShiftAssignment assignment) {
        log.info("Sending shift updated notification for assignment ID: {}", assignment.getId());

        String title = NotificationType.SHIFT_UPDATED.getTitle();
        String body = String.format("Ca lÃ m viá»‡c %s ngÃ y %s Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t",
                                   assignment.getShiftTemplate().getTemplateName(),
                                   assignment.getAssignmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SHIFT_UPDATED.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gá»­i notification khi shift bá»‹ há»§y
     */
    public CompletableFuture<Void> sendShiftCancelledNotification(ShiftAssignment assignment, String reason) {
        log.info("Sending shift cancelled notification for assignment ID: {}", assignment.getId());

        String title = NotificationType.SHIFT_CANCELLED.getTitle();
        String body = String.format("Ca lÃ m viá»‡c %s ngÃ y %s Ä‘Ã£ bá»‹ há»§y. LÃ½ do: %s",
                                   assignment.getShiftTemplate().getTemplateName(),
                                   assignment.getAssignmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                   reason);

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SHIFT_CANCELLED.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());
        data.put("reason", reason);

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gá»­i reminder notification trÆ°á»›c khi ca báº¯t Ä‘áº§u
     */
    public CompletableFuture<Void> sendShiftReminderNotification(ShiftAssignment assignment, int minutesBefore) {
        log.info("Sending shift reminder notification for assignment ID: {} ({} minutes before)", 
                assignment.getId(), minutesBefore);

        String title = NotificationType.SHIFT_REMINDER.getTitle();
        String body = String.format("Ca lÃ m viá»‡c %s sáº½ báº¯t Ä‘áº§u trong %d phÃºt (lÃºc %s)",
                                   assignment.getShiftTemplate().getTemplateName(),
                                   minutesBefore,
                                   assignment.getPlannedStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SHIFT_REMINDER.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());
        data.put("minutesBefore", String.valueOf(minutesBefore));

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gá»­i notification nháº¯c nhá»Ÿ check-in
     */
    public CompletableFuture<Void> sendCheckInReminderNotification(ShiftAssignment assignment) {
        log.info("Sending check-in reminder notification for assignment ID: {}", assignment.getId());

        String title = NotificationType.CHECK_IN_REMINDER.getTitle();
        String body = String.format("ÄÃ£ Ä‘áº¿n giá» check-in cho ca %s. Vui lÃ²ng check-in ngay!",
                                   assignment.getShiftTemplate().getTemplateName());

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.CHECK_IN_REMINDER.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());
        data.put("action", "check_in");

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gá»­i notification nháº¯c nhá»Ÿ check-out
     */
    public CompletableFuture<Void> sendCheckOutReminderNotification(ShiftAssignment assignment) {
        log.info("Sending check-out reminder notification for assignment ID: {}", assignment.getId());

        String title = NotificationType.CHECK_OUT_REMINDER.getTitle();
        String body = String.format("ÄÃ£ Ä‘áº¿n giá» check-out cho ca %s. Vui lÃ²ng check-out!",
                                   assignment.getShiftTemplate().getTemplateName());

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.CHECK_OUT_REMINDER.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());
        data.put("action", "check_out");

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gá»­i notification khi nháº­n Ä‘Æ°á»£c swap request
     */
    public CompletableFuture<Void> sendSwapRequestReceivedNotification(ShiftSwapRequest swapRequest) {
        log.info("Sending swap request received notification for request ID: {}", swapRequest.getId());

        String title = NotificationType.SWAP_REQUEST_RECEIVED.getTitle();
        String body = String.format("%s muá»‘n Ä‘á»•i ca vá»›i báº¡n. Ca %s ngÃ y %s",
                                   swapRequest.getRequester().getFullName(),
                                   swapRequest.getTargetAssignment().getShiftTemplate().getTemplateName(),
                                   swapRequest.getTargetAssignment().getAssignmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SWAP_REQUEST_RECEIVED.getCode());
        data.put("swapRequestId", swapRequest.getId().toString());
        data.put("requesterId", swapRequest.getRequester().getId().toString());
        data.put("requesterName", swapRequest.getRequester().getFullName());
        data.put("priority", swapRequest.getPriority().toString());
        data.put("isEmergency", swapRequest.getIsEmergency().toString());

        return sendNotificationToUser(swapRequest.getTargetEmployee(), title, body, data);
    }

    /**
     * Gá»­i notification khi swap request Ä‘Æ°á»£c phÃª duyá»‡t
     */
    public CompletableFuture<Void> sendSwapRequestApprovedNotification(ShiftSwapRequest swapRequest) {
        log.info("Sending swap request approved notification for request ID: {}", swapRequest.getId());

        String title = NotificationType.SWAP_REQUEST_APPROVED.getTitle();
        String body = String.format("YÃªu cáº§u Ä‘á»•i ca cá»§a báº¡n vá»›i %s Ä‘Ã£ Ä‘Æ°á»£c phÃª duyá»‡t",
                                   swapRequest.getTargetEmployee().getFullName());

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SWAP_REQUEST_APPROVED.getCode());
        data.put("swapRequestId", swapRequest.getId().toString());
        data.put("targetUserName", swapRequest.getTargetEmployee().getFullName());

        // Gá»­i cho cáº£ requester vÃ  target employee
        CompletableFuture<Void> requesterNotification = sendNotificationToUser(swapRequest.getRequester(), title, body, data);
        CompletableFuture<Void> targetNotification = sendNotificationToUser(swapRequest.getTargetEmployee(), title, body, data);

        return CompletableFuture.allOf(requesterNotification, targetNotification);
    }

    /**
     * Gá»­i notification khi swap request bá»‹ tá»« chá»‘i
     */
    public CompletableFuture<Void> sendSwapRequestRejectedNotification(ShiftSwapRequest swapRequest, String reason) {
        log.info("Sending swap request rejected notification for request ID: {}", swapRequest.getId());

        String title = NotificationType.SWAP_REQUEST_REJECTED.getTitle();
        String body = String.format("YÃªu cáº§u Ä‘á»•i ca cá»§a báº¡n vá»›i %s Ä‘Ã£ bá»‹ tá»« chá»‘i. LÃ½ do: %s",
                                   swapRequest.getTargetEmployee().getFullName(),
                                   reason);

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SWAP_REQUEST_REJECTED.getCode());
        data.put("swapRequestId", swapRequest.getId().toString());
        data.put("targetUserName", swapRequest.getTargetEmployee().getFullName());
        data.put("reason", reason);

        return sendNotificationToUser(swapRequest.getRequester(), title, body, data);
    }

    /**
     * Gá»­i notification khi schedule Ä‘Æ°á»£c publish
     */
    public CompletableFuture<Void> sendSchedulePublishedNotification(ShiftSchedule schedule, List<User> employees) {
        log.info("Sending schedule published notification for schedule ID: {} to {} employees", 
                schedule.getId(), employees.size());

        String title = NotificationType.SCHEDULE_PUBLISHED.getTitle();
        String body = String.format("Lá»‹ch lÃ m viá»‡c '%s' tá»« %s Ä‘áº¿n %s Ä‘Ã£ Ä‘Æ°á»£c xuáº¥t báº£n",
                                   schedule.getScheduleName(),
                                   schedule.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                   schedule.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SCHEDULE_PUBLISHED.getCode());
        data.put("scheduleId", schedule.getId().toString());
        data.put("scheduleName", schedule.getScheduleName());
        data.put("startDate", schedule.getStartDate().toString());
        data.put("endDate", schedule.getEndDate().toString());

        return sendBulkNotifications(employees, title, body, data);
    }

    /**
     * Gá»­i notification Ä‘áº¿n má»™t user cá»¥ thá»ƒ
     */
    private CompletableFuture<Void> sendNotificationToUser(User user, String title, String body, Map<String, String> data) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // TODO: Get FCM token from user profile or device registration
            String fcmToken = getFCMTokenForUser(user.getId());
            
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("No FCM token found for user: {}", user.getId());
                future.complete(null);
                return future;
            }

            Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                        .setIcon("ic_notification")
                        .setColor("#1890ff")
                        .setSound("default")
                        .build())
                    .build())
                .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                        .setSound("default")
                        .setBadge(1)
                        .build())
                    .build())
                .build();

            firebaseMessaging.sendAsync(message).addListener(() -> {
                log.debug("Successfully sent notification to user: {}", user.getId());
                
                // Also save to Firebase for notification history
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("title", title);
                notificationData.put("body", body);
                notificationData.putAll(data);
                
                firebaseShiftService.sendShiftNotification(user.getId(), title, body, 
                                                         data.get("type"), notificationData);
                
                future.complete(null);
            }, Runnable::run);

        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", user.getId(), e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Gá»­i bulk notifications Ä‘áº¿n nhiá»u users
     */
    private CompletableFuture<Void> sendBulkNotifications(List<User> users, String title, String body, Map<String, String> data) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                        .setIcon("ic_notification")
                        .setColor("#1890ff")
                        .setSound("default")
                        .build())
                    .build())
                .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                        .setSound("default")
                        .setBadge(1)
                        .build())
                    .build());

            // Collect FCM tokens
            for (User user : users) {
                String fcmToken = getFCMTokenForUser(user.getId());
                if (fcmToken != null && !fcmToken.isEmpty()) {
                    messageBuilder.addToken(fcmToken);
                }
            }

            MulticastMessage message = messageBuilder.build();

            firebaseMessaging.sendMulticastAsync(message).addListener(() -> {
                log.debug("Successfully sent bulk notification to {} users", users.size());
                future.complete(null);
            }, Runnable::run);

        } catch (Exception e) {
            log.error("Error sending bulk notifications: {}", e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Láº¥y FCM token cá»§a user (placeholder - cáº§n implement)
     */
    private String getFCMTokenForUser(Long userId) {
        // TODO: Implement FCM token retrieval from user device registration
        // This could be stored in database or Redis cache
        log.debug("Getting FCM token for user: {}", userId);
        return null; // Placeholder
    }

    /**
     * Scheduled method Ä‘á»ƒ gá»­i shift reminders
     */
    public void sendScheduledShiftReminders(List<ShiftAssignment> upcomingAssignments, int minutesBefore) {
        log.info("Sending scheduled shift reminders for {} assignments", upcomingAssignments.size());

        for (ShiftAssignment assignment : upcomingAssignments) {
            try {
                sendShiftReminderNotification(assignment, minutesBefore);
            } catch (Exception e) {
                log.error("Error sending reminder for assignment {}: {}", assignment.getId(), e.getMessage());
            }
        }
    }

    /**
     * Scheduled method Ä‘á»ƒ gá»­i check-in reminders
     */
    public void sendScheduledCheckInReminders(List<ShiftAssignment> pendingCheckIns) {
        log.info("Sending scheduled check-in reminders for {} assignments", pendingCheckIns.size());

        for (ShiftAssignment assignment : pendingCheckIns) {
            try {
                sendCheckInReminderNotification(assignment);
            } catch (Exception e) {
                log.error("Error sending check-in reminder for assignment {}: {}", assignment.getId(), e.getMessage());
            }
        }
    }

    /**
     * Scheduled method Ä‘á»ƒ gá»­i check-out reminders
     */
    public void sendScheduledCheckOutReminders(List<ShiftAssignment> pendingCheckOuts) {
        log.info("Sending scheduled check-out reminders for {} assignments", pendingCheckOuts.size());

        for (ShiftAssignment assignment : pendingCheckOuts) {
            try {
                sendCheckOutReminderNotification(assignment);
            } catch (Exception e) {
                log.error("Error sending check-out reminder for assignment {}: {}", assignment.getId(), e.getMessage());
            }
        }
    }
}
