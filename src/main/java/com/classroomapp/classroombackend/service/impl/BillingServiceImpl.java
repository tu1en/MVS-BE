package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.Invoice;
import com.classroomapp.classroombackend.model.InvoiceItem;
import com.classroomapp.classroombackend.model.Payment;
import com.classroomapp.classroombackend.repository.InvoiceRepository;
import com.classroomapp.classroombackend.repository.PaymentRepository;
import com.classroomapp.classroombackend.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Map<String, Object> getStudentBillingData(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting billing data for student {} from {} to {}", studentId, startDate, endDate);
        
        try {
            Map<String, Object> billingData = new HashMap<>();
            
            // Get billing summary
            Map<String, Object> summary = getStudentBillingSummary(studentId);
            billingData.put("summary", summary);
            
            // Get invoices
            List<Invoice> invoices = getStudentInvoices(studentId, startDate, endDate);
            List<Map<String, Object>> invoiceData = invoices.stream()
                .map(this::convertInvoiceToMap)
                .collect(Collectors.toList());
            billingData.put("invoices", invoiceData);
            
            // Get payments
            List<Payment> payments = getStudentPayments(studentId, startDate, endDate);
            List<Map<String, Object>> paymentData = payments.stream()
                .map(this::convertPaymentToMap)
                .collect(Collectors.toList());
            billingData.put("payments", paymentData);
            
            log.info("Retrieved billing data: {} invoices, {} payments", invoices.size(), payments.size());
            return billingData;
            
        } catch (Exception e) {
            log.error("Error retrieving billing data for student {}", studentId, e);
            throw new RuntimeException("Cannot retrieve billing data", e);
        }
    }

    @Override
    public Map<String, Object> getStudentBillingSummary(Long studentId) {
        try {
            Object[] summary = invoiceRepository.getBillingSummaryByStudentId(studentId);
            
            Map<String, Object> summaryMap = new HashMap<>();
            if (summary != null && summary.length >= 4) {
                summaryMap.put("totalDebt", summary[0] != null ? ((BigDecimal) summary[0]).longValue() : 0L);
                summaryMap.put("totalPaid", summary[1] != null ? ((BigDecimal) summary[1]).longValue() : 0L);
                summaryMap.put("unpaidInvoices", summary[2] != null ? ((Long) summary[2]).intValue() : 0);
                summaryMap.put("overdueAmount", summary[3] != null ? ((BigDecimal) summary[3]).longValue() : 0L);
            } else {
                // Default values if no data
                summaryMap.put("totalDebt", 0L);
                summaryMap.put("totalPaid", 0L);
                summaryMap.put("unpaidInvoices", 0);
                summaryMap.put("overdueAmount", 0L);
            }
            
            return summaryMap;
            
        } catch (Exception e) {
            log.error("Error getting billing summary for student {}", studentId, e);
            // Return default values on error
            Map<String, Object> defaultSummary = new HashMap<>();
            defaultSummary.put("totalDebt", 0L);
            defaultSummary.put("totalPaid", 0L);
            defaultSummary.put("unpaidInvoices", 0);
            defaultSummary.put("overdueAmount", 0L);
            return defaultSummary;
        }
    }

    @Override
    public List<Invoice> getStudentInvoices(Long studentId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return invoiceRepository.findByStudentIdAndDateRange(studentId, startDate, endDate);
        } else {
            return invoiceRepository.findByStudentIdOrderByIssueDateDesc(studentId);
        }
    }

    @Override
    public List<Payment> getStudentPayments(Long studentId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            return paymentRepository.findByStudentIdAndDateRange(studentId, startDateTime, endDateTime);
        } else {
            return paymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId);
        }
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
    }

    @Override
    public byte[] generateInvoicePDF(Long invoiceId) {
        // Placeholder implementation - in real system would generate PDF
        log.info("Generating invoice PDF for invoice ID: {}", invoiceId);
        String content = "Mock Invoice PDF Content for Invoice ID: " + invoiceId;
        return content.getBytes();
    }

    @Override
    public byte[] generateReceiptPDF(Long receiptId) {
        // Placeholder implementation - in real system would generate PDF
        log.info("Generating receipt PDF for receipt ID: {}", receiptId);
        String content = "Mock Receipt PDF Content for Receipt ID: " + receiptId;
        return content.getBytes();
    }

    /**
     * Convert Invoice entity to Map for API response
     */
    private Map<String, Object> convertInvoiceToMap(Invoice invoice) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", invoice.getId());
        map.put("invoiceNumber", invoice.getInvoiceNumber());
        map.put("issueDate", invoice.getIssueDate().toString());
        map.put("dueDate", invoice.getDueDate().toString());
        map.put("totalAmount", invoice.getTotalAmount().longValue());
        map.put("paidAmount", invoice.getPaidAmount().longValue());
        map.put("status", invoice.getStatus().name());
        map.put("note", invoice.getNote());
        
        // Add items if available
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            List<Map<String, Object>> items = invoice.getItems().stream()
                .map(this::convertInvoiceItemToMap)
                .collect(Collectors.toList());
            map.put("items", items);
        }
        
        return map;
    }

    /**
     * Convert InvoiceItem entity to Map for API response
     */
    private Map<String, Object> convertInvoiceItemToMap(InvoiceItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("description", item.getDescription());
        map.put("quantity", item.getQuantity());
        map.put("unitPrice", item.getUnitPrice().longValue());
        map.put("amount", item.getAmount().longValue());
        return map;
    }

    /**
     * Convert Payment entity to Map for API response
     */
    private Map<String, Object> convertPaymentToMap(Payment payment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", payment.getId());
        map.put("paymentDate", payment.getPaymentDate().toString());
        map.put("amount", payment.getAmount().longValue());
        map.put("paymentMethod", payment.getPaymentMethod().name());
        map.put("referenceNumber", payment.getReferenceNumber());
        map.put("note", payment.getNote());
        map.put("status", payment.getStatus().name());
        map.put("receiptId", payment.getReceiptId());
        
        // Add invoice number if available
        if (payment.getInvoice() != null) {
            map.put("invoiceNumber", payment.getInvoice().getInvoiceNumber());
        }
        
        return map;
    }
}