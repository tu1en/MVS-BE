package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.Invoice;
import com.classroomapp.classroombackend.model.Payment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for billing operations
 */
public interface BillingService {

    /**
     * Get billing data for a student within date range
     */
    Map<String, Object> getStudentBillingData(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Get billing summary for a student
     */
    Map<String, Object> getStudentBillingSummary(Long studentId);

    /**
     * Get all invoices for a student
     */
    List<Invoice> getStudentInvoices(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Get all payments for a student
     */
    List<Payment> getStudentPayments(Long studentId, LocalDate startDate, LocalDate endDate);

    /**
     * Get invoice by ID
     */
    Invoice getInvoiceById(Long invoiceId);

    /**
     * Get payment by ID
     */
    Payment getPaymentById(Long paymentId);

    /**
     * Generate mock invoice/receipt PDF (placeholder)
     */
    byte[] generateInvoicePDF(Long invoiceId);

    /**
     * Generate mock receipt PDF (placeholder)
     */
    byte[] generateReceiptPDF(Long receiptId);
}