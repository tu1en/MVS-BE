package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.JobPositionDto;
import com.classroomapp.classroombackend.service.JobPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-positions")
@RequiredArgsConstructor
public class JobPositionController {
    private final JobPositionService jobPositionService;

    @PostMapping
    public ResponseEntity<JobPositionDto> create(@RequestBody JobPositionDto dto) {
        return ResponseEntity.ok(jobPositionService.createJobPosition(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPositionDto> update(@PathVariable Long id, @RequestBody JobPositionDto dto) {
        return ResponseEntity.ok(jobPositionService.updateJobPosition(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobPositionService.deleteJobPosition(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPositionDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(jobPositionService.getJobPosition(id));
    }

    @GetMapping
    public ResponseEntity<List<JobPositionDto>> getAll() {
        return ResponseEntity.ok(jobPositionService.getAllJobPositions());
    }
} 