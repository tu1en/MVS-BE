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
            return;
        }

        List<User> users = userRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();

        if (users.size() < 10 || classrooms.size() < 4) {
            System.out.println("⚠️ [ScheduleSeeder] Not enough users or classrooms to seed schedules. Skipping.");
            return;
        }

        User teacher1 = users.get(1);  // teacher user
        User teacher2 = users.get(8);  // Dr. Sarah Williams
        User teacher3 = users.get(9);  // Prof. Michael Brown
        User admin = users.get(3);     // admin user

        // Schedule 1: Mathematics - Monday 8:00-9:30
        Schedule mathSchedule = new Schedule();
        mathSchedule.setTeacher(teacher1);
        mathSchedule.setClassroom(classrooms.get(0)); // Math class
        mathSchedule.setDayOfWeek(0); // Monday
        mathSchedule.setStartTime(LocalTime.of(8, 0));
        mathSchedule.setEndTime(LocalTime.of(9, 30));
        mathSchedule.setRoom("Room 101");
        mathSchedule.setSubject("Advanced Mathematics");
        mathSchedule.setMaterialsUrl("https://drive.google.com/folder/math-materials");
        mathSchedule.setMeetUrl("https://meet.google.com/math-class");
        scheduleRepository.save(mathSchedule);

        // Schedule 2: Mathematics - Wednesday 8:00-9:30
        Schedule mathSchedule2 = new Schedule();
        mathSchedule2.setTeacher(teacher1);
        mathSchedule2.setClassroom(classrooms.get(0)); // Math class
        mathSchedule2.setDayOfWeek(2); // Wednesday
        mathSchedule2.setStartTime(LocalTime.of(8, 0));
        mathSchedule2.setEndTime(LocalTime.of(9, 30));
        mathSchedule2.setRoom("Room 101");
        mathSchedule2.setSubject("Advanced Mathematics");
        mathSchedule2.setMaterialsUrl("https://drive.google.com/folder/math-materials");
        mathSchedule2.setMeetUrl("https://meet.google.com/math-class");
        scheduleRepository.save(mathSchedule2);

        // Schedule 3: Physics - Tuesday 10:00-11:30
        Schedule physicsSchedule = new Schedule();
        physicsSchedule.setTeacher(teacher2);
        physicsSchedule.setClassroom(classrooms.get(1)); // Physics class
        physicsSchedule.setDayOfWeek(1); // Tuesday
        physicsSchedule.setStartTime(LocalTime.of(10, 0));
        physicsSchedule.setEndTime(LocalTime.of(11, 30));
        physicsSchedule.setRoom("Lab 201");
        physicsSchedule.setSubject("Classical Physics");
        physicsSchedule.setMaterialsUrl("https://drive.google.com/folder/physics-materials");
        physicsSchedule.setMeetUrl("https://meet.google.com/physics-class");
        scheduleRepository.save(physicsSchedule);

        // Schedule 4: Physics - Thursday 10:00-11:30
        Schedule physicsSchedule2 = new Schedule();
        physicsSchedule2.setTeacher(teacher2);
        physicsSchedule2.setClassroom(classrooms.get(1)); // Physics class
        physicsSchedule2.setDayOfWeek(3); // Thursday
        physicsSchedule2.setStartTime(LocalTime.of(10, 0));
        physicsSchedule2.setEndTime(LocalTime.of(11, 30));
        physicsSchedule2.setRoom("Lab 201");
        physicsSchedule2.setSubject("Classical Physics");
        physicsSchedule2.setMaterialsUrl("https://drive.google.com/folder/physics-materials");
        physicsSchedule2.setMeetUrl("https://meet.google.com/physics-class");
        scheduleRepository.save(physicsSchedule2);

        // Schedule 5: Java Programming - Friday 13:00-14:30
        Schedule csSchedule = new Schedule();
        csSchedule.setTeacher(admin);
        csSchedule.setClassroom(classrooms.get(2)); // CS class
        csSchedule.setDayOfWeek(4); // Friday
        csSchedule.setStartTime(LocalTime.of(13, 0));
        csSchedule.setEndTime(LocalTime.of(14, 30));
        csSchedule.setRoom("Computer Lab 301");
        csSchedule.setSubject("Java Programming");
        csSchedule.setMaterialsUrl("https://drive.google.com/folder/java-materials");
        csSchedule.setMeetUrl("https://meet.google.com/java-class");
        scheduleRepository.save(csSchedule);

        // Schedule 6: English Literature - Monday 14:00-15:30
        Schedule englishSchedule = new Schedule();
        englishSchedule.setTeacher(teacher3);
        englishSchedule.setClassroom(classrooms.get(3)); // English class
        englishSchedule.setDayOfWeek(0); // Monday
        englishSchedule.setStartTime(LocalTime.of(14, 0));
        englishSchedule.setEndTime(LocalTime.of(15, 30));
        englishSchedule.setRoom("Room 105");
        englishSchedule.setSubject("English Literature");
        englishSchedule.setMaterialsUrl("https://drive.google.com/folder/english-materials");
        englishSchedule.setMeetUrl("https://meet.google.com/english-class");
        scheduleRepository.save(englishSchedule);

        // Add special schedules from the old CreateManagerSchedules logic
        Schedule eveningMath = new Schedule();
        eveningMath.setClassroom(classrooms.get(0)); // Math Class
        eveningMath.setTeacher(teacher1);
        eveningMath.setDayOfWeek(4); // Friday
        eveningMath.setStartTime(LocalTime.of(18, 0));
        eveningMath.setEndTime(LocalTime.of(20, 0));
        eveningMath.setRoom("Online");
        eveningMath.setSubject(classrooms.get(0).getSubject());
        scheduleRepository.save(eveningMath);

        Schedule weekendPhysics = new Schedule();
        weekendPhysics.setClassroom(classrooms.get(1)); // Physics Class
        weekendPhysics.setTeacher(teacher2);
        weekendPhysics.setDayOfWeek(5); // Saturday
        weekendPhysics.setStartTime(LocalTime.of(9, 0));
        weekendPhysics.setEndTime(LocalTime.of(12, 0));
        weekendPhysics.setRoom("P301");
        weekendPhysics.setSubject(classrooms.get(1).getSubject());
        scheduleRepository.save(weekendPhysics);

        System.out.println("✅ [ScheduleSeeder] Created 6 standard schedules and 2 special schedules.");
    }
} 