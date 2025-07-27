package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeacherScheduleService {

    private final ScheduleRepository scheduleRepository;

    /**
     * Get teacher schedules in date range - uses multiple fallback methods
     */
    public List<Schedule> getTeacherSchedules(Long teacherId, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
        
            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
            
            log.info("🔍 Querying schedules for teacher {} from {} to {}", 
                teacherId, startDateTime, endDateTime);
            
            // PRIORITY 1: Try native query first (most reliable)
            try {
                List<Schedule> nativeResults = scheduleRepository.findByTeacherIdAndDateRangeNative(
                    teacherId, startDateTime, endDateTime);
                
                log.info("✅ Native query found {} schedules", nativeResults.size());
                
                if (!nativeResults.isEmpty()) {
                    logFirstSchedule(nativeResults.get(0), "Native Query");
                    return nativeResults;
                }
                
                // If no results, check if teacher has any schedules at all
                List<Schedule> allTeacherSchedules = scheduleRepository.findByTeacherIdNative(teacherId);
                log.info("   Teacher has {} total schedules in database", allTeacherSchedules.size());
                
                return nativeResults; // Return empty list
                
            } catch (Exception e) {
                log.error("❌ Native query failed: {}", e.getMessage());
            }

            // PRIORITY 2: Try JPQL with FETCH JOIN
            try {
                List<Schedule> fetchResults = scheduleRepository.findByTeacherIdAndDateRangeWithFetch(
                    teacherId, startDateTime, endDateTime);
                
                log.info("✅ FETCH JOIN query found {} schedules", fetchResults.size());
                
                if (!fetchResults.isEmpty()) {
                    logFirstSchedule(fetchResults.get(0), "FETCH JOIN Query");
                    return fetchResults;
                }
                
                return fetchResults; // Return empty list
                
            } catch (Exception e) {
                log.error("❌ FETCH JOIN query failed: {}", e.getMessage());
            }

            // PRIORITY 3: Try original JPQL query (last resort)
            try {
                List<Schedule> jpqlResults = scheduleRepository.findByTeacherIdAndDateRange(
                    teacherId, startDateTime, endDateTime);
                
                log.info("✅ Original JPQL query found {} schedules", jpqlResults.size());
                
                if (!jpqlResults.isEmpty()) {
                    logFirstSchedule(jpqlResults.get(0), "Original JPQL Query");
                }
                
                return jpqlResults;
                
            } catch (Exception e) {
                log.error("❌ All query methods failed. Last error: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to fetch teacher schedules: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            log.error("❌ Error in getTeacherSchedules: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get today's schedules for teacher
     */
    public List<Schedule> getTodaySchedules(Long teacherId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startDateTime = today.atStartOfDay();
            LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

            log.info("🔍 Getting today schedules for teacher {} ({} to {})", 
                teacherId, startDateTime, endDateTime);
            
            // Try native query first
            try {
                List<Schedule> schedules = scheduleRepository.findTodaySchedulesByTeacherIdNative(
                    teacherId, startDateTime, endDateTime);
                
                log.info("✅ Found {} schedules for today (native)", schedules.size());
                return schedules;
                
            } catch (Exception e) {
                log.error("❌ Native today query failed: {}", e.getMessage());
            }

            // Fallback to JPQL
            List<Schedule> schedules = scheduleRepository.findTodaySchedulesByTeacherId(
                teacherId, startDateTime, endDateTime);
            
            log.info("✅ Found {} schedules for today (JPQL)", schedules.size());
            return schedules;
            
        } catch (Exception e) {
            log.error("❌ Error getting today's schedules: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get upcoming schedules for teacher
     */
    public List<Schedule> getUpcomingSchedules(Long teacherId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            log.info("🔍 Getting upcoming schedules for teacher {} after {}", teacherId, now);
            
            // Try native query first
            try {
                List<Schedule> schedules = scheduleRepository.findUpcomingSchedulesByTeacherIdNative(
                    teacherId, now);
                
                log.info("✅ Found {} upcoming schedules (native)", schedules.size());
                return schedules;
                
            } catch (Exception e) {
                log.error("❌ Native upcoming query failed: {}", e.getMessage());
            }

            // Fallback to JPQL
            List<Schedule> schedules = scheduleRepository.findUpcomingSchedulesByTeacherId(
                teacherId, now);
            
            log.info("✅ Found {} upcoming schedules (JPQL)", schedules.size());
            return schedules;
            
        } catch (Exception e) {
            log.error("❌ Error getting upcoming schedules: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Count schedules in date range
     */
    public long countSchedulesInRange(Long teacherId, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();

            log.info("🔍 Counting schedules for teacher {} from {} to {}", 
                teacherId, startDateTime, endDateTime);

            // Try native query first
            try {
                long count = scheduleRepository.countByTeacherIdAndDateRangeNative(
                    teacherId, startDateTime, endDateTime);
                
                log.info("📊 Schedule count (native): {}", count);
                return count;
                
            } catch (Exception e) {
                log.error("❌ Native count query failed: {}", e.getMessage());
            }

            // Fallback to JPQL
            long count = scheduleRepository.countByTeacherIdAndDateRange(
                teacherId, startDateTime, endDateTime);
            
            log.info("📊 Schedule count (JPQL): {}", count);
            return count;
            
        } catch (Exception e) {
            log.error("❌ Error counting schedules: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Debug method - get all schedules for teacher (no date filter)
     */
    public List<Schedule> getAllTeacherSchedules(Long teacherId) {
        try {
            log.info("🔍 Getting ALL schedules for teacher {}", teacherId);
            
            // Try native query first
            try {
                List<Schedule> schedules = scheduleRepository.findByTeacherIdNative(teacherId);
                log.info("✅ Found {} total schedules (native)", schedules.size());
                
                if (!schedules.isEmpty()) {
                    logFirstSchedule(schedules.get(0), "All Schedules Native");
                }
                
                return schedules;
                
            } catch (Exception e) {
                log.error("❌ Native findByTeacherId failed: {}", e.getMessage());
            }

            // Fallback to JPQL with fetch
            try {
                List<Schedule> schedules = scheduleRepository.findByTeacherIdWithFetch(teacherId);
                log.info("✅ Found {} total schedules (JPQL with fetch)", schedules.size());
                return schedules;
                
            } catch (Exception e) {
                log.error("❌ JPQL with fetch failed: {}", e.getMessage());
            }

            // Last resort - original JPQL
            List<Schedule> schedules = scheduleRepository.findByTeacherId(teacherId);
            log.info("✅ Found {} total schedules (original JPQL)", schedules.size());
            return schedules;
            
        } catch (Exception e) {
            log.error("❌ Error getting all teacher schedules: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Schedule> getTeacherSchedulesForDto(Long teacherId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return scheduleRepository.findByTeacherIdAndDateRange(teacherId, startDateTime, endDateTime);
    }

    /**
     * Utility method to log schedule details
     */
    private void logFirstSchedule(Schedule schedule, String queryType) {
        if (schedule != null) {
            log.info("   {} - First schedule: ID={}, Title='{}', Start={}, Teacher={}", 
                queryType,
                schedule.getId(), 
                schedule.getTitle(), 
                schedule.getStartDatetime(),
                schedule.getTeacherId());
        }
    }
}