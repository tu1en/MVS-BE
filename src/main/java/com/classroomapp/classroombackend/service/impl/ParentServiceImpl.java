package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.service.BillingService;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.model.TimetableEvent;
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
    private final TimetableEventRepository timetableEventRepository;
    private final BillingService billingService;

    @Autowired
    public ParentServiceImpl(ParentRepository parentRepository, 
                            StudentParentRepository studentParentRepository,
                            TimetableEventRepository timetableEventRepository,
                            BillingService billingService) {
        this.parentRepository = parentRepository;
        this.studentParentRepository = studentParentRepository;
        this.timetableEventRepository = timetableEventRepository;
        this.billingService = billingService;
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
        return studentParentRepository.findActiveRelationshipsWithDetailsForParent(parentId);
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
        List<Map<String, Object>> schedule = new ArrayList<>();
        
        try {
            // Query timetable events for the date range
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            List<TimetableEvent> events = timetableEventRepository.findEventsByDateRange(startDateTime, endDateTime);
            
            // Filter for class events (students see classes, not internal meetings)
            events = events.stream()
                .filter(event -> event.getEventType() == TimetableEvent.EventType.CLASS || 
                               event.getEventType() == TimetableEvent.EventType.ASSIGNMENT_DUE)
                .collect(java.util.stream.Collectors.toList());
            
            for (TimetableEvent event : events) {
                Map<String, Object> scheduleEvent = new HashMap<>();
                scheduleEvent.put("id", event.getId());
                scheduleEvent.put("date", event.getStartDatetime().toLocalDate().toString());
                scheduleEvent.put("startTime", event.getStartDatetime().toLocalTime().toString());
                scheduleEvent.put("endTime", event.getEndDatetime().toLocalTime().toString());
                scheduleEvent.put("title", event.getTitle());
                scheduleEvent.put("subject", extractSubjectFromTitle(event.getTitle()));
                scheduleEvent.put("teacher", "Giáo viên"); // Default teacher name
                scheduleEvent.put("classroom", event.getLocation() != null ? event.getLocation() : "Phòng học");
                scheduleEvent.put("type", event.getEventType() == TimetableEvent.EventType.CLASS ? "class" : "assignment");
                scheduleEvent.put("description", event.getDescription());
                
                schedule.add(scheduleEvent);
            }
            
            log.info("Retrieved {} schedule events for child {} from {} to {}", 
                    schedule.size(), childId, startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error retrieving schedule for child {}", childId, e);
        }
        
        return schedule;
    }
    
    /**
     * Extract subject name from event title
     */
    private String extractSubjectFromTitle(String title) {
        if (title == null) return "Môn học";
        
        // Extract subject from titles like "Toán 11A - Học kỳ 1"
        if (title.contains(" - ")) {
            return title.substring(0, title.indexOf(" - "));
        }
        
        // For titles like "Kiểm tra Toán học"
        if (title.startsWith("Kiểm tra ")) {
            return title.substring("Kiểm tra ".length());
        }
        
        return title;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getChildExams(Long childId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> exams = new ArrayList<>();
        
        try {
            // Query timetable events for exam events in the date range
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            List<TimetableEvent> events = timetableEventRepository.findEventsByDateRange(startDateTime, endDateTime);
            
            // Filter for exam events only
            events = events.stream()
                .filter(event -> event.getEventType() == TimetableEvent.EventType.EXAM)
                .collect(java.util.stream.Collectors.toList());
            
            for (TimetableEvent event : events) {
                Map<String, Object> exam = new HashMap<>();
                exam.put("id", event.getId());
                exam.put("examDate", event.getStartDatetime().toLocalDate().toString());
                exam.put("examTime", event.getStartDatetime().toLocalTime().toString());
                exam.put("examName", event.getTitle());
                exam.put("subject", extractSubjectFromTitle(event.getTitle()));
                exam.put("classroom", event.getLocation() != null ? event.getLocation() : "Phòng thi");
                exam.put("duration", calculateDurationInMinutes(event.getStartDatetime(), event.getEndDatetime()));
                exam.put("description", event.getDescription() != null ? event.getDescription() : "Bài kiểm tra");
                
                exams.add(exam);
            }
            
            log.info("Retrieved {} exam events for child {} from {} to {}", 
                    exams.size(), childId, startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error retrieving exams for child {}", childId, e);
        }
        
        return exams;
    }
    
    /**
     * Calculate duration between two LocalDateTime objects in minutes
     */
    private long calculateDurationInMinutes(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toMinutes();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getChildBillingData(Long childId, LocalDate startDate, LocalDate endDate) {
        try {
            // Use BillingService to get real billing data
            return billingService.getStudentBillingData(childId, startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error retrieving billing data for child {}", childId, e);
            
            // Return empty data structure on error
            Map<String, Object> emptyBillingData = new HashMap<>();
            Map<String, Object> emptySummary = new HashMap<>();
            emptySummary.put("totalDebt", 0L);
            emptySummary.put("totalPaid", 0L);
            emptySummary.put("unpaidInvoices", 0);
            emptySummary.put("overdueAmount", 0L);
            
            emptyBillingData.put("summary", emptySummary);
            emptyBillingData.put("invoices", new ArrayList<>());
            emptyBillingData.put("payments", new ArrayList<>());
            
            return emptyBillingData;
        }
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