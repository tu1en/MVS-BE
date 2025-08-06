package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.RecruitmentPlan;
import com.classroomapp.classroombackend.repository.RecruitmentPlanRepository;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
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
    
    @Autowired
    private JobPositionRepository jobPositionRepository;
    
    @Autowired
    private RecruitmentApplicationRepository recruitmentApplicationRepository;
    
    public List<RecruitmentPlan> getAllRecruitmentPlans() {
        // Tự động scan và đóng kế hoạch tương lai trước khi trả về
        scanAndCloseFuturePlans();
        
        List<RecruitmentPlan> plans = recruitmentPlanRepository.findAll();
        // Tính totalQuantity từ các vị trí
        for (RecruitmentPlan plan : plans) {
            plan.setTotalQuantity(calculateTotalQuantity(plan.getId()));
        }
        return plans;
    }
    
    public RecruitmentPlan getRecruitmentPlanById(Long id) {
        // Tự động scan và đóng kế hoạch tương lai trước khi trả về
        scanAndCloseFuturePlans();
        
        RecruitmentPlan plan = recruitmentPlanRepository.findById(id).orElse(null);
        if (plan != null) {
            plan.setTotalQuantity(calculateTotalQuantity(id));
        }
        return plan;
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
        
        // Tự động tính totalQuantity từ các vị trí (ban đầu = 0)
        plan.setTotalQuantity(0);
        
        // Tự động đóng kế hoạch nếu ngày bắt đầu trong tương lai
        if (plan.getStartDate().isAfter(LocalDate.now())) {
            plan.setStatus(RecruitmentPlan.Status.CLOSED);
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
        existingPlan.setStatus(plan.getStatus());
        existingPlan.setUpdatedAt(java.time.LocalDateTime.now());
        
        // Tự động đóng kế hoạch nếu ngày bắt đầu trong tương lai
        if (plan.getStartDate().isAfter(LocalDate.now())) {
            existingPlan.setStatus(RecruitmentPlan.Status.CLOSED);
        }
        
        // Tự động tính totalQuantity từ các vị trí
        existingPlan.setTotalQuantity(calculateTotalQuantity(id));
        
        return recruitmentPlanRepository.save(existingPlan);
    }
    
    public void deleteRecruitmentPlan(Long id) {
        // Xóa các vị trí liên quan
        jobPositionRepository.deleteByRecruitmentPlanId(id);
        
        // Xóa các đơn ứng tuyển liên quan
        recruitmentApplicationRepository.deleteByJobPosition_RecruitmentPlanId(id);
        
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
    
    // Tính tổng số lượng từ các vị trí trong kế hoạch
    private Integer calculateTotalQuantity(Long recruitmentPlanId) {
        return jobPositionRepository.findByRecruitmentPlanId(recruitmentPlanId)
                .stream()
                .mapToInt(job -> job.getQuantity() != null ? job.getQuantity() : 0)
                .sum();
    }
    
    // Cập nhật totalQuantity cho một kế hoạch cụ thể
    public void updateTotalQuantity(Long recruitmentPlanId) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(recruitmentPlanId).orElse(null);
        if (plan != null) {
            plan.setTotalQuantity(calculateTotalQuantity(recruitmentPlanId));
            recruitmentPlanRepository.save(plan);
        }
    }
    
    // Tự động đóng kế hoạch khi hết hạn và mở kế hoạch khi đến ngày (chạy mỗi ngày lúc 00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoManagePlanStatus() {
        LocalDate today = LocalDate.now();
        List<RecruitmentPlan> allPlans = recruitmentPlanRepository.findAll();
        
        for (RecruitmentPlan plan : allPlans) {
            boolean shouldUpdate = false;
            
            // Đóng kế hoạch khi hết hạn
            if (plan.getStatus() == RecruitmentPlan.Status.OPEN && 
                plan.getEndDate().isBefore(today)) {
                plan.setStatus(RecruitmentPlan.Status.CLOSED);
                shouldUpdate = true;
            }
            
            // Mở kế hoạch khi đến ngày bắt đầu
            if (plan.getStatus() == RecruitmentPlan.Status.CLOSED && 
                !plan.getStartDate().isAfter(today) && 
                !plan.getEndDate().isBefore(today)) {
                plan.setStatus(RecruitmentPlan.Status.OPEN);
                shouldUpdate = true;
            }
            
            // Đóng kế hoạch chưa đến ngày mở
            if (plan.getStatus() == RecruitmentPlan.Status.OPEN && 
                plan.getStartDate().isAfter(today)) {
                plan.setStatus(RecruitmentPlan.Status.CLOSED);
                shouldUpdate = true;
            }
            
            if (shouldUpdate) {
                plan.setUpdatedAt(java.time.LocalDateTime.now());
                recruitmentPlanRepository.save(plan);
            }
        }
    }
    
    // Scan và đóng các kế hoạch có ngày bắt đầu trong tương lai
    @Transactional
    public void scanAndCloseFuturePlans() {
        LocalDate today = LocalDate.now();
        List<RecruitmentPlan> allPlans = recruitmentPlanRepository.findAll();
        
        for (RecruitmentPlan plan : allPlans) {
            if (plan.getStatus() == RecruitmentPlan.Status.OPEN && 
                plan.getStartDate().isAfter(today)) {
                plan.setStatus(RecruitmentPlan.Status.CLOSED);
                plan.setUpdatedAt(java.time.LocalDateTime.now());
                recruitmentPlanRepository.save(plan);
            }
        }
    }
    
    // Kiểm tra xem kế hoạch có thể mở không
    public boolean canOpenPlan(Long planId) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(planId).orElse(null);
        if (plan == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return !plan.getStartDate().isAfter(today);
    }
    
    public List<RecruitmentPlan> getOpenRecruitmentPlans() {
        // Tự động scan và đóng kế hoạch tương lai trước khi trả về
        scanAndCloseFuturePlans();
        
        List<RecruitmentPlan> openPlans = recruitmentPlanRepository.findAll().stream()
                .filter(plan -> plan.getStatus() == RecruitmentPlan.Status.OPEN)
                .collect(java.util.stream.Collectors.toList());
        
        // Tính totalQuantity từ các vị trí
        for (RecruitmentPlan plan : openPlans) {
            plan.setTotalQuantity(calculateTotalQuantity(plan.getId()));
        }
        
        return openPlans;
    }
} 