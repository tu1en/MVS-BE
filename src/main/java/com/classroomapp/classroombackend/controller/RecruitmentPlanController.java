package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.model.RecruitmentPlan;
import com.classroomapp.classroombackend.service.RecruitmentPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruitment-plans")
@CrossOrigin(origins = "*")
public class RecruitmentPlanController {
    
    @Autowired
    private RecruitmentPlanService recruitmentPlanService;
    
    @GetMapping
    public ResponseEntity<List<RecruitmentPlan>> getAllRecruitmentPlans() {
        List<RecruitmentPlan> plans = recruitmentPlanService.getAllRecruitmentPlans();
        return ResponseEntity.ok(plans);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RecruitmentPlan> getRecruitmentPlanById(@PathVariable Long id) {
        RecruitmentPlan plan = recruitmentPlanService.getRecruitmentPlanById(id);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(plan);
    }
    
    @GetMapping("/open")
    public ResponseEntity<List<RecruitmentPlan>> getOpenRecruitmentPlans() {
        List<RecruitmentPlan> openPlans = recruitmentPlanService.getOpenRecruitmentPlans();
        return ResponseEntity.ok(openPlans);
    }
    
    @PostMapping
    public ResponseEntity<RecruitmentPlan> createRecruitmentPlan(@RequestBody RecruitmentPlan plan) {
        try {
            RecruitmentPlan createdPlan = recruitmentPlanService.createRecruitmentPlan(plan);
            return ResponseEntity.ok(createdPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RecruitmentPlan> updateRecruitmentPlan(@PathVariable Long id, @RequestBody RecruitmentPlan plan) {
        try {
            RecruitmentPlan updatedPlan = recruitmentPlanService.updateRecruitmentPlan(id, plan);
            return ResponseEntity.ok(updatedPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecruitmentPlan(@PathVariable Long id) {
        try {
            recruitmentPlanService.deleteRecruitmentPlan(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<RecruitmentPlan> changeStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            RecruitmentPlan.Status planStatus = RecruitmentPlan.Status.valueOf(status.toUpperCase());
            RecruitmentPlan updatedPlan = recruitmentPlanService.changeStatus(id, planStatus);
            return ResponseEntity.ok(updatedPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/close")
    public ResponseEntity<RecruitmentPlan> closeRecruitmentPlan(@PathVariable Long id) {
        try {
            RecruitmentPlan closedPlan = recruitmentPlanService.changeStatus(id, RecruitmentPlan.Status.CLOSED);
            return ResponseEntity.ok(closedPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/open")
    public ResponseEntity<RecruitmentPlan> openRecruitmentPlan(@PathVariable Long id) {
        try {
            RecruitmentPlan openedPlan = recruitmentPlanService.changeStatus(id, RecruitmentPlan.Status.OPEN);
            return ResponseEntity.ok(openedPlan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 