package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.service.ScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getAllSchedules() {
        List<ScheduleDto> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable Long id) {
        ScheduleDto schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTeacherId(@PathVariable Long teacherId) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByStudentId(@PathVariable Long studentId) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByStudentId(studentId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByClassroomId(@PathVariable Long classroomId) {
        log.info("GET /api/schedules/classroom/{} - Fetching schedules for classroom", classroomId);
        List<ScheduleDto> schedules = scheduleService.getSchedulesByClassroomId(classroomId);
        log.info("Found {} schedules for classroom ID {}", schedules.size(), classroomId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{scheduleId}/lectures")
    public ResponseEntity<List<LectureDto>> getLecturesBySchedule(@PathVariable Long scheduleId) {
        log.info("GET /api/schedules/{}/lectures - Fetching lectures for schedule", scheduleId);
        List<LectureDto> lectures = scheduleService.getLecturesByScheduleId(scheduleId);
        log.info("Found {} lectures for schedule ID {}", lectures.size(), scheduleId);
        return ResponseEntity.ok(lectures);
    }

    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(@Valid @RequestBody ScheduleDto scheduleDto) {
        ScheduleDto createdSchedule = scheduleService.createSchedule(scheduleDto);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @PathVariable Long id, 
            @Valid @RequestBody ScheduleDto scheduleDto) {
        ScheduleDto updatedSchedule = scheduleService.updateSchedule(id, scheduleDto);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/sample/{classroomId}")
    public ResponseEntity<String> createSampleSchedules(@PathVariable Long classroomId) {
        log.info("POST /api/schedules/sample/{} - Creating sample schedules", classroomId);
        try {
            scheduleService.createSampleSchedules(classroomId);
            return ResponseEntity.ok("Đã tạo lịch học mẫu cho lớp học");
        } catch (Exception e) {
            log.error("Error creating sample schedules for classroom {}: {}", classroomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo lịch học mẫu: " + e.getMessage());
        }
    }
} 