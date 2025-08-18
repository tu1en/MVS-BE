package com.classroomapp.classroombackend.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.request.CreateInvoiceDto;
import com.classroomapp.classroombackend.dto.request.UpdateInvoiceStatusDto;
import com.classroomapp.classroombackend.model.Invoice;
import com.classroomapp.classroombackend.model.InvoiceItem;
import com.classroomapp.classroombackend.model.Payment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.InvoiceItemRepository;
import com.classroomapp.classroombackend.repository.InvoiceRepository;
import com.classroomapp.classroombackend.repository.PaymentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.BillingNotificationService;
import com.classroomapp.classroombackend.service.ManagerBillingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerBillingServiceImpl implements ManagerBillingService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BillingNotificationService billingNotificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
            .map(this::convertInvoiceToMap)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllStudents() {
        List<User> students = userRepository.findActiveStudents();
        return students.stream()
            .map(this::convertStudentToMap)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> createInvoiceWithFile(String invoiceDataJson, MultipartFile invoiceFile) {
        try {
            // Parse JSON to DTO
            CreateInvoiceDto invoiceDto = objectMapper.readValue(invoiceDataJson, CreateInvoiceDto.class);
            
            // Save file if provided
            String documentPath = null;
            if (invoiceFile != null && !invoiceFile.isEmpty()) {
                documentPath = saveInvoiceFile(invoiceFile, invoiceDto.getInvoiceNumber());
            }
            
            return createInvoiceInternal(invoiceDto, documentPath);
            
        } catch (Exception e) {
            log.error("Error creating invoice with file", e);
            throw new RuntimeException("Không thể tạo hóa đơn: " + e.getMessage());
        }
    }

    private Map<String, Object> createInvoiceInternal(CreateInvoiceDto invoiceDto, String documentPath) {
        try {
            // Check if invoice number already exists
            if (invoiceRepository.findByInvoiceNumber(invoiceDto.getInvoiceNumber()).isPresent()) {
                throw new RuntimeException("Số hóa đơn đã tồn tại: " + invoiceDto.getInvoiceNumber());
            }

            // Verify student exists
            User student = userRepository.findById(invoiceDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học sinh với ID: " + invoiceDto.getStudentId()));

            // Calculate total amount from items
            BigDecimal totalAmount = invoiceDto.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create invoice
            Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceDto.getInvoiceNumber())
                .studentId(invoiceDto.getStudentId())
                .issueDate(invoiceDto.getIssueDate())
                .dueDate(invoiceDto.getDueDate())
                .totalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .status(Invoice.InvoiceStatus.valueOf(invoiceDto.getStatus()))
                .note(invoiceDto.getNote())
                .documentPath(documentPath)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            Invoice savedInvoice = invoiceRepository.save(invoice);

            // Create invoice items
            List<InvoiceItem> items = invoiceDto.getItems().stream()
                .map(itemDto -> InvoiceItem.builder()
                    .invoiceId(savedInvoice.getId())
                    .description(itemDto.getDescription())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .amount(itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())))
                    .build())
                .collect(Collectors.toList());

            // Save invoice items
            invoiceItemRepository.saveAll(items);

            log.info("Created invoice {} for student {}", savedInvoice.getInvoiceNumber(), student.getFullName());

            // Send notification to parent
            billingNotificationService.notifyInvoiceCreated(
                invoiceDto.getStudentId(), 
                invoiceDto.getInvoiceNumber(), 
                totalAmount.longValue()
            );

            return convertInvoiceToMap(savedInvoice);

        } catch (Exception e) {
            log.error("Error creating invoice", e);
            throw new RuntimeException("Không thể tạo hóa đơn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateInvoiceStatus(Long invoiceId, UpdateInvoiceStatusDto statusDto) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            String oldStatus = invoice.getStatus().name();
            Invoice.InvoiceStatus newStatus = Invoice.InvoiceStatus.valueOf(statusDto.getStatus());

            // Update invoice
            invoice.setStatus(newStatus);
            invoice.setUpdatedAt(LocalDateTime.now());

            // If status is PAID, create payment record
            if (newStatus == Invoice.InvoiceStatus.PAID && statusDto.getPaidAmount() != null) {
                invoice.setPaidAmount(statusDto.getPaidAmount());
                
                // Create payment record
                Payment payment = Payment.builder()
                    .invoiceId(invoiceId)
                    .studentId(invoice.getStudentId())
                    .paymentDate(LocalDateTime.now())
                    .amount(statusDto.getPaidAmount())
                    .paymentMethod(Payment.PaymentMethod.valueOf(statusDto.getPaymentMethod() != null ? 
                                  statusDto.getPaymentMethod() : "CASH"))
                    .referenceNumber(statusDto.getPaymentReference())
                    .note(statusDto.getNote())
                    .status(Payment.PaymentStatus.COMPLETED)
                    .receiptId(System.currentTimeMillis()) // Generate simple receipt ID
                    .createdAt(LocalDateTime.now())
                    .build();

                paymentRepository.save(payment);
                log.info("Created payment record for invoice {}", invoice.getInvoiceNumber());
            }

            Invoice updatedInvoice = invoiceRepository.save(invoice);

            // Send notification to parent based on new status
            switch (newStatus) {
                case PAID -> billingNotificationService.notifyInvoicePaid(
                    invoice.getStudentId(), invoice.getInvoiceNumber(), invoice.getPaidAmount().longValue());
                case PARTIAL -> billingNotificationService.notifyPartialPayment(
                    invoice.getStudentId(), invoice.getInvoiceNumber(), 
                    invoice.getPaidAmount().longValue(), invoice.getRemainingAmount().longValue());
                case OVERDUE -> billingNotificationService.notifyInvoiceOverdue(
                    invoice.getStudentId(), invoice.getInvoiceNumber(), invoice.getRemainingAmount().longValue());
            }

            log.info("Updated invoice {} status from {} to {}", 
                    invoice.getInvoiceNumber(), oldStatus, newStatus);

            return convertInvoiceToMap(updatedInvoice);

        } catch (Exception e) {
            log.error("Error updating invoice status", e);
            throw new RuntimeException("Không thể cập nhật trạng thái hóa đơn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteInvoice(Long invoiceId) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
                throw new RuntimeException("Chỉ có thể xóa hóa đơn có trạng thái PENDING");
            }

            invoiceRepository.delete(invoice);
            log.info("Deleted invoice {}", invoice.getInvoiceNumber());

        } catch (Exception e) {
            log.error("Error deleting invoice {}", invoiceId, e);
            throw new RuntimeException("Không thể xóa hóa đơn: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> recordPayment(Long invoiceId, Map<String, Object> paymentData) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

            BigDecimal paymentAmount = new BigDecimal(paymentData.get("amount").toString());
            
            // Create payment record
            Payment payment = Payment.builder()
                .invoiceId(invoiceId)
                .studentId(invoice.getStudentId())
                .paymentDate(LocalDateTime.now())
                .amount(paymentAmount)
                .paymentMethod(Payment.PaymentMethod.valueOf(
                    paymentData.getOrDefault("paymentMethod", "CASH").toString()))
                .referenceNumber((String) paymentData.get("referenceNumber"))
                .note((String) paymentData.get("note"))
                .status(Payment.PaymentStatus.COMPLETED)
                .receiptId(System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .build();

            Payment savedPayment = paymentRepository.save(payment);

            // Update invoice paid amount and status
            BigDecimal newPaidAmount = invoice.getPaidAmount().add(paymentAmount);
            invoice.setPaidAmount(newPaidAmount);
            
            if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
                invoice.setStatus(Invoice.InvoiceStatus.PAID);
            } else {
                invoice.setStatus(Invoice.InvoiceStatus.PARTIAL);
            }
            
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            log.info("Recorded payment of {} for invoice {}", paymentAmount, invoice.getInvoiceNumber());

            Map<String, Object> result = new HashMap<>();
            result.put("id", savedPayment.getId());
            result.put("amount", savedPayment.getAmount().longValue());
            result.put("paymentDate", savedPayment.getPaymentDate().toString());
            result.put("receiptId", savedPayment.getReceiptId());

            return result;

        } catch (Exception e) {
            log.error("Error recording payment for invoice {}", invoiceId, e);
            throw new RuntimeException("Không thể ghi nhận thanh toán: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> convertInvoiceToMap(Invoice invoice) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", invoice.getId());
        map.put("invoiceNumber", invoice.getInvoiceNumber());
        map.put("studentId", invoice.getStudentId());
        map.put("issueDate", invoice.getIssueDate().toString());
        map.put("dueDate", invoice.getDueDate().toString());
        map.put("totalAmount", invoice.getTotalAmount().longValue());
        map.put("paidAmount", invoice.getPaidAmount().longValue());
        map.put("status", invoice.getStatus().name());
        map.put("note", invoice.getNote());
        map.put("createdAt", invoice.getCreatedAt().toString());
        
        // Add student name if available
        if (invoice.getStudent() != null) {
            map.put("studentName", invoice.getStudent().getFullName());
        }
        
        // Add items if available
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            List<Map<String, Object>> items = invoice.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("description", item.getDescription());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice().longValue());
                    itemMap.put("amount", item.getAmount().longValue());
                    return itemMap;
                })
                .collect(Collectors.toList());
            map.put("items", items);
        }
        
        return map;
    }


    @Override
    public byte[] getInvoiceFile(Long invoiceId) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            if (invoice.getDocumentPath() == null) {
                throw new RuntimeException("Hóa đơn không có file đính kèm");
            }
            
            Path filePath = Paths.get(invoice.getDocumentPath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File hóa đơn không tồn tại");
            }
            
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            log.error("Error reading invoice file for invoice {}", invoiceId, e);
            throw new RuntimeException("Không thể đọc file hóa đơn");
        }
    }

    private String saveInvoiceFile(MultipartFile file, String invoiceNumber) {
        try {
            // Create uploads directory if not exists
            String uploadDir = "uploads/invoices/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".pdf";
            String filename = invoiceNumber + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("Saved invoice file: {}", filePath.toString());
            return filePath.toString();
            
        } catch (IOException e) {
            log.error("Error saving invoice file for invoice {}", invoiceNumber, e);
            throw new RuntimeException("Không thể lưu file hóa đơn");
        }
    }

    @Override
    public void notifyParentInvoiceStatusChange(Long studentId, String invoiceNumber, String oldStatus, String newStatus) {
        // This method will be implemented if needed
        // For now we use the BillingNotificationService for notifications
        log.info("Invoice {} status changed from {} to {} for student {}", invoiceNumber, oldStatus, newStatus, studentId);
    }

    private Map<String, Object> convertStudentToMap(User student) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", student.getId());
        map.put("username", student.getUsername());
        map.put("fullName", student.getFullName());
        map.put("email", student.getEmail());
        map.put("phone", student.getPhoneNumber());
        return map;
    }
}