package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.service.BillingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingNotificationServiceImpl implements BillingNotificationService {

    private final UserRepository userRepository;
    private final StudentParentRepository studentParentRepository;

    private String formatCurrency(Long amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    private List<Parent> getParentsOfStudent(Long studentId) {
        List<StudentParent> studentParents = studentParentRepository.findActiveParentsByStudentId(studentId);
        return studentParents.stream()
            .map(StudentParent::getParent)
            .filter(parent -> parent != null)
            .toList();
    }

    @Override
    public void notifyInvoiceCreated(Long studentId, String invoiceNumber, Long totalAmount) {
        try {
            List<Parent> parents = getParentsOfStudent(studentId);
            User student = userRepository.findById(studentId).orElse(null);
            
            if (student == null) {
                log.warn("Student not found with ID: {}", studentId);
                return;
            }

            String message = String.format(
                "🧾 HÓA ĐÁN MỚI\\n\\n" +
                "Hóa đơn %s đã được tạo cho học sinh %s.\\n" +
                "Tổng số tiền: %s\\n" +
                "Vui lòng kiểm tra trong mục Tài chính để xem chi tiết.",
                invoiceNumber, student.getFullName(), formatCurrency(totalAmount)
            );

            for (Parent parent : parents) {
                sendNotificationToParent(parent, "Hóa đơn mới", message);
            }
            
            log.info("Sent invoice creation notification for {} to {} parents", invoiceNumber, parents.size());
            
        } catch (Exception e) {
            log.error("Error sending invoice creation notification", e);
        }
    }

    @Override
    public void notifyInvoicePaid(Long studentId, String invoiceNumber, Long paidAmount) {
        try {
            List<Parent> parents = getParentsOfStudent(studentId);
            User student = userRepository.findById(studentId).orElse(null);
            
            if (student == null) {
                log.warn("Student not found with ID: {}", studentId);
                return;
            }

            String message = String.format(
                "✅ THANH TOÁN THÀNH CÔNG\\n\\n" +
                "Hóa đơn %s của học sinh %s đã được thanh toán thành công.\\n" +
                "Số tiền: %s\\n" +
                "Cảm ơn quý phụ huynh đã thanh toán đúng hạn!",
                invoiceNumber, student.getFullName(), formatCurrency(paidAmount)
            );

            for (Parent parent : parents) {
                sendNotificationToParent(parent, "Thanh toán thành công", message);
            }
            
            log.info("Sent invoice paid notification for {} to {} parents", invoiceNumber, parents.size());
            
        } catch (Exception e) {
            log.error("Error sending invoice paid notification", e);
        }
    }

    @Override
    public void notifyInvoiceOverdue(Long studentId, String invoiceNumber, Long overdueAmount) {
        try {
            List<Parent> parents = getParentsOfStudent(studentId);
            User student = userRepository.findById(studentId).orElse(null);
            
            if (student == null) {
                log.warn("Student not found with ID: {}", studentId);
                return;
            }

            String message = String.format(
                "⚠️ HÓA ĐÁN QUÁ HẠN\\n\\n" +
                "Hóa đơn %s của học sinh %s đã quá hạn thanh toán.\\n" +
                "Số tiền chưa thanh toán: %s\\n" +
                "Vui lòng liên hệ phòng kế toán để thanh toán sớm nhất.",
                invoiceNumber, student.getFullName(), formatCurrency(overdueAmount)
            );

            for (Parent parent : parents) {
                sendNotificationToParent(parent, "Hóa đơn quá hạn", message);
            }
            
            log.info("Sent overdue notification for {} to {} parents", invoiceNumber, parents.size());
            
        } catch (Exception e) {
            log.error("Error sending overdue notification", e);
        }
    }

    @Override
    public void notifyPartialPayment(Long studentId, String invoiceNumber, Long paidAmount, Long remainingAmount) {
        try {
            List<Parent> parents = getParentsOfStudent(studentId);
            User student = userRepository.findById(studentId).orElse(null);
            
            if (student == null) {
                log.warn("Student not found with ID: {}", studentId);
                return;
            }

            String message = String.format(
                "💰 THANH TOÁN MỘT PHẦN\\n\\n" +
                "Hóa đơn %s của học sinh %s đã được thanh toán một phần.\\n" +
                "Số tiền đã thanh toán: %s\\n" +
                "Số tiền còn lại: %s\\n" +
                "Vui lòng thanh toán số tiền còn lại.",
                invoiceNumber, student.getFullName(), 
                formatCurrency(paidAmount), formatCurrency(remainingAmount)
            );

            for (Parent parent : parents) {
                sendNotificationToParent(parent, "Thanh toán một phần", message);
            }
            
            log.info("Sent partial payment notification for {} to {} parents", invoiceNumber, parents.size());
            
        } catch (Exception e) {
            log.error("Error sending partial payment notification", e);
        }
    }

    /**
     * Send notification to a specific parent
     * This is a placeholder - implement based on your notification system
     */
    private void sendNotificationToParent(Parent parent, String title, String message) {
        try {
            // TODO: Implement actual notification sending mechanism
            // Options:
            // 1. Save to database notification table
            // 2. Send email via EmailService
            // 3. Send SMS
            // 4. Send push notification
            // 5. WebSocket real-time notification
            
            log.info("Notification to parent {}: {} - {}", 
                    parent.getName(), title, message.substring(0, Math.min(50, message.length())));
            
            // Example of saving to database (you'll need to create Notification entity):
            // Notification notification = new Notification();
            // notification.setUserId(parent.getUserId());
            // notification.setTitle(title);
            // notification.setMessage(message);
            // notification.setType("BILLING");
            // notification.setCreatedAt(LocalDateTime.now());
            // notificationRepository.save(notification);
            
        } catch (Exception e) {
            log.error("Error sending notification to parent {}", parent.getId(), e);
        }
    }
}