package com.classroomapp.classroombackend.controller.classroommanagement;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateScheduleDto;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller cho Schedule Management
 */
@RestController
@RequestMapping("/api/classroom-management/schedules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", allowCredentials = "true")
public class ScheduleController {

    private final ClassroomScheduleService scheduleService;

    /**
     * Láº¥y táº¥t cáº£ schedules theo classroom ID
     */
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByClassroom(
            @PathVariable Long classroomId,
            Authentication authentication) {
        
        log.info("ðŸ” Getting schedules for classroom ID: {} - User: {}", classroomId, authentication.getName());
        List<ScheduleDto> schedules = scheduleService.getSchedulesByClassroomId(classroomId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Láº¥y schedule theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> getScheduleById(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("ðŸ” Getting schedule by ID: {} - User: {}", id, authentication.getName());
        ScheduleDto schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Láº¥y schedules theo ngÃ y trong tuáº§n
     */
    @GetMapping("/day/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByDay(
            @PathVariable DayOfWeek dayOfWeek,
            Authentication authentication) {
        
        log.info("ðŸ” Getting schedules for day: {} - User: {}", dayOfWeek, authentication.getName());
        List<ScheduleDto> schedules = scheduleService.getSchedulesByDayOfWeek(dayOfWeek);
        return ResponseEntity.ok(schedules);
    }

    /**
     * TÃ¬m kiáº¿m schedules theo Ä‘á»‹a Ä‘iá»ƒm
     */
    @GetMapping("/location")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByLocation(
            @RequestParam String location,
            Authentication authentication) {
        
        log.info("ðŸ” Searching schedules by location: {} - User: {}", location, authentication.getName());
        List<ScheduleDto> schedules = scheduleService.getSchedulesByLocation(location);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Táº¡o schedule má»›i
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createSchedule(
            @Valid @RequestBody CreateScheduleDto createDto,
            Authentication authentication) {
        
        log.info("ðŸ“ Creating new schedule for classroom ID: {} - User: {}", 
                createDto.getClassroomId(), authentication.getName());
        
        try {
            ScheduleDto newSchedule = scheduleService.createSchedule(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSchedule);
        } catch (IllegalArgumentException e) {
            log.error("âŒ Validation error creating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("âŒ Error creating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Cáº­p nháº­t schedule
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleDto updateDto,
            Authentication authentication) {
        
        log.info("ðŸ“ Updating schedule ID: {} - User: {}", id, authentication.getName());
        
        try {
            ScheduleDto updatedSchedule = scheduleService.updateSchedule(id, updateDto);
            return ResponseEntity.ok(updatedSchedule);
        } catch (IllegalArgumentException e) {
            log.error("âŒ Validation error updating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("âŒ Error updating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * XÃ³a schedule
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSchedule(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("ðŸ—‘ï¸ Deleting schedule ID: {} - User: {}", id, authentication.getName());
        
        try {
            scheduleService.deleteSchedule(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "XÃ³a lá»‹ch há»c thÃ nh cÃ´ng");
            response.put("deletedId", id);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("âŒ Error deleting schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Kiá»ƒm tra conflict lá»‹ch há»c
     */
    @PostMapping("/check-conflict")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> checkScheduleConflict(
            @RequestBody CreateScheduleDto scheduleDto,
            Authentication authentication) {
        
        log.info("ðŸ” Checking schedule conflict for classroom ID: {} - User: {}", 
                scheduleDto.getClassroomId(), authentication.getName());
        
        boolean hasConflict = scheduleService.hasScheduleConflict(
                scheduleDto.getClassroomId(),
                scheduleDto.getDayOfWeek(),
                scheduleDto.getStartTime(),
                scheduleDto.getEndTime(),
                null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasConflict", hasConflict);
        response.put("message", hasConflict ? "CÃ³ xung Ä‘á»™t lá»‹ch há»c" : "KhÃ´ng cÃ³ xung Ä‘á»™t");
        
        return ResponseEntity.ok(response);
    }
}
