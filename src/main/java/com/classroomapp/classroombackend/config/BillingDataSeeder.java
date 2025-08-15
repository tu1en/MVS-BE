package com.classroomapp.classroombackend.config;

import com.classroomapp.classroombackend.model.*;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.*;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Billing Data Seeder - Tạo dữ liệu mẫu cho hệ thống hóa đơn phụ huynh
 * Chạy sau khi các seeder khác đã tạo users và parent relationships
 */
@Component
@Order(10) // Run after other seeders
@RequiredArgsConstructor
@Slf4j
public class BillingDataSeeder implements CommandLineRunner {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StudentParentRepository studentParentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            log.info("🧾 Starting Billing Data Seeder...");
            
            // Skip if data already exists
            if (invoiceRepository.count() > 0) {
                log.info("Billing data already exists, skipping seeder");
                return;
            }

            seedBillingData();
            log.info("✅ Billing Data Seeder completed successfully!");
            
        } catch (Exception e) {
            log.error("❌ Error in Billing Data Seeder", e);
            throw e;
        }
    }

    private void seedBillingData() {
        log.info("Creating billing data for existing students...");

        // Get existing students (those who have parent relationships)
        List<User> studentsWithParents = studentParentRepository.findAll()
            .stream()
            .map(sp -> sp.getStudent())
            .filter(student -> student != null)
            .distinct()
            .toList();

        if (studentsWithParents.isEmpty()) {
            log.warn("No students with parent relationships found. Skipping billing data creation.");
            return;
        }

        log.info("Found {} students with parents. Creating invoices...", studentsWithParents.size());

        // Create invoices for each student
        for (int i = 0; i < studentsWithParents.size() && i < 4; i++) {
            User student = studentsWithParents.get(i);
            createInvoicesForStudent(student, i + 1);
        }
    }

    private void createInvoicesForStudent(User student, int studentIndex) {
        String studentName = student.getFullName();
        log.info("Creating invoices for student: {}", studentName);

        switch (studentIndex) {
            case 1 -> createStudent1Invoices(student);
            case 2 -> createStudent2Invoices(student);
            case 3 -> createStudent3Invoices(student);
            case 4 -> createStudent4Invoices(student);
        }
    }

    private void createStudent1Invoices(User student) {
        // Student 1: 2 paid invoices, 1 pending
        
        // Invoice 1 - Paid
        Invoice invoice1 = createInvoice(
            "HĐ-2025-001", student.getId(), 
            LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 15),
            BigDecimal.valueOf(2500000), BigDecimal.valueOf(2500000),
            Invoice.InvoiceStatus.PAID, "Hóa đơn học phí tháng 1/2025 - Môn Toán"
        );
        addInvoiceItem(invoice1, "Học phí môn Toán - Tháng 1", 1, BigDecimal.valueOf(2500000));
        createPayment(invoice1, LocalDateTime.of(2025, 1, 20, 10, 30), 
                     BigDecimal.valueOf(2500000), Payment.PaymentMethod.BANK_TRANSFER,
                     "TF20250120001", "Chuyển khoản học phí tháng 1");

        // Invoice 2 - Paid  
        Invoice invoice2 = createInvoice(
            "HĐ-2025-002", student.getId(),
            LocalDate.of(2025, 2, 15), LocalDate.of(2025, 3, 15),
            BigDecimal.valueOf(1800000), BigDecimal.valueOf(1800000),
            Invoice.InvoiceStatus.PAID, "Phí hoạt động ngoại khóa và câu lạc bộ"
        );
        addInvoiceItem(invoice2, "Phí tham quan ngoại khóa", 1, BigDecimal.valueOf(800000));
        addInvoiceItem(invoice2, "Phí hoạt động thể thao", 1, BigDecimal.valueOf(500000));
        addInvoiceItem(invoice2, "Phí câu lạc bộ học thuật", 1, BigDecimal.valueOf(500000));
        createPayment(invoice2, LocalDateTime.of(2025, 2, 20, 14, 15),
                     BigDecimal.valueOf(1800000), Payment.PaymentMethod.CASH,
                     null, "Thanh toán tiền mặt phí ngoại khóa");

        // Invoice 3 - Pending
        Invoice invoice3 = createInvoice(
            "HĐ-2025-003", student.getId(),
            LocalDate.of(2025, 3, 1), LocalDate.of(2025, 4, 1),
            BigDecimal.valueOf(2500000), BigDecimal.ZERO,
            Invoice.InvoiceStatus.PENDING, "Hóa đơn học phí tháng 3/2025 - Môn Toán"
        );
        addInvoiceItem(invoice3, "Học phí môn Toán - Tháng 3", 1, BigDecimal.valueOf(2500000));
    }

    private void createStudent2Invoices(User student) {
        // Student 2: 1 paid, 1 partial, 1 overdue
        
        // Invoice 1 - Paid
        Invoice invoice1 = createInvoice(
            "HĐ-2025-004", student.getId(),
            LocalDate.of(2025, 1, 10), LocalDate.of(2025, 2, 10),
            BigDecimal.valueOf(3200000), BigDecimal.valueOf(3200000),
            Invoice.InvoiceStatus.PAID, "Học phí khóa Vật lý nâng cao"
        );
        addInvoiceItem(invoice1, "Học phí khóa Vật lý nâng cao - Học kỳ 1", 1, BigDecimal.valueOf(3200000));
        createPayment(invoice1, LocalDateTime.of(2025, 1, 15, 11, 20),
                     BigDecimal.valueOf(3200000), Payment.PaymentMethod.ONLINE,
                     "ON20250115001", "Thanh toán online khóa Vật lý");

        // Invoice 2 - Partial
        Invoice invoice2 = createInvoice(
            "HĐ-2025-005", student.getId(),
            LocalDate.of(2025, 2, 20), LocalDate.of(2025, 3, 20),
            BigDecimal.valueOf(2800000), BigDecimal.valueOf(1400000),
            Invoice.InvoiceStatus.PARTIAL, "Học phí tháng 2/2025 - Môn Vật lý"
        );
        addInvoiceItem(invoice2, "Học phí môn Vật lý - Tháng 2", 1, BigDecimal.valueOf(2800000));
        createPayment(invoice2, LocalDateTime.of(2025, 3, 1, 13, 20),
                     BigDecimal.valueOf(1400000), Payment.PaymentMethod.CASH,
                     null, "Thanh toán một phần bằng tiền mặt");

        // Invoice 3 - Overdue
        Invoice invoice3 = createInvoice(
            "HĐ-2025-006", student.getId(),
            LocalDate.of(2025, 2, 25), LocalDate.of(2025, 1, 25), // Due date in past = overdue
            BigDecimal.valueOf(1200000), BigDecimal.ZERO,
            Invoice.InvoiceStatus.OVERDUE, "Phí tài liệu và đồ dùng học tập"
        );
        addInvoiceItem(invoice3, "Sách giáo khoa Vật lý 12", 2, BigDecimal.valueOf(300000));
        addInvoiceItem(invoice3, "Bộ dụng cụ thí nghiệm Vật lý", 1, BigDecimal.valueOf(600000));
    }

    private void createStudent3Invoices(User student) {
        // Student 3: Mixed status
        Invoice invoice1 = createInvoice(
            "HĐ-2025-007", student.getId(),
            LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1),
            BigDecimal.valueOf(2500000), BigDecimal.ZERO,
            Invoice.InvoiceStatus.PENDING, "Hóa đơn học phí tháng 4/2025"
        );
        addInvoiceItem(invoice1, "Học phí môn Toán - Tháng 4", 1, BigDecimal.valueOf(2500000));
    }

    private void createStudent4Invoices(User student) {
        // Student 4: Recent invoice
        Invoice invoice1 = createInvoice(
            "HĐ-2025-008", student.getId(),
            LocalDate.of(2025, 3, 15), LocalDate.of(2025, 4, 15),
            BigDecimal.valueOf(1500000), BigDecimal.ZERO,
            Invoice.InvoiceStatus.PENDING, "Phí thi cuối kỳ và chứng chỉ"
        );
        addInvoiceItem(invoice1, "Phí thi cuối kỳ", 1, BigDecimal.valueOf(800000));
        addInvoiceItem(invoice1, "Phí làm chứng chỉ", 1, BigDecimal.valueOf(700000));
    }

    private Invoice createInvoice(String invoiceNumber, Long studentId, LocalDate issueDate, 
                                LocalDate dueDate, BigDecimal totalAmount, BigDecimal paidAmount,
                                Invoice.InvoiceStatus status, String note) {
        Invoice invoice = Invoice.builder()
            .invoiceNumber(invoiceNumber)
            .studentId(studentId)
            .issueDate(issueDate)
            .dueDate(dueDate)
            .totalAmount(totalAmount)
            .paidAmount(paidAmount)
            .status(status)
            .note(note)
            .documentPath("/uploads/invoices/" + invoiceNumber + ".pdf") // Sample path
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice: {} for student ID: {} - Amount: {}", 
                invoiceNumber, studentId, totalAmount);
        return saved;
    }

    private void addInvoiceItem(Invoice invoice, String description, Integer quantity, BigDecimal unitPrice) {
        InvoiceItem item = InvoiceItem.builder()
            .invoiceId(invoice.getId())
            .description(description)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .amount(unitPrice.multiply(BigDecimal.valueOf(quantity)))
            .build();

        invoiceItemRepository.save(item);
        log.debug("Added item to invoice {}: {} x{} = {}", 
                invoice.getInvoiceNumber(), description, quantity, item.getAmount());
    }

    private void createPayment(Invoice invoice, LocalDateTime paymentDate, BigDecimal amount,
                             Payment.PaymentMethod method, String reference, String note) {
        Long receiptId = System.currentTimeMillis() + invoice.getId();
        Payment payment = Payment.builder()
            .invoiceId(invoice.getId())
            .studentId(invoice.getStudentId())
            .paymentDate(paymentDate)
            .amount(amount)
            .paymentMethod(method)
            .referenceNumber(reference)
            .note(note)
            .status(Payment.PaymentStatus.COMPLETED)
            .receiptId(receiptId)
            .receiptPath("/uploads/receipts/receipt_" + invoice.getInvoiceNumber() + ".pdf") // Sample path
            .createdAt(LocalDateTime.now())
            .build();

        paymentRepository.save(payment);
        log.info("Created payment: {} VND for invoice {} via {}", 
                amount, invoice.getInvoiceNumber(), method);
    }
}