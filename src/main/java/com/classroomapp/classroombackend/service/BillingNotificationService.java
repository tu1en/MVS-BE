package com.classroomapp.classroombackend.service;

/**
 * Service for sending billing-related notifications to parents
 */
public interface BillingNotificationService {

    /**
     * Send notification when new invoice is created
     */
    void notifyInvoiceCreated(Long studentId, String invoiceNumber, Long totalAmount);

    /**
     * Send notification when invoice status changes to PAID
     */
    void notifyInvoicePaid(Long studentId, String invoiceNumber, Long paidAmount);

    /**
     * Send notification when invoice becomes overdue
     */
    void notifyInvoiceOverdue(Long studentId, String invoiceNumber, Long overdueAmount);

    /**
     * Send notification when partial payment is made
     */
    void notifyPartialPayment(Long studentId, String invoiceNumber, Long paidAmount, Long remainingAmount);
}