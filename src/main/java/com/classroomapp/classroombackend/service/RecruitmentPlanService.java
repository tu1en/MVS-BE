package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.RecruitmentPlan;
import com.classroomapp.classroombackend.repository.RecruitmentPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecruitmentPlanService {
    
    @Autowired
    private RecruitmentPlanRepository recruitmentPlanRepository;
    
    public List<RecruitmentPlan> getAllRecruitmentPlans() {
        return recruitmentPlanRepository.findAll();
    }
    
    public RecruitmentPlan getRecruitmentPlanById(Long id) {
        return recruitmentPlanRepository.findById(id).orElse(null);
    }
    
    public RecruitmentPlan createRecruitmentPlan(RecruitmentPlan plan) {
        // Kiểm tra startDate không được trong quá khứ
        if (plan.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        
        // Kiểm tra endDate phải sau startDate
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        return recruitmentPlanRepository.save(plan);
    }
    
    public RecruitmentPlan updateRecruitmentPlan(Long id, RecruitmentPlan plan) {
        RecruitmentPlan existingPlan = recruitmentPlanRepository.findById(id).orElse(null);
        if (existingPlan == null) {
            throw new IllegalArgumentException("Recruitment plan not found");
        }
        
        // Kiểm tra startDate không được trong quá khứ
        if (plan.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }
        
        // Kiểm tra endDate phải sau startDate
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        existingPlan.setTitle(plan.getTitle());
        existingPlan.setStartDate(plan.getStartDate());
        existingPlan.setEndDate(plan.getEndDate());
        existingPlan.setTotalQuantity(plan.getTotalQuantity());
        existingPlan.setStatus(plan.getStatus());
        existingPlan.setUpdatedAt(java.time.LocalDateTime.now());
        
        return recruitmentPlanRepository.save(existingPlan);
    }
    
    public void deleteRecruitmentPlan(Long id) {
        recruitmentPlanRepository.deleteById(id);
    }
    
    public RecruitmentPlan changeStatus(Long id, RecruitmentPlan.Status status) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(id).orElse(null);
        if (plan == null) {
            throw new IllegalArgumentException("Recruitment plan not found");
        }
        
        plan.setStatus(status);
        plan.setUpdatedAt(java.time.LocalDateTime.now());
        return recruitmentPlanRepository.save(plan);
    }
    
    // Tự động đóng kế hoạch khi hết hạn (chạy mỗi ngày lúc 00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoCloseExpiredPlans() {
        LocalDate today = LocalDate.now();
        List<RecruitmentPlan> openPlans = recruitmentPlanRepository.findAll();
        
        for (RecruitmentPlan plan : openPlans) {
            if (plan.getStatus() == RecruitmentPlan.Status.OPEN && 
                plan.getEndDate().isBefore(today)) {
                plan.setStatus(RecruitmentPlan.Status.CLOSED);
                plan.setUpdatedAt(java.time.LocalDateTime.now());
                recruitmentPlanRepository.save(plan);
            }
        }
    }
    
    public List<RecruitmentPlan> getOpenRecruitmentPlans() {
        return recruitmentPlanRepository.findAll().stream()
                .filter(plan -> plan.getStatus() == RecruitmentPlan.Status.OPEN)
                .collect(java.util.stream.Collectors.toList());
    }
} 