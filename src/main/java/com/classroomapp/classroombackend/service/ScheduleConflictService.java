package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.entity.ScheduleConflict;
import com.classroomapp.classroombackend.repository.ScheduleConflictRepository;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService.ConflictType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleConflictService {
    
    private final ScheduleConflictRepository scheduleConflictRepository;
    
    public List<ScheduleConflict> getAllConflicts() {
        return scheduleConflictRepository.findAll();
    }
    
    public List<ScheduleConflict> getUnresolvedConflicts() {
        return scheduleConflictRepository.findByResolvedFalseOrderByDetectedAtDesc();
    }
    
    public ScheduleConflict saveConflict(ScheduleConflict conflict) {
        return scheduleConflictRepository.save(conflict);
    }
    
    public ScheduleConflict createConflict(Long classId, String className, String schedule, 
                                         ConflictType conflictType, String details) {
        ScheduleConflict conflict = new ScheduleConflict(classId, className, schedule, conflictType, details);
        return scheduleConflictRepository.save(conflict);
    }
    
    public ScheduleConflict createConflict(Long classId, String className, String schedule, 
                                         ConflictType conflictType, String details, 
                                         LocalDate startDate, LocalDate endDate, 
                                         String teacherName, String roomName) {
        ScheduleConflict conflict = new ScheduleConflict(classId, className, schedule, conflictType, details, 
                                                       startDate, endDate, teacherName, roomName);
        return scheduleConflictRepository.save(conflict);
    }
    
    public void resolveConflict(Long conflictId, String resolutionNotes) {
        scheduleConflictRepository.findById(conflictId).ifPresent(conflict -> {
            conflict.setResolved(true);
            conflict.setResolvedAt(java.time.LocalDateTime.now());
            conflict.setResolutionNotes(resolutionNotes);
            scheduleConflictRepository.save(conflict);
            log.info("Resolved conflict with ID: {}", conflictId);
        });
    }
    
    public void deleteConflict(Long conflictId) {
        scheduleConflictRepository.deleteById(conflictId);
        log.info("Deleted conflict with ID: {}", conflictId);
    }
    
    // ✅ FIX: Sửa thứ tự tham số để match với ClassController
    public List<ScheduleConflict> checkScheduleConflicts(Long roomId, Long teacherId, String schedule, 
                                                        LocalDate startDate, LocalDate endDate) {
        try {
            // Log để debug
            log.info("Checking schedule conflicts - Room: {}, Teacher: {}, Period: {} to {}", 
                    roomId, teacherId, startDate, endDate);
            
            // Implementation for checking schedule conflicts
            List<ScheduleConflict> conflicts = scheduleConflictRepository.findByDetectedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
                
            // Additional logic để check conflicts thực tế có thể thêm ở đây
            // Ví dụ: check room conflicts, teacher conflicts, etc.
            
            log.info("Found {} schedule conflicts between {} and {}", conflicts.size(), startDate, endDate);
            return conflicts;
            
        } catch (Exception e) {
            log.error("Error checking schedule conflicts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check schedule conflicts: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getRoomAvailabilitySummary(Long roomId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();
        List<ScheduleConflict> roomConflicts = scheduleConflictRepository.findUnresolvedConflictsByRoom(roomId);
        summary.put("totalConflicts", roomConflicts.size());
        summary.put("conflicts", roomConflicts);
        summary.put("roomId", roomId);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        log.info("Room {} availability summary: {} conflicts", roomId, roomConflicts.size());
        return summary;
    }
    
    public Map<String, Object> getTeacherAvailabilitySummary(Long teacherId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();
        List<ScheduleConflict> teacherConflicts = scheduleConflictRepository.findUnresolvedConflictsByTeacher(teacherId);
        summary.put("totalConflicts", teacherConflicts.size());
        summary.put("conflicts", teacherConflicts);
        summary.put("teacherId", teacherId);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        log.info("Teacher {} availability summary: {} conflicts", teacherId, teacherConflicts.size());
        return summary;
    }
    
    public Map<String, Object> findOptimalSlot(Long roomId, Long teacherId, List<String> preferredDays, 
                                              String startTime, String endTime, 
                                              LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // Simple implementation - find first available slot
        List<ScheduleConflict> teacherConflicts = scheduleConflictRepository.findUnresolvedConflictsByTeacher(teacherId);
        List<ScheduleConflict> roomConflicts = scheduleConflictRepository.findUnresolvedConflictsByRoom(roomId);
        
        result.put("teacherConflicts", teacherConflicts.size());
        result.put("roomConflicts", roomConflicts.size());
        result.put("hasConflicts", !teacherConflicts.isEmpty() || !roomConflicts.isEmpty());
        result.put("suggestedDate", startDate);
        result.put("suggestedTime", startTime + " - " + endTime);
        
        log.info("Found optimal slot for room {} and teacher {}: {} conflicts total", 
                roomId, teacherId, teacherConflicts.size() + roomConflicts.size());
        
        return result;
    }
}