package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.AccomplishmentDto;
import com.classroomapp.classroombackend.service.AccomplishmentService;

@RestController
@RequestMapping("/api/accomplishments")
@PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
public class AccomplishmentController {
    
    private final AccomplishmentService accomplishmentService;
    
    @Autowired
    public AccomplishmentController(AccomplishmentService accomplishmentService) {
        this.accomplishmentService = accomplishmentService;
    }
    
    @GetMapping("/my-accomplishments")
    public ResponseEntity<List<AccomplishmentDto>> getMyAccomplishments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accomplishmentService.getAccomplishmentsByOwner(userDetails));
    }
    
    @PostMapping
    public ResponseEntity<AccomplishmentDto> createAccomplishment(
            @RequestBody AccomplishmentDto accomplishmentDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accomplishmentService.createAccomplishment(accomplishmentDto, userDetails));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@accomplishmentSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<AccomplishmentDto> updateAccomplishment(
            @PathVariable Long id,
            @RequestBody AccomplishmentDto accomplishmentDto) {
        return ResponseEntity.ok(accomplishmentService.updateAccomplishment(id, accomplishmentDto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("@accomplishmentSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<Void> deleteAccomplishment(@PathVariable Long id) {
        accomplishmentService.deleteAccomplishment(id);
        return ResponseEntity.ok().build();
    }
}