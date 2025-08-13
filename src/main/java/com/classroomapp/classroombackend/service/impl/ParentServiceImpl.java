package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.service.ParentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of ParentService
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@Service
@Slf4j
@Transactional
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;

    @Autowired
    public ParentServiceImpl(ParentRepository parentRepository, 
                            StudentParentRepository studentParentRepository) {
        this.parentRepository = parentRepository;
        this.studentParentRepository = studentParentRepository;
    }

    @Override
    public Parent createParent(Parent parent) {
        log.info("Creating new parent: {}", parent.getName());
        
        // Validate unique constraints
        if (parent.getEmail() != null && parentRepository.existsByEmail(parent.getEmail())) {
            throw new IllegalArgumentException("Parent with email already exists: " + parent.getEmail());
        }
        
        if (parent.getPhone() != null && parentRepository.existsByPhone(parent.getPhone())) {
            throw new IllegalArgumentException("Parent with phone already exists: " + parent.getPhone());
        }
        
        if (parentRepository.existsByUserId(parent.getUserId())) {
            throw new IllegalArgumentException("Parent already exists for user ID: " + parent.getUserId());
        }

        parent.setCreatedAt(LocalDateTime.now());
        parent.setUpdatedAt(LocalDateTime.now());
        
        Parent savedParent = parentRepository.save(parent);
        log.info("Created parent with ID: {}", savedParent.getId());
        
        return savedParent;
    }

    @Override
    public Parent updateParent(Long parentId, Parent parent) {
        log.info("Updating parent: {}", parentId);
        
        Parent existingParent = parentRepository.findById(parentId)
            .orElseThrow(() -> new IllegalArgumentException("Parent not found: " + parentId));

        // Update fields
        if (parent.getName() != null) {
            existingParent.setName(parent.getName());
        }
        if (parent.getPhone() != null) {
            existingParent.setPhone(parent.getPhone());
        }
        if (parent.getEmail() != null) {
            existingParent.setEmail(parent.getEmail());
        }
        if (parent.getStatus() != null) {
            existingParent.setStatus(parent.getStatus());
        }
        
        existingParent.setUpdatedAt(LocalDateTime.now());
        
        Parent updatedParent = parentRepository.save(existingParent);
        log.info("Updated parent: {}", updatedParent.getId());
        
        return updatedParent;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Parent> getParentById(Long parentId) {
        return parentRepository.findById(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Parent> getParentByUserId(Long userId) {
        return parentRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Parent> getParentByEmail(String email) {
        return parentRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentParent> getChildrenByParentId(Long parentId) {
        return studentParentRepository.findActiveChildrenByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getChildIdsByParentId(Long parentId) {
        return studentParentRepository.findStudentIdsByParentId(parentId);
    }

    @Override
    public StudentParent linkParentToStudent(Long parentId, Long studentId, 
                                           StudentParent.RelationType relationType, 
                                           Boolean isPrimary, Boolean legalGuardian) {
        log.info("Linking parent {} to student {} with relation {}", parentId, studentId, relationType);
        
        // Check if relationship already exists
        if (studentParentRepository.existsActiveRelationship(parentId, studentId)) {
            throw new IllegalArgumentException("Active relationship already exists between parent and student");
        }
        
        // Validate parent exists
        if (!parentRepository.existsById(parentId)) {
            throw new IllegalArgumentException("Parent not found: " + parentId);
        }
        
        StudentParent relationship = new StudentParent(parentId, studentId, relationType, 
                                                      isPrimary, legalGuardian);
        
        StudentParent savedRelationship = studentParentRepository.save(relationship);
        log.info("Created parent-student relationship: {}", savedRelationship.getId());
        
        return savedRelationship;
    }

    @Override
    public void unlinkParentFromStudent(Long parentId, Long studentId) {
        log.info("Unlinking parent {} from student {}", parentId, studentId);
        
        Optional<StudentParent> relationship = studentParentRepository
            .findActiveRelationship(parentId, studentId);
        
        if (relationship.isPresent()) {
            StudentParent rel = relationship.get();
            rel.terminate();
            studentParentRepository.save(rel);
            log.info("Terminated parent-student relationship: {}", rel.getId());
        } else {
            throw new IllegalArgumentException("Active relationship not found between parent and student");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToStudent(Long parentId, Long studentId) {
        return studentParentRepository.existsActiveRelationship(parentId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parent> getParentsByStudentId(Long studentId) {
        return parentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Parent> getPrimaryParentByStudentId(Long studentId) {
        return parentRepository.findPrimaryParentByStudentId(studentId);
    }

    @Override
    public Parent updateParentStatus(Long parentId, Parent.ParentStatus status) {
        log.info("Updating parent {} status to {}", parentId, status);
        
        Parent parent = parentRepository.findById(parentId)
            .orElseThrow(() -> new IllegalArgumentException("Parent not found: " + parentId));
        
        parent.setStatus(status);
        parent.setUpdatedAt(LocalDateTime.now());
        
        Parent updatedParent = parentRepository.save(parent);
        log.info("Updated parent status: {}", updatedParent.getId());
        
        return updatedParent;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parent> getActiveParents() {
        return parentRepository.findByStatus(Parent.ParentStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parent> searchParentsByName(String name) {
        return parentRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countChildrenByParentId(Long parentId) {
        return studentParentRepository.countActiveChildrenByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateParentStudentRelationship(Long parentId, Long studentId) {
        return studentParentRepository.existsActiveRelationship(parentId, studentId);
    }

    @Override
    public Parent createParentFromUser(Long userId, String name, String phone, String email) {
        log.info("Creating parent from user registration: {}", userId);
        
        Parent parent = new Parent(userId, name, phone, email);
        return createParent(parent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Parent> getParentsWithPendingLeaveNotices() {
        return parentRepository.findParentsWithPendingLeaveNotices();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getChildSchedule(Long childId, LocalDate startDate, LocalDate endDate) {
        // This would typically integrate with your existing schedule/timetable system
        // For now, returning sample data - replace with actual implementation
        List<Map<String, Object>> schedule = new ArrayList<>();
        
        try {
            // Query your schedule/timetable tables here
            // Example implementation - replace with your actual data access logic
            
            // Sample schedule event
            Map<String, Object> event = new HashMap<>();
            event.put("id", 1L);
            event.put("date", startDate.toString());
            event.put("startTime", "08:00");
            event.put("endTime", "09:30");
            event.put("subject", "Toán học");
            event.put("title", "Toán học - Đại số");
            event.put("teacher", "Cô Nguyễn Thị A");
            event.put("classroom", "Phòng 101");
            event.put("type", "class");
            
            schedule.add(event);
            
            log.info("Retrieved {} schedule events for child {} from {} to {}", 
                    schedule.size(), childId, startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error retrieving schedule for child {}", childId, e);
        }
        
        return schedule;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getChildExams(Long childId, LocalDate startDate, LocalDate endDate) {
        // This would typically integrate with your existing exam system
        // For now, returning sample data - replace with actual implementation
        List<Map<String, Object>> exams = new ArrayList<>();
        
        try {
            // Query your exam tables here
            // Example implementation - replace with your actual data access logic
            
            // Sample exam event
            Map<String, Object> exam = new HashMap<>();
            exam.put("id", 1L);
            exam.put("examDate", startDate.plusDays(3).toString());
            exam.put("examTime", "14:00");
            exam.put("examName", "Kiểm tra giữa kỳ");
            exam.put("subject", "Toán học");
            exam.put("classroom", "Phòng 201");
            exam.put("duration", 90); // minutes
            exam.put("description", "Kiểm tra chương 1-3");
            
            exams.add(exam);
            
            log.info("Retrieved {} exam events for child {} from {} to {}", 
                    exams.size(), childId, startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error retrieving exams for child {}", childId, e);
        }
        
        return exams;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getChildBillingData(Long childId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> billingData = new HashMap<>();
        
        try {
            // This would typically integrate with your existing billing system
            // For now, returning sample data - replace with actual implementation
            
            // Summary data
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDebt", 2500000L); // VND
            summary.put("totalPaid", 7500000L);
            summary.put("unpaidInvoices", 2L);
            summary.put("overdueAmount", 1000000L);
            
            // Sample invoices
            List<Map<String, Object>> invoices = new ArrayList<>();
            Map<String, Object> invoice = new HashMap<>();
            invoice.put("id", 1L);
            invoice.put("invoiceNumber", "INV-2024-001");
            invoice.put("issueDate", "2024-01-15");
            invoice.put("dueDate", "2024-02-15");
            invoice.put("totalAmount", 2500000L);
            invoice.put("paidAmount", 0L);
            invoice.put("status", "PENDING");
            
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("description", "Học phí tháng 1/2024");
            item.put("quantity", 1);
            item.put("unitPrice", 2500000L);
            item.put("amount", 2500000L);
            items.add(item);
            invoice.put("items", items);
            
            invoices.add(invoice);
            
            // Sample payments
            List<Map<String, Object>> payments = new ArrayList<>();
            Map<String, Object> payment = new HashMap<>();
            payment.put("id", 1L);
            payment.put("paymentDate", "2024-01-10T10:30:00");
            payment.put("invoiceNumber", "INV-2023-012");
            payment.put("amount", 2500000L);
            payment.put("paymentMethod", "BANK_TRANSFER");
            payment.put("note", "Thanh toán học phí tháng 12/2023");
            payment.put("receiptId", 1L);
            payments.add(payment);
            
            billingData.put("summary", summary);
            billingData.put("invoices", invoices);
            billingData.put("payments", payments);
            
            log.info("Retrieved billing data for child {} from {} to {}", 
                    childId, startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error retrieving billing data for child {}", childId, e);
        }
        
        return billingData;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChildBillingAccess(Long parentId, Long childId) {
        try {
            // Check if parent has access to this child
            if (!hasAccessToStudent(parentId, childId)) {
                return false;
            }
            
            // Check specific billing access (from parent_billing_access table)
            // For now, return true if parent has access to child
            // In production, you would query the parent_billing_access table
            return true;
            
        } catch (Exception e) {
            log.error("Error checking billing access for parent {} and child {}", parentId, childId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getBillingDocument(Long parentId, Long documentId, String type) {
        try {
            // This would typically:
            // 1. Validate parent has access to this document
            // 2. Retrieve document from file system or generate PDF
            // 3. Return as byte array
            
            // For now, returning sample PDF content
            // In production, you would integrate with your document storage system
            String sampleContent = "Sample " + type + " document content for document " + documentId;
            return sampleContent.getBytes();
            
        } catch (Exception e) {
            log.error("Error retrieving billing document {} of type {} for parent {}", 
                    documentId, type, parentId, e);
            throw new IllegalArgumentException("Document not found or access denied");
        }
    }
}