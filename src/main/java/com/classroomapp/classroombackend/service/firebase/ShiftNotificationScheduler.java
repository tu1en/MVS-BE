package com.classroomapp.classroombackend.service.firebase;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftAssignmentService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftSwapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Scheduled Tasks cho Shift Notifications
 * Tá»± Ä‘á»™ng gá»­i notifications cho shift reminders, check-in/out reminders
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftNotificationScheduler {

    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftSwapService shiftSwapService;
    private final ShiftNotificationService shiftNotificationService;

    /**
     * Gá»­i shift reminders má»—i 15 phÃºt
     * Nháº¯c nhá»Ÿ trÆ°á»›c 30 phÃºt vÃ  15 phÃºt trÆ°á»›c khi ca báº¯t Ä‘áº§u
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15 minutes
    public void sendShiftReminders() {
        log.debug("Running shift reminders scheduler");

        try {
            LocalDate today = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            
            // TÃ¬m assignments sáº¯p báº¯t Ä‘áº§u trong 30 phÃºt
            LocalTime reminderTime30 = currentTime.plusMinutes(30);
            LocalTime reminderTime15 = currentTime.plusMinutes(15);
            
            List<ShiftAssignment> todayAssignments = shiftAssignmentService.findByDate(today);
            
            for (ShiftAssignment assignment : todayAssignments) {
                if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.SCHEDULED) {
                    LocalTime startTime = assignment.getPlannedStartTime();
                    
                    // Reminder 30 phÃºt trÆ°á»›c
                    if (isTimeForReminder(currentTime, startTime, reminderTime30)) {
                        shiftNotificationService.sendShiftReminderNotification(assignment, 30);
                    }
                    
                    // Reminder 15 phÃºt trÆ°á»›c
                    if (isTimeForReminder(currentTime, startTime, reminderTime15)) {
                        shiftNotificationService.sendShiftReminderNotification(assignment, 15);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Lỗi trong scheduler nhắc nhở ca làm: {}", e.getMessage());
        }
    }

    /**
     * Gá»­i check-in reminders má»—i 5 phÃºt
     * Nháº¯c nhá»Ÿ check-in cho assignments Ä‘Ã£ Ä‘áº¿n giá»
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes
    public void sendCheckInReminders() {
        log.debug("Running check-in reminders scheduler");

        try {
            List<ShiftAssignment> pendingCheckIns = shiftAssignmentService.findPendingCheckIns();
            
            if (!pendingCheckIns.isEmpty()) {
                log.info("Sending check-in reminders for {} assignments", pendingCheckIns.size());
                shiftNotificationService.sendScheduledCheckInReminders(pendingCheckIns);
            }
            
        } catch (Exception e) {
            log.error("Lỗi trong scheduler nhắc nhở check-in: {}", e.getMessage());
        }
    }

    /**
     * Gá»­i check-out reminders má»—i 10 phÃºt
     * Nháº¯c nhá»Ÿ check-out cho assignments sáº¯p káº¿t thÃºc
     */
    @Scheduled(fixedRate = 10 * 60 * 1000) // 10 minutes
    public void sendCheckOutReminders() {
        log.debug("Running check-out reminders scheduler");

        try {
            List<ShiftAssignment> pendingCheckOuts = shiftAssignmentService.findPendingCheckOuts();
            
            if (!pendingCheckOuts.isEmpty()) {
                log.info("Sending check-out reminders for {} assignments", pendingCheckOuts.size());
                shiftNotificationService.sendScheduledCheckOutReminders(pendingCheckOuts);
            }
            
        } catch (Exception e) {
            log.error("Lỗi trong scheduler nhắc nhở check-out: {}", e.getMessage());
        }
    }

    /**
     * Xá»­ lÃ½ expired swap requests má»—i giá»
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour
    public void processExpiredSwapRequests() {
        log.debug("Running expired swap requests processor");

        try {
            shiftSwapService.processExpiredRequests();
            
        } catch (Exception e) {
            log.error("Lỗi xử lý yêu cầu đổi ca hết hạn: {}", e.getMessage());
        }
    }

    /**
     * Cleanup old notifications má»—i ngÃ y lÃºc 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    public void cleanupOldNotifications() {
        log.info("Running cleanup old notifications");

        try {
            // Cleanup notifications older than 30 days
            int daysOld = 30;
            int cleaned = shiftSwapService.cleanupOldRequests(daysOld);
            log.info("Cleaned up {} old swap requests", cleaned);
            
        } catch (Exception e) {
            log.error("Lỗi dọn dẹp thông báo cũ: {}", e.getMessage());
        }
    }

    /**
     * Gá»­i daily summary notifications má»—i ngÃ y lÃºc 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8:00 AM
    public void sendDailySummaryNotifications() {
        log.info("Sending daily summary notifications");

        try {
            LocalDate today = LocalDate.now();
            List<ShiftAssignment> todayAssignments = shiftAssignmentService.findByDate(today);
            
            // Group assignments by employee and send summary
            // TODO: Implement daily summary logic
            log.info("Found {} assignments for today", todayAssignments.size());
            
        } catch (Exception e) {
            log.error("Lỗi gửi thông báo tổng hợp hằng ngày: {}", e.getMessage());
        }
    }

    /**
     * Gá»­i weekly schedule notifications má»—i Chá»§ nháº­t lÃºc 6:00 PM
     */
    @Scheduled(cron = "0 0 18 * * SUN") // Every Sunday at 6:00 PM
    public void sendWeeklyScheduleNotifications() {
        log.info("Sending weekly schedule notifications");

        try {
            LocalDate nextMonday = LocalDate.now().plusDays(1);
            
            // TODO: Implement weekly schedule notification logic
            log.info("Preparing weekly schedule notifications for week starting {}", nextMonday);
            
        } catch (Exception e) {
            log.error("Lỗi gửi thông báo lịch hằng tuần: {}", e.getMessage());
        }
    }

    /**
     * Monitor system health má»—i 30 phÃºt
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void monitorSystemHealth() {
        log.debug("Running system health monitor");

        try {
            // Check for assignments without check-in after start time + 30 minutes
            LocalDate today = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            LocalTime checkTime = currentTime.minusMinutes(30);
            
            List<ShiftAssignment> todayAssignments = shiftAssignmentService.findByDate(today);
            
            int missedCheckIns = 0;
            for (ShiftAssignment assignment : todayAssignments) {
                if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.SCHEDULED &&
                    assignment.getPlannedStartTime().isBefore(checkTime) &&
                    assignment.getCheckInTime() == null) {
                    missedCheckIns++;
                }
            }
            
            if (missedCheckIns > 0) {
                log.warn("Found {} assignments with missed check-ins", missedCheckIns);
                // TODO: Send alert to managers
            }
            
        } catch (Exception e) {
            log.error("Lỗi trong theo dõi sức khỏe hệ thống: {}", e.getMessage());
        }
    }

    /**
     * Kiá»ƒm tra xem cÃ³ pháº£i lÃºc gá»­i reminder khÃ´ng
     */
    private boolean isTimeForReminder(LocalTime currentTime, LocalTime startTime, LocalTime reminderTime) {
        // Kiá»ƒm tra xem thá»i gian hiá»‡n táº¡i cÃ³ náº±m trong khoáº£ng reminder khÃ´ng
        // Vá»›i tolerance 2 phÃºt Ä‘á»ƒ trÃ¡nh miss notifications
        LocalTime toleranceStart = reminderTime.minusMinutes(2);
        LocalTime toleranceEnd = reminderTime.plusMinutes(2);
        
        return startTime.equals(reminderTime) || 
               (currentTime.isAfter(toleranceStart) && currentTime.isBefore(toleranceEnd) && 
                startTime.equals(reminderTime));
    }

    /**
     * Emergency notification cho critical issues
     */
    public void sendEmergencyNotification(String title, String message, List<Long> managerIds) {
        log.warn("Sending emergency notification: {}", title);

        try {
            // TODO: Implement emergency notification logic
            // Send to all managers immediately
            
        } catch (Exception e) {
            log.error("Lỗi gửi thông báo khẩn cấp: {}", e.getMessage());
        }
    }

    /**
     * Test method Ä‘á»ƒ trigger notifications manually (for development)
     */
    public void triggerTestNotifications() {
        log.info("Triggering test notifications");

        try {
            sendShiftReminders();
            sendCheckInReminders();
            sendCheckOutReminders();
            
        } catch (Exception e) {
            log.error("Lỗi kích hoạt thông báo kiểm thử: {}", e.getMessage());
        }
    }
}
