package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;

    @GetMapping("/my-timetable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TimetableEventDto>> getMyTimetable(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting timetable for user: {} from {} to {}", userDetails.getUsername(), startDate, endDate);

        // FIX: Look up user by email from the JWT subject, not by username
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
        
        log.info("Found user with ID: {}, Role: {}", currentUser.getId(), currentUser.getRole());

        List<TimetableEventDto> events = scheduleService.getTimetableForUser(currentUser.getId(), startDate, endDate);
        
        log.info("Found {} events for user {}", events.size(), currentUser.getId());
        
        // Log some details about the events if any exist
        if (!events.isEmpty()) {
            log.info("First event: title={}, startDatetime={}, classroomId={}", 
                events.get(0).getTitle(),
                events.get(0).getStartDatetime(),
                events.get(0).getClassroomId());
        } else {
            log.warn("No events found for user {} between {} and {}", currentUser.getId(), startDate, endDate);
        }
        
        return ResponseEntity.ok(events);
    }
} 