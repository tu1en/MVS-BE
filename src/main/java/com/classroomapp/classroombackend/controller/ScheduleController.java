package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ScheduleDTO;
import com.classroomapp.classroombackend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        return new ResponseEntity<>(scheduleService.createSchedule(scheduleDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ScheduleDTO> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleDTO scheduleDTO) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, scheduleDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TEACHER')")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByTeacherId(@PathVariable Long teacherId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByTeacherId(teacherId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'STUDENT')")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByStudentId(studentId));
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'TEACHER')")
    public ResponseEntity<List<ScheduleDTO>> getSchedulesByClassId(@PathVariable String classId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByClassId(classId));
    }
}
