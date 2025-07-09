package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class TimetableEventSeeder {

    private final TimetableEventRepository timetableEventRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public TimetableEventSeeder(TimetableEventRepository timetableEventRepository, UserRepository userRepository, ClassroomRepository classroomRepository) {
        this.timetableEventRepository = timetableEventRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public void seed() {
        if (timetableEventRepository.count() > 0) {
            return;
        }

        List<User> users = userRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();

        if (users.isEmpty() || classrooms.isEmpty()) {
            System.out.println("⚠️ [TimetableEventSeeder] Not enough users or classrooms to seed events. Skipping.");
            return;
        }

        User teacher = users.get(1);  // teacher user
        User admin = users.get(3);    // admin user

        // Create events for a specific week: e.g., starting from today
        LocalDateTime weekStart = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0);

        // Monday - Mathematics Class 8:00-9:30
        TimetableEvent mathMonday = new TimetableEvent();
        mathMonday.setTitle("Advanced Mathematics");
        mathMonday.setDescription("Weekly mathematics class covering calculus and algebra topics");
        mathMonday.setStartDatetime(weekStart.withHour(8).withMinute(0)); // Monday this week
        mathMonday.setEndDatetime(weekStart.withHour(9).withMinute(30));
        mathMonday.setEventType(TimetableEvent.EventType.CLASS);
        mathMonday.setClassroomId(classrooms.get(0).getId());
        mathMonday.setCreatedBy(teacher.getId());
        mathMonday.setLocation("Room 101");
        mathMonday.setColor("#52c41a");
        timetableEventRepository.save(mathMonday);

        // Tuesday - Physics Lab 10:00-11:30
        TimetableEvent physicsTuesday = new TimetableEvent();
        physicsTuesday.setTitle("Classical Physics Lab");
        physicsTuesday.setDescription("Hands-on physics experiments and lab work");
        physicsTuesday.setStartDatetime(weekStart.plusDays(1).withHour(10).withMinute(0)); // Tuesday
        physicsTuesday.setEndDatetime(weekStart.plusDays(1).withHour(11).withMinute(30));
        physicsTuesday.setEventType(TimetableEvent.EventType.CLASS);
        physicsTuesday.setClassroomId(classrooms.get(1).getId());
        physicsTuesday.setCreatedBy(teacher.getId());
        physicsTuesday.setLocation("Lab 201");
        physicsTuesday.setColor("#722ed1");
        timetableEventRepository.save(physicsTuesday);

        // Friday - Java Programming 13:00-14:30
        TimetableEvent javaFriday = new TimetableEvent();
        javaFriday.setTitle("Java Programming");
        javaFriday.setDescription("Object-oriented programming concepts and practical coding");
        javaFriday.setStartDatetime(weekStart.plusDays(4).withHour(13).withMinute(0)); // Friday
        javaFriday.setEndDatetime(weekStart.plusDays(4).withHour(14).withMinute(30));
        javaFriday.setEventType(TimetableEvent.EventType.CLASS);
        javaFriday.setClassroomId(classrooms.get(2).getId());
        javaFriday.setCreatedBy(admin.getId());
        javaFriday.setLocation("Computer Lab 301");
        javaFriday.setColor("#fa8c16");
        timetableEventRepository.save(javaFriday);

        // Add an exam event for next week
        LocalDateTime nextWeekStart = weekStart.plusWeeks(1);
        TimetableEvent mathExam = new TimetableEvent();
        mathExam.setTitle("Mathematics Midterm Exam");
        mathExam.setDescription("Comprehensive exam covering chapters 1-5 of calculus");
        mathExam.setStartDatetime(nextWeekStart.withHour(9).withMinute(0)); // Monday next week
        mathExam.setEndDatetime(nextWeekStart.withHour(11).withMinute(0));
        mathExam.setEventType(TimetableEvent.EventType.EXAM);
        mathExam.setClassroomId(classrooms.get(0).getId());
        mathExam.setCreatedBy(teacher.getId());
        mathExam.setLocation("Exam Hall A");
        mathExam.setColor("#f5222d");
        mathExam.setReminderMinutes(60);
        timetableEventRepository.save(mathExam);

        // Add an assignment due date
        TimetableEvent assignmentDue = new TimetableEvent();
        assignmentDue.setTitle("Calculus Problem Set Due");
        assignmentDue.setDescription("Submit completed calculus problem set from chapter 3");
        assignmentDue.setStartDatetime(weekStart.plusDays(3).withHour(23).withMinute(59)); // Wednesday
        assignmentDue.setEndDatetime(weekStart.plusDays(3).withHour(23).withMinute(59));
        assignmentDue.setEventType(TimetableEvent.EventType.ASSIGNMENT_DUE);
        assignmentDue.setClassroomId(classrooms.get(0).getId());
        assignmentDue.setCreatedBy(teacher.getId());
        assignmentDue.setLocation("Online Submission");
        assignmentDue.setColor("#faad14");
        assignmentDue.setReminderMinutes(1440); // 24 hours reminder
        timetableEventRepository.save(assignmentDue);

        System.out.println("✅ [TimetableEventSeeder] Created sample timetable events.");
    }
} 