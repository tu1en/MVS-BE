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
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.TimetableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;
    private final UserRepository userRepository;

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

        System.out.println("📅 TimetableController.getMyTimetable: Request received");
        System.out.println("   Authentication: " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("   Start Date: " + startDate);
        System.out.println("   End Date: " + endDate);

        if (authentication == null) {
            System.out.println("❌ TimetableController.getMyTimetable: No authentication provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get user ID from authentication
            String username = authentication.getName();
            System.out.println("📅 TimetableController.getMyTimetable: Username from auth: " + username);

            // Resolve real user from authentication
            User currentUser = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username).orElse(null));
            if (currentUser == null) {
                System.out.println("❌ TimetableController.getMyTimetable: Cannot resolve user from authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Long userId = currentUser.getId();

            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusMonths(1).minusDays(1);

            System.out.println("📅 TimetableController.getMyTimetable: Date range: " + start + " to " + end);
            System.out.println("📅 TimetableController.getMyTimetable: Getting events for user ID: " + userId);

            // Convert to LocalDateTime for service
            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(23, 59, 59);

            // Get events for the authenticated user using the new method
            List<TimetableEventDto> events = timetableService.getEventsForUser(userId, startDateTime, endDateTime);
            System.out.println("📅 TimetableController.getMyTimetable: Found " + events.size() + " events for user");

            return ResponseEntity.ok(events);
        } catch (Exception e) {
            System.out.println("❌ TimetableController.getMyTimetable: Error - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
    
    // Create sample data for a specific classroom
    @PostMapping("/create-sample-data/{classroomId}")
    public ResponseEntity<String> createSampleDataForClassroom(@PathVariable Long classroomId) {
        try {
            // Lấy thời gian hiện tại
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            int currentDay = now.getDayOfMonth();
            
            // Tạo lịch học trong tuần hiện tại và tuần tiếp theo
            
            // Buổi học lý thuyết
            CreateEventDto theoryClass = new CreateEventDto();
            theoryClass.setTitle("Bài giảng lý thuyết");
            theoryClass.setDescription("Giới thiệu các khái niệm cơ bản và lý thuyết nền tảng");
            theoryClass.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay, 8, 0));
            theoryClass.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay, 9, 30));
            theoryClass.setEventType("CLASS");
            theoryClass.setClassroomId(classroomId);
            theoryClass.setLocation("Phòng học 101");
            theoryClass.setIsAllDay(false);
            theoryClass.setColor("#007bff");
            timetableService.createEvent(theoryClass, 1L);
            
            // Buổi thực hành
            CreateEventDto practiceClass = new CreateEventDto();
            practiceClass.setTitle("Buổi thực hành");
            practiceClass.setDescription("Áp dụng kiến thức lý thuyết vào bài tập thực hành");
            practiceClass.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 2, 13, 0));
            practiceClass.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 2, 15, 30));
            practiceClass.setEventType("CLASS");
            practiceClass.setClassroomId(classroomId);
            practiceClass.setLocation("Phòng thực hành 202");
            practiceClass.setIsAllDay(false);
            practiceClass.setColor("#28a745");
            timetableService.createEvent(practiceClass, 1L);
            
            // Bài kiểm tra
            CreateEventDto examEvent = new CreateEventDto();
            examEvent.setTitle("Bài kiểm tra giữa kỳ");
            examEvent.setDescription("Kiểm tra kiến thức đã học trong nửa đầu khóa học");
            examEvent.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 7, 10, 0));
            examEvent.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 7, 11, 30));
            examEvent.setEventType("EXAM");
            examEvent.setClassroomId(classroomId);
            examEvent.setLocation("Phòng thi A");
            examEvent.setIsAllDay(false);
            examEvent.setColor("#dc3545");
            timetableService.createEvent(examEvent, 1L);
            
            // Hạn nộp bài tập
            CreateEventDto assignmentDue = new CreateEventDto();
            assignmentDue.setTitle("Hạn nộp bài tập lớn");
            assignmentDue.setDescription("Nộp báo cáo và mã nguồn của dự án");
            assignmentDue.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 10, 23, 59));
            assignmentDue.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 10, 23, 59));
            assignmentDue.setEventType("ASSIGNMENT_DUE");
            assignmentDue.setClassroomId(classroomId);
            assignmentDue.setIsAllDay(true);
            assignmentDue.setColor("#ffc107");
            timetableService.createEvent(assignmentDue, 1L);
            
            // Buổi hỏi đáp
            CreateEventDto meetingEvent = new CreateEventDto();
            meetingEvent.setTitle("Buổi hỏi đáp");
            meetingEvent.setDescription("Giải đáp thắc mắc và chuẩn bị cho kỳ thi cuối kỳ");
            meetingEvent.setStartDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 14, 15, 0));
            meetingEvent.setEndDatetime(LocalDateTime.of(currentYear, currentMonth, currentDay + 14, 16, 30));
            meetingEvent.setEventType("MEETING");
            meetingEvent.setClassroomId(classroomId);
            meetingEvent.setLocation("Phòng họp trực tuyến");
            meetingEvent.setIsAllDay(false);
            meetingEvent.setColor("#6f42c1");
            timetableService.createEvent(meetingEvent, 1L);
            
            return ResponseEntity.ok("Đã tạo dữ liệu lịch học mẫu thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
        }
    }
}
