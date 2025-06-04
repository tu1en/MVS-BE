package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.AccomplishmentDto;
import com.classroomapp.classroombackend.service.AccomplishmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{userId}/accomplishments")
public class AccomplishmentController {
    
    private final AccomplishmentService accomplishmentService;
    
    @Autowired
    public AccomplishmentController(AccomplishmentService accomplishmentService) {
        this.accomplishmentService = accomplishmentService;
    }
    
    @GetMapping
    public ResponseEntity<List<AccomplishmentDto>> getStudentAccomplishments(@PathVariable Long userId) {
        return ResponseEntity.ok(accomplishmentService.getStudentAccomplishments(userId));
    }
    
    @PostMapping
    public ResponseEntity<AccomplishmentDto> createAccomplishment(
            @PathVariable Long userId,
            @RequestBody AccomplishmentDto accomplishmentDto) {
        accomplishmentDto.setUserId(userId);
        return ResponseEntity.ok(accomplishmentService.createAccomplishment(accomplishmentDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AccomplishmentDto> updateAccomplishment(
            @PathVariable Long userId,
            @PathVariable Long id,
            @RequestBody AccomplishmentDto accomplishmentDto) {
        accomplishmentDto.setUserId(userId);
        return ResponseEntity.ok(accomplishmentService.updateAccomplishment(id, accomplishmentDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccomplishment(@PathVariable Long id) {
        accomplishmentService.deleteAccomplishment(id);
        return ResponseEntity.ok().build();
    }
}