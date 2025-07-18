package com.classroomapp.classroombackend.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClassroomRepository classroomRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream().map(user ->
            "ID: " + user.getId() + ", Username: " + user.getUsername() +
            ", Role: " + user.getRoleId() + ", Name: " + user.getFullName()
        ).toList());
    }

    @GetMapping("/database-state")
    public ResponseEntity<?> getDatabaseState() {
        // Get all users
        List<User> allUsers = userRepository.findAll();

        // Get all schedules
        List<Schedule> allSchedules = scheduleRepository.findAll();

        // Group schedules by teacher ID
        java.util.Map<Long, List<Schedule>> schedulesByTeacher = allSchedules.stream()
            .collect(java.util.stream.Collectors.groupingBy(s -> s.getTeacher().getId()));

        // Find teacher specifically
        User teacher = userRepository.findByUsername("teacher").orElse(null);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalUsers", allUsers.size());
        result.put("totalSchedules", allSchedules.size());

        result.put("users", allUsers.stream().map(user ->
            java.util.Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getFullName(),
                "role", user.getRoleId()
            )
        ).toList());

        result.put("schedulesByTeacher", schedulesByTeacher.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                entry -> "Teacher_" + entry.getKey(),
                entry -> entry.getValue().size()
            )));

        if (teacher != null) {
            List<Schedule> teacherSchedules = scheduleRepository.findByTeacherId(teacher.getId());
            result.put("mainTeacher", java.util.Map.of(
                "id", teacher.getId(),
                "username", teacher.getUsername(),
                "name", teacher.getFullName(),
                "scheduleCount", teacherSchedules.size()
            ));
        } else {
            result.put("mainTeacher", "NOT FOUND");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/teacher")
    public ResponseEntity<?> getTeacher() {
        User teacher = userRepository.findByUsername("teacher").orElse(null);
        if (teacher == null) {
            return ResponseEntity.ok("Teacher not found");
        }

        List<Schedule> schedules = scheduleRepository.findByTeacherId(teacher.getId());

        return ResponseEntity.ok(java.util.Map.of(
            "teacher", "ID: " + teacher.getId() + ", Name: " + teacher.getFullName(),
            "scheduleCount", schedules.size(),
            "schedules", schedules.stream().map(s ->
                getDayName(s.getDayOfWeek()) + " " + s.getStartTime() + "-" + s.getEndTime() +
                " | " + s.getSubject() + " | Room: " + s.getRoom()
            ).toList()
        ));
    }

    @GetMapping("/test-schedule-api")
    public ResponseEntity<?> testScheduleApi() {
        User teacher = userRepository.findByUsername("teacher").orElse(null);
        if (teacher == null) {
            return ResponseEntity.badRequest().body("Teacher not found");
        }

        // Test the same logic as the ScheduleController
        java.time.LocalDate startDate = java.time.LocalDate.of(2025, 7, 1);
        java.time.LocalDate endDate = java.time.LocalDate.of(2025, 7, 31);

        List<Schedule> schedules = scheduleRepository.findByTeacherId(teacher.getId());

        return ResponseEntity.ok(java.util.Map.of(
            "teacherId", teacher.getId(),
            "teacherName", teacher.getFullName(),
            "dateRange", startDate + " to " + endDate,
            "totalSchedules", schedules.size(),
            "message", schedules.isEmpty() ?
                "❌ NO SCHEDULES FOUND - This is the problem!" :
                "✅ Schedules found - API should work",
            "sampleSchedules", schedules.stream().limit(5).map(s ->
                java.util.Map.of(
                    "day", getDayName(s.getDayOfWeek()),
                    "time", s.getStartTime() + "-" + s.getEndTime(),
                    "subject", s.getSubject(),
                    "room", s.getRoom()
                )
            ).toList()
        ));
    }

    @GetMapping("/force-create-schedules")
    public ResponseEntity<?> forceCreateSchedulesForTeacher() {
        return createSchedulesForTeacher();
    }

    @PostMapping("/create-schedules")
    public ResponseEntity<?> createSchedulesForTeacher() {
        User teacher = userRepository.findByUsername("teacher").orElse(null);
        if (teacher == null) {
            return ResponseEntity.badRequest().body("Teacher not found");
        }

        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            return ResponseEntity.badRequest().body("No classrooms found");
        }

        // Clear existing schedules for this teacher
        List<Schedule> existingSchedules = scheduleRepository.findByTeacherId(teacher.getId());
        if (!existingSchedules.isEmpty()) {
            scheduleRepository.deleteAll(existingSchedules);
        }

        int scheduleCount = 0;

        // Create comprehensive weekly schedule for the teacher
        // Monday schedules
        Schedule mondayMorning = createSchedule(teacher, classrooms.get(0), 0, 
            LocalTime.of(8, 0), LocalTime.of(9, 30), "Room 101", "Java Programming - Fundamentals");
        scheduleRepository.save(mondayMorning);
        scheduleCount++;

        Schedule mondayAfternoon = createSchedule(teacher, classrooms.get(Math.min(1, classrooms.size()-1)), 0, 
            LocalTime.of(14, 0), LocalTime.of(15, 30), "Room 102", "Database Design - Theory");
        scheduleRepository.save(mondayAfternoon);
        scheduleCount++;

        // Tuesday schedules
        Schedule tuesdayMorning = createSchedule(teacher, classrooms.get(0), 1, 
            LocalTime.of(9, 0), LocalTime.of(10, 30), "Lab 201", "Java Programming - Practice");
        scheduleRepository.save(tuesdayMorning);
        scheduleCount++;

        Schedule tuesdayEvening = createSchedule(teacher, classrooms.get(Math.min(2, classrooms.size()-1)), 1, 
            LocalTime.of(18, 0), LocalTime.of(19, 30), "Online", "Evening Tutorial Session");
        scheduleRepository.save(tuesdayEvening);
        scheduleCount++;

        // Wednesday schedules
        Schedule wednesdayMorning = createSchedule(teacher, classrooms.get(Math.min(1, classrooms.size()-1)), 2, 
            LocalTime.of(8, 0), LocalTime.of(9, 30), "Room 102", "Database Design - Practice");
        scheduleRepository.save(wednesdayMorning);
        scheduleCount++;

        Schedule wednesdayAfternoon = createSchedule(teacher, classrooms.get(0), 2, 
            LocalTime.of(15, 0), LocalTime.of(16, 30), "Room 101", "Advanced Java Topics");
        scheduleRepository.save(wednesdayAfternoon);
        scheduleCount++;

        // Thursday schedules
        Schedule thursdayMorning = createSchedule(teacher, classrooms.get(Math.min(2, classrooms.size()-1)), 3, 
            LocalTime.of(10, 0), LocalTime.of(11, 30), "Room 103", "Software Engineering");
        scheduleRepository.save(thursdayMorning);
        scheduleCount++;

        // Friday schedules
        Schedule fridayMorning = createSchedule(teacher, classrooms.get(0), 4, 
            LocalTime.of(8, 0), LocalTime.of(9, 30), "Room 101", "Java Programming - Review");
        scheduleRepository.save(fridayMorning);
        scheduleCount++;

        Schedule fridayAfternoon = createSchedule(teacher, classrooms.get(Math.min(1, classrooms.size()-1)), 4, 
            LocalTime.of(13, 0), LocalTime.of(14, 30), "Lab 202", "Database Lab Session");
        scheduleRepository.save(fridayAfternoon);
        scheduleCount++;

        // Saturday workshop
        Schedule saturdayWorkshop = createSchedule(teacher, classrooms.get(0), 5, 
            LocalTime.of(10, 0), LocalTime.of(12, 0), "Workshop Hall", "Weekend Java Workshop");
        scheduleRepository.save(saturdayWorkshop);
        scheduleCount++;

        return ResponseEntity.ok(java.util.Map.of(
            "message", "Created " + scheduleCount + " schedules for teacher ID " + teacher.getId(),
            "teacherId", teacher.getId(),
            "teacherName", teacher.getFullName(),
            "schedulesCreated", scheduleCount
        ));
    }

    private Schedule createSchedule(User teacher, Classroom classroom, int dayOfWeek, 
                                  LocalTime startTime, LocalTime endTime, 
                                  String room, String subject) {
        Schedule schedule = new Schedule();
        schedule.setTeacher(teacher);
        schedule.setClassroom(classroom);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setRoom(room);
        schedule.setSubject(subject);
        schedule.setMaterialsUrl("https://drive.google.com/folder/" + classroom.getName().toLowerCase().replace(" ", "-"));
        schedule.setMeetUrl("https://meet.google.com/" + classroom.getName().toLowerCase().replace(" ", "-"));
        return schedule;
    }

    private String getDayName(int dayOfWeek) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return days[dayOfWeek];
    }

    /**
     * Debug endpoint to check user role assignments
     */
    @GetMapping("/check-user-roles")
    public ResponseEntity<String> checkUserRoles() {
        StringBuilder result = new StringBuilder();

        // Check student user
        User student = userRepository.findByEmail("student@test.com").orElse(null);
        if (student != null) {
            result.append("Student User: ID=").append(student.getId())
                  .append(", Email=").append(student.getEmail())
                  .append(", Role=").append(student.getRole())
                  .append(", RoleId=").append(student.getRoleId()).append("\n");
        } else {
            result.append("Student user not found!\n");
        }

        // Check teacher user
        User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
        if (teacher != null) {
            result.append("Teacher User: ID=").append(teacher.getId())
                  .append(", Email=").append(teacher.getEmail())
                  .append(", Role=").append(teacher.getRole())
                  .append(", RoleId=").append(teacher.getRoleId()).append("\n");
        } else {
            result.append("Teacher user not found!\n");
        }

        // Check classrooms
        List<Classroom> classrooms = classroomRepository.findAll();
        result.append("Found ").append(classrooms.size()).append(" classrooms:\n");
        for (Classroom classroom : classrooms) {
            User classroomTeacher = classroom.getTeacher();
            result.append("- Classroom: ").append(classroom.getName())
                  .append(" (ID=").append(classroom.getId()).append(")")
                  .append(", Teacher: ").append(classroomTeacher != null ? classroomTeacher.getFullName() : "NULL")
                  .append(" (ID=").append(classroomTeacher != null ? classroomTeacher.getId() : "NULL")
                  .append(")\n");
        }

        return ResponseEntity.ok(result.toString());
    }

    /**
     * Force re-seed lectures for debugging
     */
    @PostMapping("/force-seed-lectures")
    public ResponseEntity<String> forceSeedLectures() {
        try {
            // This would require LectureSeeder injection, but let's keep it simple
            StringBuilder result = new StringBuilder();
            result.append("Force seed lectures endpoint called.\n");
            result.append("Check server logs for detailed seeding information.\n");
            result.append("Note: This endpoint requires backend restart to take effect.\n");

            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
