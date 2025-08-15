package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.request.CreateInvoiceDto;
import com.classroomapp.classroombackend.dto.request.UpdateInvoiceStatusDto;
import com.classroomapp.classroombackend.model.Invoice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Manager/Accountant billing operations
 */
public interface ManagerBillingService {

    /**
     * Get all invoices for management
     */
    List<Map<String, Object>> getAllInvoices();

    /**
     * Get all students for invoice creation
     */
    List<Map<String, Object>> getAllStudents();

    /**
     * Create new invoice with optional file upload
     */
    Map<String, Object> createInvoiceWithFile(String invoiceDataJson, MultipartFile invoiceFile);

    /**
     * Get invoice file data for download
     */
    byte[] getInvoiceFile(Long invoiceId);

    /**
     * Update invoice status and create payment record if status is PAID
     */
    Map<String, Object> updateInvoiceStatus(Long invoiceId, UpdateInvoiceStatusDto statusDto);

    /**
     * Delete invoice (only if PENDING)
     */
    void deleteInvoice(Long invoiceId);

    /**
     * Record manual payment for an invoice
     */
    Map<String, Object> recordPayment(Long invoiceId, Map<String, Object> paymentData);

    /**
     * Convert Invoice entity to Map for API response
     */
    Map<String, Object> convertInvoiceToMap(Invoice invoice);

    /**
     * Send notification to parent when invoice status changes
     */
    void notifyParentInvoiceStatusChange(Long studentId, String invoiceNumber, String oldStatus, String newStatus);
}