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
        log.info("Starting daily contract status update job");
        
        LocalDate today = LocalDate.now();
        
        log.info("Starting contract status update - Today: {}", today);
        
        // Lấy tất cả hợp đồng đang ACTIVE
        List<Contract> activeContracts = contractRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
        log.info("Found {} active contracts to check", activeContracts.size());
        
        int nearExpiryCount = 0;
        int expiredCount = 0;
        
        for (Contract contract : activeContracts) {
            if (contract.getEndDate() == null) {
                log.info("Contract {} has no end date, skipping", contract.getId());
                continue; // Bỏ qua hợp đồng không có ngày kết thúc
            }
            
            LocalDate endDate = contract.getEndDate();
            LocalDate nearExpiryDate = endDate.minusDays(15); // 15 ngày TRƯỚC ngày kết thúc
            
            log.info("Checking contract {} - End date: {}, Near expiry date: {}", 
                    contract.getId(), endDate, nearExpiryDate);
            
            // Kiểm tra hợp đồng đã hết hạn (ngày kết thúc <= hôm nay)
            if (endDate.isBefore(today) || endDate.isEqual(today)) {
                contract.setStatus("EXPIRED");
                contractRepository.save(contract);
                expiredCount++;
                log.info("Contract {} expired on {}", contract.getId(), endDate);
            }
            // Kiểm tra hợp đồng gần hết hạn (hôm nay >= 15 ngày trước ngày kết thúc)
            else if ((today.isEqual(nearExpiryDate) || today.isAfter(nearExpiryDate)) && today.isBefore(endDate)) {
                contract.setStatus("NEAR_EXPIRY");
                contractRepository.save(contract);
                nearExpiryCount++;
                
                // Gửi email thông báo
                sendExpiryNotificationEmail(contract);
                log.info("Contract {} marked as near expiry - expires on {}, near expiry started on {}", 
                        contract.getId(), endDate, nearExpiryDate);
            } else {
                log.info("Contract {} is still active - expires on {}", contract.getId(), endDate);
            }
        }
        
        log.info("Contract status update completed: {} near expiry, {} expired", nearExpiryCount, expiredCount);
    }
    
    /**
     * Gửi email thông báo hợp đồng gần hết hạn
     */
    private void sendExpiryNotificationEmail(Contract contract) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(contract.getEmail());
            message.setSubject("Thông báo: Hợp đồng của bạn sắp hết hạn");
            
            String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Chúng tôi xin thông báo rằng hợp đồng của bạn sắp hết hạn:\n\n" +
                "- Họ tên: %s\n" +
                "- Vị trí: %s\n" +
                "- Phòng ban: %s\n" +
                "- Ngày kết thúc hợp đồng: %s\n" +
                "- Thời gian còn lại: khoảng 15 ngày\n\n" +
                "Vui lòng liên hệ với phòng Nhân sự để thực hiện các thủ tục gia hạn hợp đồng nếu cần thiết.\n\n" +
                "Trân trọng,\n" +
                "Phòng Nhân sự\n" +
                "Hệ thống Quản lý Lớp học",
                contract.getFullName(),
                contract.getFullName(),
                contract.getPosition(),
                contract.getDepartment() != null ? contract.getDepartment() : "Chưa xác định",
                contract.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            
            message.setText(emailContent);
            mailSender.send(message);
            
            log.info("Expiry notification email sent to: {}", contract.getEmail());
        } catch (Exception e) {
            log.error("Failed to send expiry notification email to {}: {}", contract.getEmail(), e.getMessage());
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
