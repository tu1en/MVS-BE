package com.classroomapp.classroombackend.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.entity.ScheduleConflict;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.repository.ScheduleConflictRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService.ConflictType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleConflictService {
    
    private final ScheduleConflictRepository scheduleConflictRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassRepository classRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
            
            // Existing placeholder fetch (kept for backward compatibility)
            List<ScheduleConflict> conflicts = scheduleConflictRepository.findByDetectedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

            // 1) Xung đột giáo viên theo slot mới
            if (teacherId != null && schedule != null) {
                List<ClassEntity> teacherClasses = classRepository.findConflictingClassesByTeacher(teacherId, startDate, endDate);
                conflicts.addAll(detectOverlapsWithClasses("Giáo viên bận lịch khác", schedule, teacherClasses));
            }

            // Gợi ý: để kiểm tra học sinh chính xác, dùng overload có classId
            
            log.info("Found {} schedule conflicts between {} and {}", conflicts.size(), startDate, endDate);
            return conflicts;
            
        } catch (Exception e) {
            log.error("Error checking schedule conflicts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check schedule conflicts: " + e.getMessage());
        }
    }

    /**
     * Overload: có classId để kiểm tra xung đột học sinh của lớp hiện tại
     */
    public List<ScheduleConflict> checkScheduleConflicts(Long classId, Long roomId, Long teacherId, String schedule,
                                                         LocalDate startDate, LocalDate endDate) {
        List<ScheduleConflict> conflicts = checkScheduleConflicts(roomId, teacherId, schedule, startDate, endDate);
        try {
            if (classId == null || schedule == null || schedule.isBlank()) return conflicts;

            ClassEntity current = classRepository.findById(classId).orElse(null);
            if (current == null || current.getClassName() == null) return conflicts;

            // Tìm Classroom tương ứng theo tên lớp
            var classroomOpt = classroomRepository.findByName(current.getClassName());
            if (classroomOpt.isEmpty()) return conflicts;
            Long classroomId = classroomOpt.get().getId();

            // Lấy danh sách học viên của lớp
            var enrollments = classroomEnrollmentRepository.findByClassroomId(classroomId);
            if (enrollments == null || enrollments.isEmpty()) return conflicts;

            // Parse schedule mới 1 lần
            JsonNode newNode = objectMapper.readTree(schedule);
            java.time.LocalTime newStart = newNode.has("startTime") ? java.time.LocalTime.parse(newNode.get("startTime").asText()) : null;
            java.time.LocalTime newEnd = newNode.has("endTime") ? java.time.LocalTime.parse(newNode.get("endTime").asText()) : null;
            java.util.Set<Integer> newDays = new java.util.HashSet<>();
            if (newNode.has("days") && newNode.get("days").isArray()) {
                for (JsonNode d : newNode.get("days")) newDays.add(mapDayStringToIndex(d.asText()));
            }
            if (newStart == null || newEnd == null || newDays.isEmpty()) return conflicts;

            for (var enr : enrollments) {
                Long studentId = enr.getUser() != null ? enr.getUser().getId() : null;
                if (studentId == null) continue;
                var otherClassrooms = classroomRepository.findClassroomsByStudentId(studentId);
                if (otherClassrooms == null) continue;
                for (var oc : otherClassrooms) {
                    if (oc.getId().equals(classroomId)) continue; // chính lớp hiện tại
                    // Map sang ClassEntity theo tên classroom
                    var otherClassEntityOpt = classRepository.findByClassName(oc.getName());
                    if (otherClassEntityOpt.isEmpty()) continue;
                    ClassEntity ocEntity = otherClassEntityOpt.get();
                    // Overlap date range
                    if (ocEntity.getEndDate() != null && ocEntity.getEndDate().isBefore(startDate)) continue;
                    if (ocEntity.getStartDate() != null && ocEntity.getStartDate().isAfter(endDate)) continue;
                    // Overlap schedule by days and time
                    if (ocEntity.getScheduleJson() == null) continue;
                    if (isScheduleOverlap(schedule, ocEntity.getScheduleJson())) {
                        String detail = "Học viên #" + studentId + " trùng lịch với lớp '" + ocEntity.getClassName() + "'";
                        conflicts.add(new ScheduleConflict(ocEntity.getId(), ocEntity.getClassName(), ocEntity.getScheduleJson(),
                                ConflictType.TIME_OVERLAP, detail));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Student conflict check error: {}", e.getMessage());
        }
        return conflicts;
    }

    private boolean isScheduleOverlap(String scheduleA, String scheduleB) {
        if (scheduleA == null || scheduleB == null || scheduleA.isBlank() || scheduleB.isBlank()) return false;
        try {
            JsonNode a = objectMapper.readTree(scheduleA);
            JsonNode b = objectMapper.readTree(scheduleB);
            java.time.LocalTime aStart = a.has("startTime") ? java.time.LocalTime.parse(a.get("startTime").asText()) : null;
            java.time.LocalTime aEnd = a.has("endTime") ? java.time.LocalTime.parse(a.get("endTime").asText()) : null;
            java.util.Set<Integer> aDays = new java.util.HashSet<>();
            if (a.has("days") && a.get("days").isArray()) for (JsonNode d : a.get("days")) aDays.add(mapDayStringToIndex(d.asText()));

            java.time.LocalTime bStart = b.has("startTime") ? java.time.LocalTime.parse(b.get("startTime").asText()) : null;
            java.time.LocalTime bEnd = b.has("endTime") ? java.time.LocalTime.parse(b.get("endTime").asText()) : null;
            java.util.Set<Integer> bDays = new java.util.HashSet<>();
            if (b.has("days") && b.get("days").isArray()) for (JsonNode d : b.get("days")) bDays.add(mapDayStringToIndex(d.asText()));

            if (aStart == null || aEnd == null || bStart == null || bEnd == null || aDays.isEmpty() || bDays.isEmpty()) return false;
            boolean dayOverlap = aDays.stream().anyMatch(bDays::contains);
            boolean timeOverlap = !(aEnd.isBefore(bStart) || !aStart.isBefore(bEnd));
            return dayOverlap && timeOverlap;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper: so sánh slot JSON schedule với danh sách lớp
    private List<ScheduleConflict> detectOverlapsWithClasses(String reason, String scheduleJson, List<ClassEntity> classes) {
        List<ScheduleConflict> results = new java.util.ArrayList<>();
        if (scheduleJson == null || scheduleJson.isBlank() || classes == null || classes.isEmpty()) return results;
        try {
            JsonNode nodeNew = objectMapper.readTree(scheduleJson);
            java.time.LocalTime newStart = nodeNew.has("startTime") ? java.time.LocalTime.parse(nodeNew.get("startTime").asText()) : null;
            java.time.LocalTime newEnd = nodeNew.has("endTime") ? java.time.LocalTime.parse(nodeNew.get("endTime").asText()) : null;
            java.util.Set<Integer> dayIndexes = new HashSet<>();
            if (nodeNew.has("days") && nodeNew.get("days").isArray()) {
                for (JsonNode d : nodeNew.get("days")) dayIndexes.add(mapDayStringToIndex(d.asText()));
            }
            if (newStart == null || newEnd == null || dayIndexes.isEmpty()) return results;

            for (ClassEntity c : classes) {
                if (c.getScheduleJson() == null) continue;
                try {
                    JsonNode cs = objectMapper.readTree(c.getScheduleJson());
                    java.time.LocalTime st = cs.has("startTime") ? java.time.LocalTime.parse(cs.get("startTime").asText()) : null;
                    java.time.LocalTime en = cs.has("endTime") ? java.time.LocalTime.parse(cs.get("endTime").asText()) : null;
                    java.util.Set<Integer> cd = new HashSet<>();
                    if (cs.has("days") && cs.get("days").isArray()) {
                        for (JsonNode d : cs.get("days")) cd.add(mapDayStringToIndex(d.asText()));
                    }
                    boolean dayOverlap = cd.stream().anyMatch(dayIndexes::contains);
                    boolean timeOverlap = (st != null && en != null) && !(en.isBefore(newStart) || !st.isBefore(newEnd));
                    if (dayOverlap && timeOverlap) {
                        ScheduleConflict sc = new ScheduleConflict(
                            c.getId(), c.getClassName(), c.getScheduleJson(), ConflictType.TIME_OVERLAP, reason
                        );
                        results.add(sc);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return results;
    }

    private int mapDayStringToIndex(String day) {
        if (day == null) return 0;
        String d = day.trim().toLowerCase();
        switch (d) {
            case "mon": case "monday": case "thu2": case "monday_vi": return 0;
            case "tue": case "tuesday": case "thu3": return 1;
            case "wed": case "wednesday": case "thu4": return 2;
            case "thu": case "thursday": case "thu5": return 3;
            case "fri": case "friday": case "thu6": return 4;
            case "sat": case "saturday": case "thu7": return 5;
            case "sun": case "sunday": case "cn": return 6;
            default: return 0;
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