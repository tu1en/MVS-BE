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
@RestController("scheduleClassroomController")
@RequestMapping("/api/v1/classroom/schedules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", allowCredentials = "true")
public class ScheduleClassroomController {

    private final ClassroomScheduleService scheduleService;

    /**
     * Lay tat ca schedules theo classroom ID
     */
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByClassroom(
            @PathVariable Long classroomId,
            Authentication authentication) {
        
        log.info("Getting schedules for classroom ID: {} - User: {}", classroomId, authentication.getName());
        List<ScheduleDto> schedules = scheduleService.getSchedulesByClassroomId(classroomId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lay schedule theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ScheduleDto> getScheduleById(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Getting schedule by ID: {} - User: {}", id, authentication.getName());
        ScheduleDto schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Lay schedules theo ngay trong tuan
     */
    @GetMapping("/day/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByDay(
            @PathVariable DayOfWeek dayOfWeek,
            Authentication authentication) {
        
        log.info("Getting schedules for day: {} - User: {}", dayOfWeek, authentication.getName());
        List<ScheduleDto> schedules = scheduleService.getSchedulesByDayOfWeek(dayOfWeek);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Tim kiem schedules theo dia diem
     */
    @GetMapping("/location")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByLocation(
            @RequestParam String location,
            Authentication authentication) {
        
        log.info("Searching schedules by location: {} - User: {}", location, authentication.getName());
        List<ScheduleDto> schedules = scheduleService.getSchedulesByLocation(location);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Tao schedule moi
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createSchedule(
            @Valid @RequestBody CreateScheduleDto createDto,
            Authentication authentication) {
        
        log.info("Creating new schedule for classroom ID: {} - User: {}", 
                createDto.getClassroomId(), authentication.getName());
        
        try {
            ScheduleDto newSchedule = scheduleService.createSchedule(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSchedule);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("Error creating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Cap nhat schedule
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleDto updateDto,
            Authentication authentication) {
        
        log.info("Updating schedule ID: {} - User: {}", id, authentication.getName());
        
        try {
            ScheduleDto updatedSchedule = scheduleService.updateSchedule(id, updateDto);
            return ResponseEntity.ok(updatedSchedule);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("Error updating schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("type", "BUSINESS_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Xoa schedule
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteSchedule(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Deleting schedule ID: {} - User: {}", id, authentication.getName());
        
        try {
            scheduleService.deleteSchedule(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xoa lich hoc thanh cong");
            response.put("deletedId", id);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error deleting schedule: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Kiem tra conflict lich hoc
     */
    @PostMapping("/check-conflict")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> checkScheduleConflict(
            @RequestBody CreateScheduleDto scheduleDto,
            Authentication authentication) {
        
        log.info("Checking schedule conflict for classroom ID: {} - User: {}", 
                scheduleDto.getClassroomId(), authentication.getName());
        
        boolean hasConflict = scheduleService.hasScheduleConflict(
                scheduleDto.getClassroomId(),
                scheduleDto.getDayOfWeek(),
                scheduleDto.getStartTime(),
                scheduleDto.getEndTime(),
                null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("hasConflict", hasConflict);
        response.put("message", hasConflict ? "Co xung dot lich hoc" : "Khong co xung dot");
        
        return ResponseEntity.ok(response);
    }
}