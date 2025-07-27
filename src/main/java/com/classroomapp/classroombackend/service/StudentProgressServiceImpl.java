package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.StudentProgressDto;
import com.classroomapp.classroombackend.dto.ProgressAnalyticsDto;
import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.repository.StudentProgressRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentProgressServiceImpl implements StudentProgressService {
    
    private final StudentProgressRepository studentProgressRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final AssignmentRepository assignmentRepository;
    
    @Override
    public StudentProgressDto createOrUpdateProgress(StudentProgressDto progressDto) {
        try {
            StudentProgress progress;
            
            if (progressDto.getId() != null) {
                // Update existing progress
                progress = studentProgressRepository.findById(progressDto.getId())
                    .orElseThrow(() -> new RuntimeException("Progress not found with id: " + progressDto.getId()));
            } else {
                // Create new progress
                progress = new StudentProgress();
            }
            
            // Set fields
            progress.setStudentId(progressDto.getStudentId());
            progress.setClassroomId(progressDto.getClassroomId());
            progress.setAssignmentId(progressDto.getAssignmentId());
            progress.setProgressType(StudentProgress.ProgressType.valueOf(progressDto.getProgressType()));
            progress.setProgressPercentage(progressDto.getProgressPercentage());
            progress.setPointsEarned(progressDto.getPointsEarned());
            progress.setMaxPoints(progressDto.getMaxPoints());
            progress.setCompletionDate(progressDto.getCompletionDate());
            progress.setLastAccessed(LocalDateTime.now());
            progress.setTimeSpentMinutes(progressDto.getTimeSpentMinutes());
            progress.setNotes(progressDto.getNotes());
            
            StudentProgress saved = studentProgressRepository.save(progress);
            return convertToDto(saved);
        } catch (Exception e) {
            log.error("Error creating/updating progress: ", e);
            throw new RuntimeException("Failed to create/update progress", e);
        }
    }
    
    @Override
    public List<StudentProgressDto> getStudentProgressByClassroom(Long studentId, Long classroomId) {
        try {
            List<StudentProgress> progressList = studentProgressRepository
                .findByStudentIdAndClassroomIdOrderByLastAccessedDesc(studentId, classroomId);
            return progressList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving student progress for studentId: {}, classroomId: {}", studentId, classroomId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public StudentProgressDto getProgressByAssignment(Long studentId, Long assignmentId) {
        try {
            Optional<StudentProgress> progress = studentProgressRepository
                .findByStudentIdAndAssignmentId(studentId, assignmentId);
            return progress.map(this::convertToDto).orElse(new StudentProgressDto());
        } catch (Exception e) {
            log.error("Error retrieving assignment progress for studentId: {}, assignmentId: {}", studentId, assignmentId, e);
            return new StudentProgressDto();
        }
    }
    
    @Override
    public StudentProgressDto getOverallProgress(Long studentId, Long classroomId) {
        try {
            Optional<StudentProgress> progress = studentProgressRepository
                .findOverallProgress(studentId, classroomId);
            return progress.map(this::convertToDto).orElse(calculateOverallProgress(studentId, classroomId));
        } catch (Exception e) {
            log.error("Error retrieving overall progress for studentId: {}, classroomId: {}", studentId, classroomId, e);
            return new StudentProgressDto();
        }
    }
    
    @Override
    public List<StudentProgressDto> getAllStudentsProgress(Long classroomId) {
        try {
            List<StudentProgress> progressList = studentProgressRepository
                .findByClassroomIdOrderByStudentIdAscLastAccessedDesc(classroomId);
            return progressList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving all students progress for classroomId: {}", classroomId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public ProgressAnalyticsDto getProgressAnalytics(Long classroomId) {
        try {
            BigDecimal averageProgress = studentProgressRepository.getAverageProgressByClassroom(classroomId);
            // TODO: Implement more analytics as ProgressAnalyticsDto structure is available
            ProgressAnalyticsDto analytics = new ProgressAnalyticsDto();
            // Set basic analytics - extend as needed
            return analytics;
        } catch (Exception e) {
            log.error("Error retrieving progress analytics for classroomId: {}", classroomId, e);
            return new ProgressAnalyticsDto();
        }
    }
    
    @Override
    public List<StudentProgressDto> getStudentsWithLowProgress(Long classroomId, BigDecimal threshold) {
        try {
            List<StudentProgress> lowProgressStudents = studentProgressRepository
                .findStudentsWithLowProgress(classroomId, threshold);
            return lowProgressStudents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving students with low progress for classroomId: {}, threshold: {}", classroomId, threshold, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public StudentProgressDto updateTimeSpent(Long studentId, Long classroomId, 
                                            StudentProgress.ProgressType progressType, Integer minutesSpent) {
        try {
            List<StudentProgress> existingProgress = studentProgressRepository
                .findByStudentIdAndClassroomIdAndProgressType(studentId, classroomId, progressType);
            
            StudentProgress progress;
            if (!existingProgress.isEmpty()) {
                progress = existingProgress.get(0);
                progress.setTimeSpentMinutes(progress.getTimeSpentMinutes() + minutesSpent);
            } else {
                progress = new StudentProgress();
                progress.setStudentId(studentId);
                progress.setClassroomId(classroomId);
                progress.setProgressType(progressType);
                progress.setTimeSpentMinutes(minutesSpent);
                progress.setProgressPercentage(BigDecimal.ZERO);
            }
            
            progress.setLastAccessed(LocalDateTime.now());
            StudentProgress saved = studentProgressRepository.save(progress);
            return convertToDto(saved);
        } catch (Exception e) {
            log.error("Error updating time spent for studentId: {}, classroomId: {}", studentId, classroomId, e);
            return new StudentProgressDto();
        }
    }
    
    @Override
    public StudentProgressDto calculateOverallProgress(Long studentId, Long classroomId) {
        try {
            // Calculate overall progress based on assignments and other activities
            List<StudentProgress> allProgress = studentProgressRepository
                .findByStudentIdAndClassroomIdOrderByLastAccessedDesc(studentId, classroomId);
            
            if (allProgress.isEmpty()) {
                return new StudentProgressDto();
            }
            
            // Calculate weighted average
            BigDecimal totalPoints = BigDecimal.ZERO;
            BigDecimal maxTotalPoints = BigDecimal.ZERO;
            
            for (StudentProgress progress : allProgress) {
                if (progress.getProgressType() == StudentProgress.ProgressType.ASSIGNMENT) {
                    totalPoints = totalPoints.add(progress.getPointsEarned());
                    maxTotalPoints = maxTotalPoints.add(progress.getMaxPoints());
                }
            }
            
            BigDecimal overallPercentage = BigDecimal.ZERO;
            if (maxTotalPoints.compareTo(BigDecimal.ZERO) > 0) {
                overallPercentage = totalPoints.divide(maxTotalPoints, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }
            
            // Create or update overall progress record
            Optional<StudentProgress> existingOverall = studentProgressRepository
                .findOverallProgress(studentId, classroomId);
            
            StudentProgress overallProgress;
            if (existingOverall.isPresent()) {
                overallProgress = existingOverall.get();
            } else {
                overallProgress = new StudentProgress();
                overallProgress.setStudentId(studentId);
                overallProgress.setClassroomId(classroomId);
                overallProgress.setProgressType(StudentProgress.ProgressType.OVERALL);
            }
            
            overallProgress.setProgressPercentage(overallPercentage);
            overallProgress.setPointsEarned(totalPoints);
            overallProgress.setMaxPoints(maxTotalPoints);
            overallProgress.setLastAccessed(LocalDateTime.now());
            
            StudentProgress saved = studentProgressRepository.save(overallProgress);
            return convertToDto(saved);
        } catch (Exception e) {
            log.error("Error calculating overall progress for studentId: {}, classroomId: {}", studentId, classroomId, e);
            return new StudentProgressDto();
        }
    }
    
    @Override
    public Integer getTotalTimeSpent(Long studentId, Long classroomId) {
        try {
            Integer totalTime = studentProgressRepository.getTotalTimeSpent(studentId, classroomId);
            return totalTime != null ? totalTime : 0;
        } catch (Exception e) {
            log.error("Error retrieving total time spent for studentId: {}, classroomId: {}", studentId, classroomId, e);
            return 0;
        }
    }
    
    @Override
    public List<StudentProgressDto> bulkUpdateProgress(List<StudentProgressDto> progressList) {
        try {
            List<StudentProgressDto> updatedProgress = new ArrayList<>();
            for (StudentProgressDto progressDto : progressList) {
                updatedProgress.add(createOrUpdateProgress(progressDto));
            }
            return updatedProgress;
        } catch (Exception e) {
            log.error("Error in bulk progress update", e);
            return progressList;
        }
    }
    
    // Helper method to convert entity to DTO
    private StudentProgressDto convertToDto(StudentProgress progress) {
        StudentProgressDto dto = new StudentProgressDto();
        dto.setId(progress.getId());
        dto.setStudentId(progress.getStudentId());
        dto.setClassroomId(progress.getClassroomId());
        dto.setAssignmentId(progress.getAssignmentId());
        dto.setProgressType(progress.getProgressType().name());
        dto.setProgressPercentage(progress.getProgressPercentage());
        dto.setPointsEarned(progress.getPointsEarned());
        dto.setMaxPoints(progress.getMaxPoints());
        dto.setCompletionDate(progress.getCompletionDate());
        dto.setLastAccessed(progress.getLastAccessed());
        dto.setTimeSpentMinutes(progress.getTimeSpentMinutes());
        dto.setNotes(progress.getNotes());
        
        // Fetch additional information if needed
        try {
            if (progress.getStudentId() != null) {
                userRepository.findById(progress.getStudentId())
                    .ifPresent(user -> dto.setStudentName(user.getUsername()));
            }
            if (progress.getClassroomId() != null) {
                classroomRepository.findById(progress.getClassroomId())
                    .ifPresent(classroom -> dto.setClassroomName(classroom.getName()));
            }
            if (progress.getAssignmentId() != null) {
                assignmentRepository.findById(progress.getAssignmentId())
                    .ifPresent(assignment -> dto.setAssignmentTitle(assignment.getTitle()));
            }
        } catch (Exception e) {
            log.warn("Error fetching additional info for progress DTO: ", e);
        }
        
        return dto;
    }
}