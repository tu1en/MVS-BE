package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.model.InterviewSchedule;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.repository.InterviewScheduleRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        InterviewSchedule entity = new InterviewSchedule();
        entity.setApplication(app);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setStatus("SCHEDULED");
        InterviewSchedule saved = interviewRepo.save(entity);
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
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        entity.setOffer(offer);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateEvaluation(Long id, String evaluation) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        entity.setEvaluation(evaluation);
        interviewRepo.save(entity);
    }

    @Override
    @Transactional
    public void updateHourlyRate(Long id, String hourlyRate) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
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
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflict(LocalDateTime startTime, LocalDateTime endTime, Long excludeApplicationId) {
        try {
            // Lấy tất cả các lịch phỏng vấn đã được xếp (SCHEDULED hoặc PENDING)
            List<InterviewSchedule> existingSchedules = interviewRepo.findAll().stream()
                .filter(schedule -> "SCHEDULED".equals(schedule.getStatus()) || "PENDING".equals(schedule.getStatus()))
                .collect(Collectors.toList());
            
            System.out.println("=== Conflict Check Debug ===");
            System.out.println("New schedule: " + startTime + " to " + endTime);
            System.out.println("Exclude application ID: " + excludeApplicationId);
            System.out.println("Total existing schedules: " + existingSchedules.size());
            
            for (InterviewSchedule schedule : existingSchedules) {
                // Bỏ qua nếu là lịch của chính ứng viên đang được kiểm tra
                if (excludeApplicationId != null && schedule.getApplication() != null 
                    && excludeApplicationId.equals(schedule.getApplication().getId())) {
                    System.out.println("Skipping own schedule: " + schedule.getId());
                    continue;
                }
                
                System.out.println("Checking against schedule: " + schedule.getId() + 
                    " (" + schedule.getStartTime() + " to " + schedule.getEndTime() + 
                    ", Application: " + (schedule.getApplication() != null ? schedule.getApplication().getId() : "null") + ")");
                
                // Kiểm tra overlap theo từng điều kiện:
                // 1. Thời gian bắt đầu mới nằm trong khoảng thời gian cũ (không bao gồm điểm cuối)
                boolean startOverlap = (startTime.isAfter(schedule.getStartTime()) && startTime.isBefore(schedule.getEndTime()));
                
                // 2. Thời gian kết thúc mới nằm trong khoảng thời gian cũ (không bao gồm điểm đầu)
                boolean endOverlap = (endTime.isAfter(schedule.getStartTime()) && endTime.isBefore(schedule.getEndTime()));
                
                // 3. Thời gian mới bao trọn thời gian cũ
                boolean containsExisting = startTime.isBefore(schedule.getStartTime()) && endTime.isAfter(schedule.getEndTime());
                
                // 4. Thời gian cũ bao trọn thời gian mới
                boolean isContainedByExisting = schedule.getStartTime().isBefore(startTime) && schedule.getEndTime().isAfter(endTime);
                
                System.out.println("  startOverlap: " + startOverlap + ", endOverlap: " + endOverlap + 
                    ", containsExisting: " + containsExisting + ", isContainedByExisting: " + isContainedByExisting);
                
                if (startOverlap || endOverlap || containsExisting || isContainedByExisting) {
                    System.out.println("  *** CONFLICT DETECTED ***");
                    return true; // Có conflict
                }
            }
            System.out.println("  *** NO CONFLICT ***");
            return false; // Không có conflict
        } catch (Exception e) {
            // Log lỗi nếu có
            System.err.println("Error in hasConflict: " + e.getMessage());
            e.printStackTrace();
            return true; // Trả về true để đảm bảo an toàn
        }
    }

    @Override
    @Transactional
    public InterviewScheduleDto update(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        InterviewSchedule entity = interviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found"));
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        InterviewSchedule saved = interviewRepo.save(entity);
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