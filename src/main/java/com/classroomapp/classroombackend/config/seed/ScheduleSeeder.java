package com.classroomapp.classroombackend.config.seed;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class ScheduleSeeder {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public ScheduleSeeder(ScheduleRepository scheduleRepository, UserRepository userRepository, ClassroomRepository classroomRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public void seed() {
        if (scheduleRepository.count() > 0) {
            System.out.println("✅ [ScheduleSeeder] Schedules already exist, skipping seeding.");
            return;
        }

        System.out.println("🔄 [ScheduleSeeder] No schedules found, starting seeding process...");

        List<Classroom> classrooms = classroomRepository.findAll();
        List<User> teachers = userRepository.findByRoleId(2); // Role ID for TEACHER

        if (classrooms.isEmpty() || teachers.isEmpty()) {
            System.out.println("⚠️ [ScheduleSeeder] Not enough classrooms or teachers to seed schedules. Skipping.");
            return;
        }

        // Find specific classrooms
        Classroom mathClass = findClassroomByPartialName(classrooms, "Toán");
        Classroom litClass = findClassroomByPartialName(classrooms, "Văn");
        Classroom engClass = findClassroomByPartialName(classrooms, "Anh");
        Classroom csClass = findClassroomByPartialName(classrooms, "Công nghệ");

        int scheduleCount = 0;

        // Seed schedules for Math class
        if (mathClass != null && mathClass.getTeacher() != null) {
            scheduleRepository.save(createSchedule(mathClass.getTeacher(), mathClass, 0, LocalTime.of(8, 0), LocalTime.of(9, 30), "Room 101", "Giải tích 1"));
            scheduleRepository.save(createSchedule(mathClass.getTeacher(), mathClass, 2, LocalTime.of(8, 0), LocalTime.of(9, 30), "Room 101", "Đại số tuyến tính"));
            scheduleCount += 2;
        }

        // Seed schedules for Literature class
        if (litClass != null && litClass.getTeacher() != null) {
            scheduleRepository.save(createSchedule(litClass.getTeacher(), litClass, 1, LocalTime.of(10, 0), LocalTime.of(11, 30), "Room 102", "Phân tích tác phẩm"));
            scheduleRepository.save(createSchedule(litClass.getTeacher(), litClass, 3, LocalTime.of(10, 0), LocalTime.of(11, 30), "Room 102", "Lý luận văn học"));
            scheduleCount += 2;
        }

        // Seed schedules for English class
        if (engClass != null && engClass.getTeacher() != null) {
            scheduleRepository.save(createSchedule(engClass.getTeacher(), engClass, 0, LocalTime.of(14, 0), LocalTime.of(15, 30), "Room 201", "Speaking & Listening"));
            scheduleRepository.save(createSchedule(engClass.getTeacher(), engClass, 4, LocalTime.of(14, 0), LocalTime.of(15, 30), "Room 201", "Reading & Writing"));
            scheduleCount += 2;
        }
        
        // Seed schedules for Computer Science class
        if (csClass != null && csClass.getTeacher() != null) {
            scheduleRepository.save(createSchedule(csClass.getTeacher(), csClass, 1, LocalTime.of(13, 0), LocalTime.of(14, 30), "Lab 301", "Lập trình hướng đối tượng"));
            scheduleRepository.save(createSchedule(csClass.getTeacher(), csClass, 4, LocalTime.of(13, 0), LocalTime.of(14, 30), "Lab 301", "Cấu trúc dữ liệu và giải thuật"));
            scheduleCount += 2;
        }

        if (scheduleCount > 0) {
            System.out.println("✅ [ScheduleSeeder] Successfully created " + scheduleCount + " schedules for specific classes.");
        } else {
            System.out.println("⚠️ [ScheduleSeeder] Could not find specific classrooms to seed schedules.");
        }
    }

    private Classroom findClassroomByPartialName(List<Classroom> classrooms, String partialName) {
        return classrooms.stream()
                .filter(c -> c.getName().contains(partialName))
                .findFirst()
                .orElse(null);
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
        schedule.setMaterialsUrl("https://docs.google.com/document/d/example");
        schedule.setMeetUrl("https://meet.google.com/lookup/example");
        return schedule;
    }

    private String getDayName(int dayOfWeek) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        if (dayOfWeek >= 0 && dayOfWeek < days.length) {
            return days[dayOfWeek];
        }
        return "Unknown";
    }
}