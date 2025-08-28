package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.AssignmentDto;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service để test hiệu năng và so sánh giữa các method
 * Giúp debug N+1 query problem
 */
@Service
@Slf4j
public class PerformanceTestService {

    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;

    /**
     * Test method cũ (có thể gây N+1 query)
     */
    @Transactional(readOnly = true)
    public void testOldMethod(Long classroomId) {
        log.info("=== TESTING OLD METHOD (Potential N+1) ===");
        long startTime = System.currentTimeMillis();
        
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        
        // Sử dụng method cũ có thể gây N+1
        List<AssignmentDto> assignments = assignmentRepository.findByClassroomOrderByDueDateAsc(classroom)
            .stream()
            .map(assignment -> {
                // Trigger lazy loading cho attachments
                if (assignment.getAttachments() != null) {
                    assignment.getAttachments().size();
                }
                return new AssignmentDto(); // Simplified mapping
            })
            .toList();
        
        long endTime = System.currentTimeMillis();
        log.info("Old method completed in {} ms, found {} assignments", 
            endTime - startTime, assignments.size());
    }

    /**
     * Test method mới (đã optimize với JOIN FETCH)
     */
    @Transactional(readOnly = true)
    public void testNewMethod(Long classroomId) {
        log.info("=== TESTING NEW METHOD (Optimized with JOIN FETCH) ===");
        long startTime = System.currentTimeMillis();
        
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        
        // Sử dụng method mới đã optimize
        List<AssignmentDto> assignments = assignmentRepository
            .findByClassroomWithAttachmentsAndClassroomOrderByDueDateAsc(classroom)
            .stream()
            .map(assignment -> {
                // Attachments đã được fetch sẵn, không cần lazy loading
                if (assignment.getAttachments() != null) {
                    assignment.getAttachments().size();
                }
                return new AssignmentDto(); // Simplified mapping
            })
            .toList();
        
        long endTime = System.currentTimeMillis();
        log.info("New method completed in {} ms, found {} assignments", 
            endTime - startTime, assignments.size());
    }

    /**
     * Test method upcoming assignments
     */
    @Transactional(readOnly = true)
    public void testUpcomingAssignments(Long classroomId) {
        log.info("=== TESTING UPCOMING ASSIGNMENTS ===");
        long startTime = System.currentTimeMillis();
        
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        
        List<AssignmentDto> assignments = assignmentRepository
            .findByClassroomAndDueDateAfterWithAttachmentsAndClassroomOrderByDueDateAsc(classroom, LocalDateTime.now())
            .stream()
            .map(assignment -> new AssignmentDto())
            .toList();
        
        long endTime = System.currentTimeMillis();
        log.info("Upcoming assignments completed in {} ms, found {} assignments", 
            endTime - startTime, assignments.size());
    }

    /**
     * Test method past assignments
     */
    @Transactional(readOnly = true)
    public void testPastAssignments(Long classroomId) {
        log.info("=== TESTING PAST ASSIGNMENTS ===");
        long startTime = System.currentTimeMillis();
        
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        
        List<AssignmentDto> assignments = assignmentRepository
            .findByClassroomAndDueDateBeforeWithAttachmentsAndClassroomOrderByDueDateDesc(classroom, LocalDateTime.now())
            .stream()
            .map(assignment -> new AssignmentDto())
            .toList();
        
        long endTime = System.currentTimeMillis();
        log.info("Past assignments completed in {} ms, found {} assignments", 
            endTime - startTime, assignments.size());
    }

    /**
     * Run all performance tests
     */
    public void runAllPerformanceTests(Long classroomId) {
        log.info("🚀 Starting Performance Tests for Classroom ID: {}", classroomId);
        
        try {
            testOldMethod(classroomId);
            testNewMethod(classroomId);
            testUpcomingAssignments(classroomId);
            testPastAssignments(classroomId);
            
            log.info("✅ All performance tests completed successfully!");
        } catch (Exception e) {
            log.error("❌ Performance test failed: {}", e.getMessage(), e);
        }
    }
}
