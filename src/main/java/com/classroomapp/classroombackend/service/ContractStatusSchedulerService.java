package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractStatusSchedulerService {

    private final ContractRepository contractRepository;
    private final JavaMailSender mailSender;

    /**
     * Chạy hàng ngày lúc 9:00 AM để kiểm tra và cập nhật trạng thái hợp đồng
     */
    @Scheduled(cron = "0 0 9 * * *") // Chạy lúc 9:00 AM mỗi ngày
    @Transactional
    public void updateContractStatuses() {
        // Scheduler disabled: Contract no longer has endDate/startDate.
        log.info("Contract status scheduler disabled (date fields removed). Skipping status update job.");
    }
    
    /**
     * Gửi email thông báo hợp đồng gần hết hạn
     */
    private void sendExpiryNotificationEmail(Contract contract) {
        // Disabled: endDate removed from Contract, email content cannot include expiry date
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(contract.getEmail());
            message.setSubject("Thông báo: Cập nhật hợp đồng");
            String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Hệ thống gửi thông báo liên quan đến hợp đồng của bạn.\n\n" +
                "Trân trọng,\n" +
                "Phòng Nhân sự\n" +
                "Hệ thống Quản lý Lớp học",
                contract.getFullName()
            );
            message.setText(emailContent);
            mailSender.send(message);
            log.info("Notification email sent to: {}", contract.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notification email to {}: {}", contract.getEmail(), e.getMessage());
        }
    }
    
    /**
     * API để test thủ công việc cập nhật trạng thái hợp đồng
     */
    public void manualUpdateContractStatuses() {
        log.info("Manual contract status update triggered");
        updateContractStatuses();
    }
}
