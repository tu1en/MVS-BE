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
    public ResponseEntity<?> create(@RequestBody JobPositionDto dto) {
        try {
            return ResponseEntity.ok(jobPositionService.createJobPosition(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi tạo vị trí");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody JobPositionDto dto) {
        try {
            return ResponseEntity.ok(jobPositionService.updateJobPosition(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi cập nhật vị trí");
        }
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
    public ResponseEntity<List<JobPositionDto>> getAll(@RequestParam(required = false) Long recruitmentPlanId) {
        if (recruitmentPlanId != null) {
            return ResponseEntity.ok(jobPositionService.getJobPositionsByRecruitmentPlan(recruitmentPlanId));
        }
        return ResponseEntity.ok(jobPositionService.getAllJobPositions());
    }

    @GetMapping("/all")
    public ResponseEntity<List<JobPositionDto>> getAllJobPositions() {
        return ResponseEntity.ok(jobPositionService.getAllJobPositionsWithoutFilter());
    }
} 