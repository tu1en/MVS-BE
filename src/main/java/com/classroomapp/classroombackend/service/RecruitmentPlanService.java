package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.RecruitmentPlan;
import com.classroomapp.classroombackend.repository.RecruitmentPlanRepository;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.repository.InterviewScheduleRepository;
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

    @Autowired
    private InterviewScheduleRepository interviewScheduleRepository;
    
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
        // Validate title max length 50
        if (plan.getTitle() != null && plan.getTitle().length() > 50) {
            throw new IllegalArgumentException("Tên kế hoạch tối đa 50 ký tự!");
        }
        // Kiểm tra startDate không được trong quá khứ
        if (plan.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được trong quá khứ");
        }
        
        // Kiểm tra endDate phải sau startDate
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Không được chồng chéo với bất kỳ kế hoạch nào khác
        validateNoOverlap(plan.getStartDate(), plan.getEndDate(), null);
        
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
            throw new IllegalArgumentException("Không tìm thấy kế hoạch tuyển dụng");
        }
        
        // Kiểm tra startDate không được trong quá khứ (chỉ khi kế hoạch chưa bắt đầu)
        if (existingPlan.getStartDate().isAfter(LocalDate.now()) && plan.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được trong quá khứ");
        }
        
        // Kiểm tra endDate phải sau startDate
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        // Không được chồng chéo với bất kỳ kế hoạch nào khác (bỏ qua chính nó)
        validateNoOverlap(plan.getStartDate(), plan.getEndDate(), id);
        
        // Validate title max length 50
        if (plan.getTitle() != null && plan.getTitle().length() > 50) {
            throw new IllegalArgumentException("Tên kế hoạch tối đa 50 ký tự!");
        }
        existingPlan.setTitle(plan.getTitle());
        // Nếu ngày bắt đầu đã qua, không cho sửa startDate
        if (!existingPlan.getStartDate().isBefore(LocalDate.now())) {
            existingPlan.setStartDate(plan.getStartDate());
        }
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
    
    private void validateNoOverlap(LocalDate newStart, LocalDate newEnd, Long excludeId) {
        List<RecruitmentPlan> all = recruitmentPlanRepository.findAll();
        for (RecruitmentPlan p : all) {
            if (excludeId != null && p.getId().equals(excludeId)) continue;
            LocalDate s = p.getStartDate();
            LocalDate e = p.getEndDate();
            boolean overlaps = !(newEnd.isBefore(s) || newStart.isAfter(e));
            if (overlaps) {
                throw new IllegalArgumentException("Recruitment plan dates overlap with existing plan: " + p.getTitle());
            }
        }
    }
    
    @Transactional
    public void deleteRecruitmentPlan(Long id) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(id).orElse(null);
        if (plan == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch tuyển dụng");
        }

        // Override: Xóa tất cả dữ liệu liên quan bất kể trạng thái
        try {
            // Xóa tất cả lịch phỏng vấn liên quan đến các đơn ứng tuyển của kế hoạch này
            interviewScheduleRepository.deleteByApplication_JobPosition_RecruitmentPlanId(id);

            // Xóa tất cả đơn ứng tuyển liên quan đến các vị trí của kế hoạch này
            recruitmentApplicationRepository.deleteByJobPosition_RecruitmentPlanId(id);

            // Xóa tất cả vị trí tuyển dụng của kế hoạch này
            jobPositionRepository.deleteByRecruitmentPlanId(id);

            // Cuối cùng xóa kế hoạch tuyển dụng
            recruitmentPlanRepository.deleteById(id);

        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể xóa kế hoạch tuyển dụng: " + e.getMessage());
        }
    }
    
    public RecruitmentPlan changeStatus(Long id, RecruitmentPlan.Status status) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(id).orElse(null);
        if (plan == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch tuyển dụng");
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
                try {
                    // Xóa toàn bộ đơn ứng tuyển thuộc các vị trí của kế hoạch này
                    recruitmentApplicationRepository.deleteByJobPosition_RecruitmentPlanId(plan.getId());
                } catch (Exception ignored) {}
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
    
    // Scan và chuẩn hoá trạng thái kế hoạch (đóng kế hoạch chưa đến ngày mở, và kế hoạch đã hết hạn)
    @Transactional
    public void scanAndCloseFuturePlans() {
        LocalDate today = LocalDate.now();
        List<RecruitmentPlan> allPlans = recruitmentPlanRepository.findAll();
        
        for (RecruitmentPlan plan : allPlans) {
            boolean changed = false;
            // Đóng kế hoạch OPEN nhưng ngày bắt đầu còn ở tương lai
            if (plan.getStatus() == RecruitmentPlan.Status.OPEN && plan.getStartDate().isAfter(today)) {
                plan.setStatus(RecruitmentPlan.Status.CLOSED);
                changed = true;
            }
            // Đóng kế hoạch OPEN nhưng đã quá ngày kết thúc
            if (plan.getStatus() == RecruitmentPlan.Status.OPEN && plan.getEndDate().isBefore(today)) {
                plan.setStatus(RecruitmentPlan.Status.CLOSED);
                changed = true;
                try {
                    // Xoá toàn bộ đơn ứng tuyển thuộc kế hoạch này
                    recruitmentApplicationRepository.deleteByJobPosition_RecruitmentPlanId(plan.getId());
                } catch (Exception ignored) {}
            }
            if (changed) {
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