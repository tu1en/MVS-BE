package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.model.InterviewSchedule;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.repository.InterviewScheduleRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.service.InterviewScheduleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterviewScheduleServiceImpl implements InterviewScheduleService {
    private final InterviewScheduleRepository interviewRepo;
    private final RecruitmentApplicationRepository appRepo;
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    @Transactional
    public InterviewScheduleDto create(Long applicationId, LocalDateTime startTime, LocalDateTime endTime) {
        RecruitmentApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        // Validate within recruitment plan window
        if (app.getJobPosition() != null && app.getJobPosition().getRecruitmentPlan() != null) {
            var plan = app.getJobPosition().getRecruitmentPlan();
            if (startTime.toLocalDate().isBefore(plan.getStartDate()) || endTime.toLocalDate().isAfter(plan.getEndDate())) {
                throw new IllegalArgumentException("Thời gian phỏng vấn phải nằm trong khoảng thời gian của kế hoạch tuyển dụng");
            }
        }
        
        // Kiểm tra trùng lịch trước khi tạo
        if (hasConflict(startTime, endTime, applicationId)) {
            throw new IllegalArgumentException("Thời gian phỏng vấn bị trùng với lịch phỏng vấn khác!");
        }
        
        InterviewSchedule entity = new InterviewSchedule();
        entity.setApplication(app);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setStatus("SCHEDULED");
        InterviewSchedule saved = interviewRepo.save(entity);
        
        // Scan lại toàn bộ lịch để đảm bảo không có conflict
        List<String> conflicts = scanAllConflicts();
        if (!conflicts.isEmpty()) {
            // Log các conflict để debug
            System.err.println("Detected conflicts after creating interview:");
            conflicts.forEach(System.err::println);
        }
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleDto> getByJobPosition(Long jobPositionId) {
        return interviewRepo.findByApplication_JobPosition_Id(jobPositionId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleDto> getByApplication(Long applicationId) {
        return interviewRepo.findByApplication_Id(applicationId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleDto> getAll() {
        return interviewRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, String result) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        entity.setStatus(status);
        entity.setResult(result);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateOffer(Long id, String offer) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch phỏng vấn"));
        entity.setOffer(offer);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateEvaluation(Long id, String evaluation) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch phỏng vấn"));
        if (evaluation != null && evaluation.length() > 200) {
            throw new IllegalArgumentException("Đánh giá tối đa 200 ký tự!");
        }
        entity.setEvaluation(evaluation);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateHourlyRate(Long id, String hourlyRate) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch phỏng vấn"));
        entity.setHourlyRate(new java.math.BigDecimal(hourlyRate));
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        interviewRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewScheduleDto getById(Long id) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch phỏng vấn"));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflict(LocalDateTime startTime, LocalDateTime endTime, Long excludeApplicationId) {
        try {
            // Lấy tất cả các lịch phỏng vấn đã được xếp (SCHEDULED hoặc PENDING)
            // Chỉ cho phép đè lịch khi status là COMPLETED (Hoàn thành)
            List<InterviewSchedule> existingSchedules = interviewRepo.findAll().stream()
                .filter(schedule -> "SCHEDULED".equals(schedule.getStatus()) || "PENDING".equals(schedule.getStatus()))
                .collect(Collectors.toList());
            
            for (InterviewSchedule schedule : existingSchedules) {
                // Bỏ qua nếu là lịch của chính ứng viên đang được kiểm tra
                if (excludeApplicationId != null && schedule.getApplication() != null 
                    && excludeApplicationId.equals(schedule.getApplication().getId())) {
                    continue;
                }
                
                // Kiểm tra overlap theo từng điều kiện:
                // 1. Thời gian bắt đầu mới nằm trong khoảng thời gian cũ (không bao gồm điểm cuối)
                boolean startOverlap = (startTime.isAfter(schedule.getStartTime()) && startTime.isBefore(schedule.getEndTime()));
                
                // 2. Thời gian kết thúc mới nằm trong khoảng thời gian cũ (không bao gồm điểm đầu)
                boolean endOverlap = (endTime.isAfter(schedule.getStartTime()) && endTime.isBefore(schedule.getEndTime()));
                
                // 3. Thời gian mới bao trọn thời gian cũ
                boolean containsExisting = startTime.isBefore(schedule.getStartTime()) && endTime.isAfter(schedule.getEndTime());
                
                // 4. Thời gian cũ bao trọn thời gian mới
                boolean isContainedByExisting = schedule.getStartTime().isBefore(startTime) && schedule.getEndTime().isAfter(endTime);
                
                if (startOverlap || endOverlap || containsExisting || isContainedByExisting) {
                    return true; // Có conflict
                }
            }
            return false; // Không có conflict
        } catch (Exception e) {
            // Log lỗi nếu có
            System.err.println("Error in hasConflict: " + e.getMessage());
            e.printStackTrace();
            return true; // Trả về true để đảm bảo an toàn
        }
    }

    /**
     * Hàm scan lại toàn bộ lịch để đảm bảo logic validation đúng
     * Kiểm tra và báo cáo tất cả các conflict trong hệ thống
     */
    @Transactional(readOnly = true)
    public List<String> scanAllConflicts() {
        List<String> conflicts = new ArrayList<>();
        List<InterviewSchedule> allSchedules = interviewRepo.findAll();
        
        // Lọc ra các lịch đang hoạt động (không phải COMPLETED)
        List<InterviewSchedule> activeSchedules = allSchedules.stream()
            .filter(schedule -> !"COMPLETED".equals(schedule.getStatus()))
            .collect(Collectors.toList());
        
        // Kiểm tra từng cặp lịch đang hoạt động
        for (int i = 0; i < activeSchedules.size(); i++) {
            for (int j = i + 1; j < activeSchedules.size(); j++) {
                InterviewSchedule schedule1 = activeSchedules.get(i);
                InterviewSchedule schedule2 = activeSchedules.get(j);
                
                if (hasTimeOverlap(schedule1, schedule2)) {
                    String conflict = String.format(
                        "Conflict giữa lịch %d (%s) và lịch %d (%s) - Thời gian: %s - %s",
                        schedule1.getId(),
                        schedule1.getApplication() != null ? schedule1.getApplication().getFullName() : "Unknown",
                        schedule2.getId(),
                        schedule2.getApplication() != null ? schedule2.getApplication().getFullName() : "Unknown",
                        schedule1.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        schedule1.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    );
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Kiểm tra xem hai lịch có bị trùng thời gian không
     */
    private boolean hasTimeOverlap(InterviewSchedule schedule1, InterviewSchedule schedule2) {
        LocalDateTime start1 = schedule1.getStartTime();
        LocalDateTime end1 = schedule1.getEndTime();
        LocalDateTime start2 = schedule2.getStartTime();
        LocalDateTime end2 = schedule2.getEndTime();
        
        // Kiểm tra overlap theo từng điều kiện:
        // 1. Thời gian bắt đầu của lịch 1 nằm trong khoảng thời gian của lịch 2
        boolean start1Overlap = (start1.isAfter(start2) && start1.isBefore(end2));
        
        // 2. Thời gian kết thúc của lịch 1 nằm trong khoảng thời gian của lịch 2
        boolean end1Overlap = (end1.isAfter(start2) && end1.isBefore(end2));
        
        // 3. Lịch 1 bao trọn lịch 2
        boolean containsSchedule2 = start1.isBefore(start2) && end1.isAfter(end2);
        
        // 4. Lịch 2 bao trọn lịch 1
        boolean containsSchedule1 = start2.isBefore(start1) && end2.isAfter(end1);
        
        return start1Overlap || end1Overlap || containsSchedule2 || containsSchedule1;
    }

    @Override
    @Transactional
    public InterviewScheduleDto update(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        // Validate within recruitment plan window
        RecruitmentApplication app = entity.getApplication();
        if (app != null && app.getJobPosition() != null && app.getJobPosition().getRecruitmentPlan() != null) {
            var plan = app.getJobPosition().getRecruitmentPlan();
            if (startTime.toLocalDate().isBefore(plan.getStartDate()) || endTime.toLocalDate().isAfter(plan.getEndDate())) {
                throw new IllegalArgumentException("Thời gian phỏng vấn phải nằm trong khoảng thời gian của kế hoạch tuyển dụng");
            }
        }
        
        // Kiểm tra trùng lịch trước khi cập nhật
        if (hasConflict(startTime, endTime, app != null ? app.getId() : null)) {
            throw new IllegalArgumentException("Thời gian phỏng vấn bị trùng với lịch phỏng vấn khác!");
        }
        
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        InterviewSchedule saved = interviewRepo.save(entity);
        
        // Scan lại toàn bộ lịch để đảm bảo không có conflict
        List<String> conflicts = scanAllConflicts();
        if (!conflicts.isEmpty()) {
            // Log các conflict để debug
            System.err.println("Detected conflicts after updating interview:");
            conflicts.forEach(System.err::println);
        }
        
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewScheduleDto> getAcceptedInterviews() {
        return interviewRepo.findAll()
                .stream()
                .filter(schedule -> "ACCEPTED".equals(schedule.getStatus()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private InterviewScheduleDto toDto(InterviewSchedule entity) {
        InterviewScheduleDto dto = new InterviewScheduleDto();
        dto.setId(entity.getId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setStatus(entity.getStatus());
        dto.setResult(entity.getResult());
        dto.setOffer(entity.getOffer());
        dto.setEvaluation(entity.getEvaluation());
        dto.setHourlyRate(entity.getHourlyRate());
        
        if (entity.getApplication() != null) {
            dto.setApplicationId(entity.getApplication().getId());
            dto.setApplicantName(entity.getApplication().getFullName());
            dto.setApplicantEmail(entity.getApplication().getEmail());
            dto.setApplicantPhone(entity.getApplication().getPhoneNumber());
            
            if (entity.getApplication().getJobPosition() != null) {
                dto.setJobTitle(entity.getApplication().getJobPosition().getTitle());
                dto.setSalaryRange(entity.getApplication().getJobPosition().getSalaryRange());
                dto.setContractType(entity.getApplication().getJobPosition().getContractType());
            }
        }
        return dto;
    }
} 