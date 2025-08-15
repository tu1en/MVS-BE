package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.request.CreateInvoiceDto;
import com.classroomapp.classroombackend.dto.request.UpdateInvoiceStatusDto;
import com.classroomapp.classroombackend.model.Invoice;
import com.classroomapp.classroombackend.service.BillingService;
import com.classroomapp.classroombackend.service.ManagerBillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * Controller for Manager/Accountant billing operations
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ManagerBillingController {

    private final ManagerBillingService managerBillingService;
    private final BillingService billingService;

    /**
     * Get all invoices for management
     */
    @GetMapping("/invoices")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<List<Map<String, Object>>> getAllInvoices() {
        try {
            log.info("Manager/Accountant retrieving all invoices");
            List<Map<String, Object>> invoices = managerBillingService.getAllInvoices();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            log.error("Error retrieving invoices", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all students for invoice creation
     */
    @GetMapping("/students")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<List<Map<String, Object>>> getAllStudents() {
        try {
            log.info("Manager/Accountant retrieving all students");
            List<Map<String, Object>> students = managerBillingService.getAllStudents();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error retrieving students", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new invoice with optional file upload
     */
    @PostMapping("/invoices")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> createInvoice(
            @RequestParam("invoiceData") String invoiceDataJson,
            @RequestParam(value = "invoiceFile", required = false) MultipartFile invoiceFile) {
        try {
            log.info("Manager/Accountant creating invoice with data: {}", invoiceDataJson);
            
            Map<String, Object> createdInvoice = managerBillingService.createInvoiceWithFile(invoiceDataJson, invoiceFile);
            return ResponseEntity.ok(createdInvoice);
        } catch (Exception e) {
            log.error("Error creating invoice", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Download invoice file
     */
    @GetMapping("/invoices/{invoiceId}/download")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<byte[]> downloadInvoiceFile(@PathVariable Long invoiceId) {
        try {
            log.info("Manager/Accountant downloading invoice file: {}", invoiceId);
            byte[] fileData = managerBillingService.getInvoiceFile(invoiceId);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=invoice_" + invoiceId + ".pdf")
                .body(fileData);
        } catch (Exception e) {
            log.error("Error downloading invoice file {}", invoiceId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update invoice status (e.g., from PENDING to PAID)
     */
    @PutMapping("/invoices/{invoiceId}/status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> updateInvoiceStatus(
            @PathVariable Long invoiceId,
            @Valid @RequestBody UpdateInvoiceStatusDto statusDto) {
        try {
            log.info("Manager/Accountant updating invoice {} status to {}", invoiceId, statusDto.getStatus());
            Map<String, Object> updatedInvoice = managerBillingService.updateInvoiceStatus(invoiceId, statusDto);
            return ResponseEntity.ok(updatedInvoice);
        } catch (Exception e) {
            log.error("Error updating invoice status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get invoice details by ID
     */
    @GetMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> getInvoiceById(@PathVariable Long invoiceId) {
        try {
            log.info("Manager/Accountant retrieving invoice: {}", invoiceId);
            Invoice invoice = billingService.getInvoiceById(invoiceId);
            return ResponseEntity.ok(managerBillingService.convertInvoiceToMap(invoice));
        } catch (Exception e) {
            log.error("Error retrieving invoice {}", invoiceId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete invoice (only if PENDING status)
     */
    @DeleteMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long invoiceId) {
        try {
            log.info("Manager/Accountant deleting invoice: {}", invoiceId);
            managerBillingService.deleteInvoice(invoiceId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting invoice {}", invoiceId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Record manual payment for an invoice
     */
    @PostMapping("/invoices/{invoiceId}/payments")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Map<String, Object>> recordPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody Map<String, Object> paymentData) {
        try {
            log.info("Manager/Accountant recording payment for invoice: {}", invoiceId);
            Map<String, Object> payment = managerBillingService.recordPayment(invoiceId, paymentData);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Error recording payment for invoice {}", invoiceId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}