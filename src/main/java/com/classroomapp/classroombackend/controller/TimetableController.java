package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.CreateEventDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.service.TimetableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    // Root endpoint for timetable view
    @GetMapping
    public ResponseEntity<List<TimetableEventDto>> getTimetable(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "month") String view) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusMonths(1).minusDays(1);
        
        // Convert to LocalDateTime for service
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        
        // Use the service to get real data
        List<TimetableEventDto> events = timetableService.getEventsByDateRange(startDateTime, endDateTime);
        return ResponseEntity.ok(events);
    }

    // Get user's timetable
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TimetableEventDto>> getUserTimetable(
            @PathVariable Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusMonths(1).minusDays(1);
        
        // Convert to LocalDateTime for service
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        
        // In a real implementation, we would get the classrooms for the user and fetch events
        // For now, just get all events
        List<TimetableEventDto> events = timetableService.getEventsByDateRange(startDateTime, endDateTime);
        return ResponseEntity.ok(events);
    }
    
    // Get timetable for the current authenticated user
    @GetMapping("/my-timetable")
    public ResponseEntity<List<TimetableEventDto>> getMyTimetable(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // In a real implementation, we would get the current user ID from the authentication
        // For now, just get all events
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusMonths(1).minusDays(1);
        
        // Convert to LocalDateTime for service
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        
        List<TimetableEventDto> events = timetableService.getEventsByDateRange(startDateTime, endDateTime);
        return ResponseEntity.ok(events);
    }
    
    // Get weekly timetable
    @GetMapping("/user/{userId}/week")
    public ResponseEntity<List<TimetableEventDto>> getWeeklyTimetable(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate) {
        
        LocalDate endDate = startDate.plusDays(6);
        
        // Convert to LocalDateTime for service
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<TimetableEventDto> events = timetableService.getEventsByDateRange(startDateTime, endDateTime);
        return ResponseEntity.ok(events);
    }
    
    // Get daily timetable
    @GetMapping("/user/{userId}/day")
    public ResponseEntity<List<TimetableEventDto>> getDailyTimetable(
            @PathVariable Long userId,
            @RequestParam LocalDate date) {
        
        // Convert to LocalDateTime for service
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(23, 59, 59);
        
        List<TimetableEventDto> events = timetableService.getEventsByDateRange(startDateTime, endDateTime);
        return ResponseEntity.ok(events);
    }
    
    // Get upcoming events
    @GetMapping("/upcoming")
    public ResponseEntity<List<TimetableEventDto>> getUpcomingEvents() {
        List<TimetableEventDto> events = timetableService.getUpcomingEvents(null);
        return ResponseEntity.ok(events);
    }
    
    // Create event
    @PostMapping("/events")
    public ResponseEntity<TimetableEventDto> createEvent(
            @Valid @RequestBody CreateEventDto createEventDto,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // In a real implementation, we would get the current user ID from the authentication
        // For now, use a placeholder user ID
        Long createdBy = 1L;
        
        TimetableEventDto createdEvent = timetableService.createEvent(createEventDto, createdBy);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }
    
    // Update event
    @PutMapping("/events/{eventId}")
    public ResponseEntity<TimetableEventDto> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventDto updateEventDto) {
        
        TimetableEventDto updatedEvent = timetableService.updateEvent(eventId, updateEventDto);
        return ResponseEntity.ok(updatedEvent);
    }
    
    // Delete event
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        timetableService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
    
    // Get event by ID
    @GetMapping("/events/{eventId}")
    public ResponseEntity<TimetableEventDto> getEventById(@PathVariable Long eventId) {
        TimetableEventDto event = timetableService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }
    
    // Add test data (temporary endpoint)
    @GetMapping("/test-data")
    public ResponseEntity<String> addTestData() {
        try {
            // Sample timetable events for June 2025
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            
            // Create a math class event
            CreateEventDto mathClass = new CreateEventDto();
            mathClass.setTitle("Math Class");
            mathClass.setDescription("Regular math class for first-year students");
            mathClass.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, 15, 9, 0));
            mathClass.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, 15, 10, 30));
            mathClass.setEventType("CLASS");
            mathClass.setClassroomId(1L);
            mathClass.setLocation("Room 101");
            mathClass.setIsAllDay(false);
            mathClass.setColor("#007bff");
            timetableService.createEvent(mathClass, 1L);
            
            // Create a history exam event
            CreateEventDto historyExam = new CreateEventDto();
            historyExam.setTitle("History Exam");
            historyExam.setDescription("Mid-term history exam");
            historyExam.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, 17, 13, 0));
            historyExam.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, 17, 15, 0));
            historyExam.setEventType("EXAM");
            historyExam.setClassroomId(1L);
            historyExam.setLocation("Exam Hall A");
            historyExam.setIsAllDay(false);
            historyExam.setColor("#dc3545");
            timetableService.createEvent(historyExam, 1L);
            
            // Create a science project meeting event
            CreateEventDto scienceMeeting = new CreateEventDto();
            scienceMeeting.setTitle("Science Project Meeting");
            scienceMeeting.setDescription("Meeting to discuss science project progress");
            scienceMeeting.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, 19, 14, 0));
            scienceMeeting.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, 19, 15, 30));
            scienceMeeting.setEventType("MEETING");
            scienceMeeting.setClassroomId(2L);
            scienceMeeting.setLocation("Room 202");
            scienceMeeting.setIsAllDay(false);
            scienceMeeting.setColor("#28a745");
            timetableService.createEvent(scienceMeeting, 2L);
            
            // Create a literature assignment due event
            CreateEventDto literatureAssignment = new CreateEventDto();
            literatureAssignment.setTitle("Literature Assignment Due");
            literatureAssignment.setDescription("Submit literature essay");
            literatureAssignment.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, 20, 23, 59));
            literatureAssignment.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, 20, 23, 59));
            literatureAssignment.setEventType("ASSIGNMENT_DUE");
            literatureAssignment.setClassroomId(3L);
            literatureAssignment.setIsAllDay(true);
            literatureAssignment.setColor("#ffc107");
            timetableService.createEvent(literatureAssignment, 3L);
            
            // Create a holiday event
            CreateEventDto holiday = new CreateEventDto();
            holiday.setTitle("School Holiday");
            holiday.setDescription("National holiday - no classes");
            holiday.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, 22, 0, 0));
            holiday.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, 22, 23, 59));
            holiday.setEventType("HOLIDAY");
            holiday.setIsAllDay(true);
            holiday.setColor("#6c757d");
            timetableService.createEvent(holiday, 1L);
            
            return ResponseEntity.ok("Test data added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding test data: " + e.getMessage());
        }
    }

    // Get events for a classroom
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<TimetableEventDto>> getClassroomEvents(
            @PathVariable Long classroomId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusMonths(1).minusDays(1);
        
        // Convert to LocalDateTime for service
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        
        List<TimetableEventDto> events = timetableService.getEventsByClassroomAndDateRange(
                classroomId, startDateTime, endDateTime);
        return ResponseEntity.ok(events);
    }
}
