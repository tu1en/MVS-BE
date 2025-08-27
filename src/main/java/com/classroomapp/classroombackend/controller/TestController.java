package com.classroomapp.classroombackend.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.classroomapp.classroombackend.dto.CreateEventDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassService;
import com.classroomapp.classroombackend.service.ScheduleService;
import com.classroomapp.classroombackend.service.TimetableService;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.config.CourseTemplateSeeder;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @Autowired
    private ClassService classService;

    @Autowired
    private TimetableEventRepository timetableEventRepository;

    @Autowired
    private TimetableService timetableService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private CourseTemplateSeeder courseTemplateSeeder;

    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }

    @GetMapping("/debug-class-ids")
    public ResponseEntity<Map<String, Object>> debugClassIds() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get all class IDs from database
            List<ClassEntity> allClasses = classRepository.findAll();
            List<Long> allIds = allClasses.stream().map(ClassEntity::getId).collect(Collectors.toList());

            // Check specific problematic IDs
            Long[] problematicIds = {2L, 6L, 10L, 14L};
            Map<String, Object> results = new HashMap<>();

            for (Long id : problematicIds) {
                boolean exists = allIds.contains(id);
                results.put("id_" + id, Map.of(
                    "exists", exists,
                    "message", exists ? "Found in database" : "NOT FOUND in database"
                ));
            }

            response.put("allClassIds", allIds);
            response.put("totalClasses", allClasses.size());
            response.put("problematicIds", results);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/check-classes")
    public ResponseEntity<Map<String, Object>> checkClasses() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ClassEntity> allClasses = classRepository.findAll();
            response.put("totalClasses", allClasses.size());
            response.put("classIds", allClasses.stream().map(ClassEntity::getId).collect(Collectors.toList()));
            response.put("classDetails", allClasses.stream().map(c ->
                Map.of("id", c.getId(), "name", c.getClassName(), "status", c.getStatus())
            ).collect(Collectors.toList()));
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/check-specific-classes")
    public ResponseEntity<Map<String, Object>> checkSpecificClasses() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check the specific class IDs that are failing: 2, 6, 10, 14
            Long[] problematicIds = {2L, 6L, 10L, 14L};
            Map<String, Object> results = new HashMap<>();

            for (Long id : problematicIds) {
                Map<String, Object> classInfo = new HashMap<>();
                Optional<ClassEntity> classEntity = classRepository.findById(id);

                if (classEntity.isPresent()) {
                    ClassEntity entity = classEntity.get();
                    classInfo.put("exists", true);
                    classInfo.put("name", entity.getClassName());
                    classInfo.put("status", entity.getStatus());
                    classInfo.put("teacher", entity.getTeacher() != null ? entity.getTeacher().getFullName() : "NULL");
                } else {
                    classInfo.put("exists", false);
                    classInfo.put("error", "Class not found in database");
                }

                results.put("class_" + id, classInfo);
            }

            response.put("problematicClasses", results);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/sql-debug")
    public ResponseEntity<Map<String, Object>> sqlDebug() {
        Map<String, Object> response = new HashMap<>();
        try {
            // SQL queries để debug
            response.put("sqlQueries", Map.of(
                "checkTeacherUser", "SELECT id, username, email, full_name, role_id FROM users WHERE email = 'teacher@test.com' OR username = 'teacher';",
                "checkClassEntities", "SELECT id, class_name, teacher_id, status, created_at FROM class_entity ORDER BY id;",
                "checkClassrooms", "SELECT id, name, teacher_id, created_at FROM classrooms ORDER BY id;",
                "checkTimetableEvents", "SELECT id, classroom_id, title, start_time, end_time, event_type FROM timetable_events WHERE classroom_id IN (SELECT id FROM classrooms WHERE teacher_id = (SELECT id FROM users WHERE email = 'teacher@test.com')) ORDER BY start_time;",
                "checkTeacherClassrooms", "SELECT c.id, c.name, c.teacher_id, u.full_name as teacher_name FROM classrooms c LEFT JOIN users u ON c.teacher_id = u.id WHERE c.teacher_id = (SELECT id FROM users WHERE email = 'teacher@test.com');",
                "checkSyncStatus", "SELECT ce.id as class_entity_id, ce.class_name, ce.teacher_id, cr.id as classroom_id, cr.name as classroom_name FROM class_entity ce LEFT JOIN classrooms cr ON ce.id = cr.id ORDER BY ce.id;",
                "countTimetableByTeacher", "SELECT COUNT(*) as total_events FROM timetable_events te JOIN classrooms c ON te.classroom_id = c.id WHERE c.teacher_id = (SELECT id FROM users WHERE email = 'teacher@test.com');"
            ));
            response.put("success", true);
            response.put("message", "Copy các SQL queries này và chạy trong database client để debug");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/sync-data")
    public ResponseEntity<Map<String, Object>> testDataSync() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🧪 TEST SYNC: Starting data synchronization test...");

            // Trigger sync all classes to classrooms
            classService.syncAllClassesToClassrooms();

            response.put("success", true);
            response.put("message", "Data sync completed successfully");
            response.put("timestamp", LocalDateTime.now());

            System.out.println("✅ TEST SYNC: Data synchronization completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TEST SYNC failed: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/create-attendance-session")
    public ResponseEntity<Map<String, Object>> createTestAttendanceSession(@RequestBody Map<String, Object> request) {
        try {
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            Long classroomId = Long.valueOf(request.get("classroomId").toString());
            String lectureTitle = (String) request.get("lectureTitle");

            // Find teacher and classroom
            User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

            // Create lecture first
            Lecture lecture = new Lecture();
            lecture.setTitle(lectureTitle);
            lecture.setClassroom(classroom);
            lecture.setCreatedAt(LocalDateTime.now());
            lecture = lectureRepository.save(lecture);

            // Create attendance session
            AttendanceSession session = new AttendanceSession();
            session.setClassroom(classroom);
            session.setLecture(lecture);
            session.setSessionDate(LocalDate.now());
            session.setStartTime(Instant.now());
            session.setEndTime(Instant.now().plusSeconds(5400)); // 1.5 hours
            session.setStatus(AttendanceSession.SessionStatus.CLOSED);
            session.setCreatedAt(LocalDateTime.now());
            session = attendanceSessionRepository.save(session);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("lectureId", lecture.getId());
            response.put("message", "Attendance session created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/count")
    public Long getUserCount() {
        return userRepository.count();
    }

    @GetMapping("/users/teacher")
    public User getTeacherUser() {
        return userRepository.findByEmail("teacher@test.com").orElse(null);
    }

    @PostMapping("/sync-lectures-to-timetable")
    public ResponseEntity<Map<String, Object>> syncLecturesToTimetable() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🧪 TEST: Starting Lectures to TimetableEvents sync...");

            // Trigger sync of all lectures to timetable events
            classService.syncAllLecturesToTimetableEvents();

            response.put("success", true);
            response.put("message", "Lectures to TimetableEvents sync completed successfully");
            response.put("timestamp", LocalDateTime.now());

            System.out.println("✅ TEST: Lectures to TimetableEvents sync completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ TEST: Error during Lectures to TimetableEvents sync: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Lectures to TimetableEvents sync failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-timetable-events")
    public ResponseEntity<Map<String, Object>> checkTimetableEvents() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🧪 TEST: Checking TimetableEvents...");

            // Get teacher user
            User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Teacher not found");
                return ResponseEntity.ok(response);
            }

            // Get teacher's classrooms
            List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());

            // Count total TimetableEvents for teacher's classrooms
            long totalEvents = 0;
            for (Classroom classroom : classrooms) {
                List<TimetableEvent> events = timetableEventRepository.findByClassroomId(classroom.getId());
                totalEvents += events.size();
                System.out.println("📅 Classroom '" + classroom.getName() + "' has " + events.size() + " TimetableEvents");
            }

            response.put("success", true);
            response.put("teacherId", teacher.getId());
            response.put("teacherEmail", teacher.getEmail());
            response.put("classroomCount", classrooms.size());
            response.put("totalTimetableEvents", totalEvents);
            response.put("message", "Found " + totalEvents + " TimetableEvents for teacher's " + classrooms.size() + " classrooms");
            response.put("timestamp", LocalDateTime.now());

            System.out.println("✅ TEST: Found " + totalEvents + " TimetableEvents for teacher");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ TEST: Error checking TimetableEvents: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error checking TimetableEvents: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/teacher-timetable-test")
    public ResponseEntity<Map<String, Object>> testTeacherTimetable(
            @RequestParam(required = false, defaultValue = "2025-08-18") String startDate,
            @RequestParam(required = false, defaultValue = "2025-08-24") String endDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🧪 TEST: Testing teacher timetable for date range: " + startDate + " to " + endDate);

            // Get teacher user
            User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Teacher not found");
                return ResponseEntity.ok(response);
            }

            // Debug: Check timetable events directly
            System.out.println("🧪 TEST: Checking TimetableEvents for teacher ID: " + teacher.getId());

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(23, 59, 59);

            List<TimetableEventDto> events = timetableService.getEventsForUser(teacher.getId(), startDateTime, endDateTime);

            System.out.println("🧪 TEST: Found " + events.size() + " events");
            for (TimetableEventDto event : events) {
                System.out.println("🧪 Event ID: " + event.getId() +
                                 ", Title: " + event.getTitle() +
                                 ", ClassroomId: " + event.getClassroomId() +
                                 ", LectureId: " + event.getLectureId() +
                                 ", StartTime: " + event.getStartDatetime());
            }

            response.put("success", true);
            response.put("teacherId", teacher.getId());
            response.put("eventsCount", events.size());
            response.put("events", events);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ TEST: Error - " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/create-timetable-test-data")
    public ResponseEntity<Map<String, Object>> createTimetableTestData() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🧪 TEST: Creating timetable test data...");

            // Get teacher user
            User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Teacher not found");
                return ResponseEntity.ok(response);
            }

            // Get teacher's classrooms
            List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
            if (classrooms.isEmpty()) {
                response.put("success", false);
                response.put("message", "No classrooms found for teacher");
                return ResponseEntity.ok(response);
            }

            Classroom classroom = classrooms.get(0); // Use first classroom

            // Create test timetable events for the week 2025-08-18 to 2025-08-24
            LocalDateTime baseDate = LocalDateTime.of(2025, 8, 18, 9, 0); // Monday 9:00 AM

            for (int i = 0; i < 5; i++) { // Monday to Friday
                LocalDateTime startTime = baseDate.plusDays(i);
                LocalDateTime endTime = startTime.plusHours(2); // 2-hour classes

                CreateEventDto eventDto = new CreateEventDto();
                eventDto.setTitle("Bài " + (i + 1) + ": Từ vựng cơ bản - " + classroom.getName());
                eventDto.setDescription("Học từ vựng về thành viên gia đình và nghề nghiệp");
                eventDto.setStartDatetime(startTime);
                eventDto.setEndDatetime(endTime);
                eventDto.setEventType("CLASS");
                eventDto.setClassroomId(classroom.getId());
                eventDto.setLocation("Phòng học " + (i + 1));
                eventDto.setColor("#1890ff");

                // Create the event
                TimetableEventDto createdEvent = timetableService.createEvent(eventDto, teacher.getId());
                System.out.println("🧪 Created event: ID=" + createdEvent.getId() +
                                 ", Title=" + createdEvent.getTitle() +
                                 ", ClassroomId=" + createdEvent.getClassroomId());
            }

            response.put("success", true);
            response.put("message", "Created 5 test timetable events for teacher");
            response.put("teacherId", teacher.getId());
            response.put("classroomId", classroom.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ TEST: Error creating timetable test data - " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/check-schedule-data")
    public ResponseEntity<Map<String, Object>> checkScheduleData() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("🧪 TEST: Checking Schedule data...");

            // Get teacher user
            User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Teacher not found");
                return ResponseEntity.ok(response);
            }

            // Check schedules using ScheduleService
            LocalDate start = LocalDate.of(2025, 8, 18);
            LocalDate end = LocalDate.of(2025, 8, 24);

            List<TimetableEventDto> schedules = scheduleService.getTimetableForUser(teacher.getId(), start, end);

            System.out.println("🧪 TEST: Found " + schedules.size() + " schedules from ScheduleService");
            for (TimetableEventDto schedule : schedules) {
                System.out.println("🧪 Schedule ID: " + schedule.getId() +
                                 ", Title: " + schedule.getTitle() +
                                 ", ClassroomId: " + schedule.getClassroomId() +
                                 ", LectureId: " + schedule.getLectureId() +
                                 ", StartTime: " + schedule.getStartDatetime());
            }

            response.put("success", true);
            response.put("teacherId", teacher.getId());
            response.put("schedulesCount", schedules.size());
            response.put("schedules", schedules);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ TEST: Error testing teacher timetable: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Error testing teacher timetable: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/create-lectures/{classroomId}")
    public ResponseEntity<Map<String, Object>> createLecturesForClassroom(@PathVariable Long classroomId) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🧪 TEST: Creating lectures for classroom ID: " + classroomId);

            // Tìm classroom
            Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
            if (classroom == null) {
                response.put("success", false);
                response.put("message", "Classroom not found with ID: " + classroomId);
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("🧪 TEST: Found classroom: " + classroom.getName());

            // Kiểm tra lectures hiện tại
            List<Lecture> existingLectures = lectureRepository.findByClassroomId(classroomId);
            System.out.println("🧪 TEST: Existing lectures count: " + existingLectures.size());

            // Gọi method createJapaneseLectures
            // courseTemplateSeeder.createJapaneseLectures(classroom);

            // Kiểm tra lại sau khi tạo
            List<Lecture> newLectures = lectureRepository.findByClassroomId(classroomId);
            System.out.println("🧪 TEST: New lectures count: " + newLectures.size());

            response.put("success", true);
            response.put("message", "Successfully called createJapaneseLectures for classroom: " + classroom.getName());
            response.put("classroomId", classroomId);
            response.put("classroomName", classroom.getName());
            response.put("existingLecturesCount", existingLectures.size());
            response.put("newLecturesCount", newLectures.size());
            response.put("lecturesCreated", newLectures.size() - existingLectures.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ TEST: Error calling createJapaneseLectures: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/classrooms")
    public ResponseEntity<Map<String, Object>> getAllClassrooms() {
        Map<String, Object> response = new HashMap<>();

        try {
            var classrooms = classroomRepository.findAll();
            response.put("success", true);
            response.put("classrooms", classrooms);
            response.put("count", classrooms.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("❌ TEST: Error getting classrooms: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}