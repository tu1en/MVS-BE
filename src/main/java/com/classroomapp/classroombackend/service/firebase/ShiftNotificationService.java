package com.classroomapp.classroombackend.service.firebase;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Service cho Push Notifications của Shift Management
 * Gửi notifications cho shift assignments, swap requests, schedule changes
 */
@Service
@RequiredArgsConstructor
public class ShiftNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ShiftNotificationService.class);

    private final FirebaseMessaging firebaseMessaging;
    private final FirebaseShiftService firebaseShiftService;

    // Notification types
    public enum NotificationType {
        SHIFT_ASSIGNED("shift_assigned", "Ca làm việc mới", "🕐"),
        SHIFT_UPDATED("shift_updated", "Ca làm việc cập nhật", "📝"),
        SHIFT_CANCELLED("shift_cancelled", "Ca làm việc bị hủy", "❌"),
        SHIFT_REMINDER("shift_reminder", "Nhắc nhở ca làm việc", "⏰"),
        CHECK_IN_REMINDER("check_in_reminder", "Nhắc nhở check-in", "📍"),
        CHECK_OUT_REMINDER("check_out_reminder", "Nhắc nhở check-out", "🏁"),
        SWAP_REQUEST_RECEIVED("swap_request_received", "Yêu cầu đổi ca", "🔄"),
        SWAP_REQUEST_APPROVED("swap_request_approved", "Đổi ca được phê duyệt", "✅"),
        SWAP_REQUEST_REJECTED("swap_request_rejected", "Đổi ca bị từ chối", "❌"),
        SCHEDULE_PUBLISHED("schedule_published", "Lịch làm việc mới", "📅"),
        SCHEDULE_UPDATED("schedule_updated", "Lịch làm việc cập nhật", "📝");

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
     * Gửi notification khi có shift assignment mới
     */
    public CompletableFuture<Void> sendShiftAssignedNotification(ShiftAssignment assignment) {
        log.info("Đang gửi thông báo ca làm việc mới cho assignment ID: {}", assignment.getId());

        String title = NotificationType.SHIFT_ASSIGNED.getTitle();
        String body = String.format("Bạn được phân công ca %s vào ngày %s từ %s đến %s",
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
     * Gửi notification khi shift bị cập nhật
     */
    public CompletableFuture<Void> sendShiftUpdatedNotification(ShiftAssignment assignment) {
        log.info("Đang gửi thông báo ca làm việc cập nhật cho assignment ID: {}", assignment.getId());

        String title = NotificationType.SHIFT_UPDATED.getTitle();
        String body = String.format("Ca làm việc %s ngày %s đã được cập nhật",
                                   assignment.getShiftTemplate().getTemplateName(),
                                   assignment.getAssignmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SHIFT_UPDATED.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gửi notification khi shift bị hủy
     */
    public CompletableFuture<Void> sendShiftCancelledNotification(ShiftAssignment assignment, String reason) {
        log.info("Đang gửi thông báo ca làm việc bị hủy cho assignment ID: {}", assignment.getId());

        String title = NotificationType.SHIFT_CANCELLED.getTitle();
        String body = String.format("Ca làm việc %s ngày %s đã bị hủy. Lý do: %s",
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
     * Gửi reminder notification trước khi ca bắt đầu
     */
    public CompletableFuture<Void> sendShiftReminderNotification(ShiftAssignment assignment, int minutesBefore) {
        log.info("Đang gửi thông báo nhắc nhở ca làm việc cho assignment ID: {} ({} phút trước)", 
                assignment.getId(), minutesBefore);

        String title = NotificationType.SHIFT_REMINDER.getTitle();
        String body = String.format("Ca làm việc %s sẽ bắt đầu trong %d phút (lúc %s)",
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
     * Gửi notification nhắc nhở check-in
     */
    public CompletableFuture<Void> sendCheckInReminderNotification(ShiftAssignment assignment) {
        log.info("Đang gửi thông báo nhắc nhở check-in cho assignment ID: {}", assignment.getId());

        String title = NotificationType.CHECK_IN_REMINDER.getTitle();
        String body = String.format("Đã đến giờ check-in cho ca %s. Vui lòng check-in ngay!",
                                   assignment.getShiftTemplate().getTemplateName());

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.CHECK_IN_REMINDER.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());
        data.put("action", "check_in");

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gửi notification nhắc nhở check-out
     */
    public CompletableFuture<Void> sendCheckOutReminderNotification(ShiftAssignment assignment) {
        log.info("Đang gửi thông báo nhắc nhở check-out cho assignment ID: {}", assignment.getId());

        String title = NotificationType.CHECK_OUT_REMINDER.getTitle();
        String body = String.format("Đã đến giờ check-out cho ca %s. Vui lòng check-out!",
                                   assignment.getShiftTemplate().getTemplateName());

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.CHECK_OUT_REMINDER.getCode());
        data.put("assignmentId", assignment.getId().toString());
        data.put("assignedUserId", assignment.getAssignedUser().getId().toString());
        data.put("action", "check_out");

        return sendNotificationToUser(assignment.getAssignedUser(), title, body, data);
    }

    /**
     * Gửi notification khi nhận được swap request
     */
    public CompletableFuture<Void> sendSwapRequestReceivedNotification(ShiftSwapRequest swapRequest) {
        log.info("Đang gửi thông báo nhận yêu cầu đổi ca cho request ID: {}", swapRequest.getId());

        String title = NotificationType.SWAP_REQUEST_RECEIVED.getTitle();
        String body = String.format("%s muốn đổi ca với bạn. Ca %s ngày %s",
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
     * Gửi notification khi swap request được phê duyệt
     */
    public CompletableFuture<Void> sendSwapRequestApprovedNotification(ShiftSwapRequest swapRequest) {
        log.info("Đang gửi thông báo yêu cầu đổi ca được phê duyệt cho request ID: {}", swapRequest.getId());

        String title = NotificationType.SWAP_REQUEST_APPROVED.getTitle();
        String body = String.format("Yêu cầu đổi ca của bạn với %s đã được phê duyệt",
                                   swapRequest.getTargetEmployee().getFullName());

        Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.SWAP_REQUEST_APPROVED.getCode());
        data.put("swapRequestId", swapRequest.getId().toString());
        data.put("targetUserName", swapRequest.getTargetEmployee().getFullName());

        // Gửi cho cả requester và target employee
        CompletableFuture<Void> requesterNotification = sendNotificationToUser(swapRequest.getRequester(), title, body, data);
        CompletableFuture<Void> targetNotification = sendNotificationToUser(swapRequest.getTargetEmployee(), title, body, data);

        return CompletableFuture.allOf(requesterNotification, targetNotification);
    }

    /**
     * Gửi notification khi swap request bị từ chối
     */
    public CompletableFuture<Void> sendSwapRequestRejectedNotification(ShiftSwapRequest swapRequest, String reason) {
        log.info("Đang gửi thông báo yêu cầu đổi ca bị từ chối cho request ID: {}", swapRequest.getId());

        String title = NotificationType.SWAP_REQUEST_REJECTED.getTitle();
        String body = String.format("Yêu cầu đổi ca của bạn với %s đã bị từ chối. Lý do: %s",
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
     * Gửi notification khi schedule được publish
     */
    public CompletableFuture<Void> sendSchedulePublishedNotification(ShiftSchedule schedule, List<User> employees) {
        log.info("Đang gửi thông báo lịch làm việc được xuất bản cho schedule ID: {} đến {} nhân viên", 
                schedule.getId(), employees.size());

        String title = NotificationType.SCHEDULE_PUBLISHED.getTitle();
        String body = String.format("Lịch làm việc '%s' từ %s đến %s đã được xuất bản",
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
     * Gửi notification đến một user cụ thể
     */
    private CompletableFuture<Void> sendNotificationToUser(User user, String title, String body, Map<String, String> data) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // TODO: Get FCM token from user profile or device registration
            String fcmToken = getFCMTokenForUser(user.getId());
            
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("Không tìm thấy FCM token cho user: {}", user.getId());
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
                log.debug("Đã gửi thông báo thành công đến user: {}", user.getId());
                
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
            log.error("Lỗi khi gửi thông báo đến user {}: {}", user.getId(), e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Gửi bulk notifications đến nhiều users
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
                log.debug("Đã gửi thông báo hàng loạt thành công đến {} users", users.size());
                future.complete(null);
            }, Runnable::run);

        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo hàng loạt: {}", e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Lấy FCM token của user (placeholder - cần implement)
     */
    private String getFCMTokenForUser(Long userId) {
        // TODO: Implement FCM token retrieval from user device registration
        // This could be stored in database or Redis cache
        log.debug("Đang lấy FCM token cho user: {}", userId);
        return null; // Placeholder
    }

    /**
     * Scheduled method để gửi shift reminders
     */
    public void sendScheduledShiftReminders(List<ShiftAssignment> upcomingAssignments, int minutesBefore) {
        log.info("Đang gửi nhắc nhở ca làm việc theo lịch cho {} assignments", upcomingAssignments.size());

        for (ShiftAssignment assignment : upcomingAssignments) {
            try {
                sendShiftReminderNotification(assignment, minutesBefore);
            } catch (Exception e) {
                log.error("Lỗi khi gửi nhắc nhở cho assignment {}: {}", assignment.getId(), e.getMessage());
            }
        }
    }

    /**
     * Scheduled method để gửi check-in reminders
     */
    public void sendScheduledCheckInReminders(List<ShiftAssignment> pendingCheckIns) {
        log.info("Đang gửi nhắc nhở check-in theo lịch cho {} assignments", pendingCheckIns.size());

        for (ShiftAssignment assignment : pendingCheckIns) {
            try {
                sendCheckInReminderNotification(assignment);
            } catch (Exception e) {
                log.error("Lỗi khi gửi nhắc nhở check-in cho assignment {}: {}", assignment.getId(), e.getMessage());
            }
        }
    }

    /**
     * Scheduled method để gửi check-out reminders
     */
    public void sendScheduledCheckOutReminders(List<ShiftAssignment> pendingCheckOuts) {
        log.info("Đang gửi nhắc nhở check-out theo lịch cho {} assignments", pendingCheckOuts.size());

        for (ShiftAssignment assignment : pendingCheckOuts) {
            try {
                sendCheckOutReminderNotification(assignment);
            } catch (Exception e) {
                log.error("Lỗi khi gửi nhắc nhở check-out cho assignment {}: {}", assignment.getId(), e.getMessage());
            }
        }
    }
}
