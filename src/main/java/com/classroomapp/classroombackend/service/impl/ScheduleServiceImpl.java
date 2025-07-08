package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ScheduleService;

import jakarta.transaction.Transactional;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;

    @Override
    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", id));
    }

    @Override
    public Schedule updateSchedule(Long id, Schedule schedule) {
        Schedule existingSchedule = getScheduleById(id);
        
        existingSchedule.setTeacher(schedule.getTeacher());
        existingSchedule.setClassroom(schedule.getClassroom());
        existingSchedule.setDayOfWeek(schedule.getDayOfWeek());
        existingSchedule.setStartTime(schedule.getStartTime());
        existingSchedule.setEndTime(schedule.getEndTime());
        existingSchedule.setRoom(schedule.getRoom());
        existingSchedule.setSubject(schedule.getSubject());
        existingSchedule.setMaterialsUrl(schedule.getMaterialsUrl());
        existingSchedule.setMeetUrl(schedule.getMeetUrl());
        
        return scheduleRepository.save(existingSchedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        Schedule schedule = getScheduleById(id);
        scheduleRepository.delete(schedule);
    }

    @Override
    public List<ScheduleDto> getSchedulesByClassroom(Long classroomId) {
        // Check if classroom exists
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        // Get schedules for classroom
        List<Schedule> schedules = scheduleRepository.findByClassroomId(classroomId);
        
        // Convert to DTOs
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getSchedulesByDay(Integer dayOfWeek) {
        // Validate day of week
        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw new IllegalArgumentException("Day of week must be between 0 and 6");
        }
        
        // Get schedules for day
        List<Schedule> schedules = scheduleRepository.findByDayOfWeek(dayOfWeek);
        
        // Convert to DTOs
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek) {
        // Check if teacher exists
        userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Validate day of week
        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw new IllegalArgumentException("Day of week must be between 0 and 6");
        }
        
        // Get schedules for teacher and day
        List<Schedule> schedules = scheduleRepository.findByTeacherIdAndDayOfWeek(teacherId, dayOfWeek);
        
        // Convert to DTOs
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ScheduleDto createScheduleEntry(ScheduleDto scheduleDto) {
        // Find teacher
        User teacher = userRepository.findById(scheduleDto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", scheduleDto.getTeacherId()));
        
        // Find classroom
        Classroom classroom = classroomRepository.findById(scheduleDto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", scheduleDto.getClassroomId()));
        
        // Parse time strings to LocalTime objects
        LocalTime startTime = LocalTime.parse(scheduleDto.getStart());
        LocalTime endTime = LocalTime.parse(scheduleDto.getEnd());
        
        // Create schedule entity
        Schedule schedule = new Schedule();
        schedule.setTeacher(teacher);
        schedule.setClassroom(classroom);
        schedule.setDayOfWeek(scheduleDto.getDay());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setRoom(scheduleDto.getRoom());
        schedule.setSubject(scheduleDto.getSubject());
        schedule.setMaterialsUrl(scheduleDto.getMaterialsUrl());
        schedule.setMeetUrl(scheduleDto.getMeetUrl());
        
        // Save schedule
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        // Return DTO of saved schedule
        return convertToDto(savedSchedule);
    }

    @Transactional
    @Override
    public ScheduleDto updateScheduleEntry(Long id, ScheduleDto scheduleDto) {
        // Find existing schedule
        Schedule existingSchedule = getScheduleById(id);
        
        // Find teacher if needed
        User teacher = existingSchedule.getTeacher();
        if (scheduleDto.getTeacherId() != null && !scheduleDto.getTeacherId().equals(existingSchedule.getTeacher().getId())) {
            teacher = userRepository.findById(scheduleDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", scheduleDto.getTeacherId()));
        }
        
        // Find classroom if needed
        Classroom classroom = existingSchedule.getClassroom();
        if (scheduleDto.getClassroomId() != null && !scheduleDto.getClassroomId().equals(existingSchedule.getClassroom().getId())) {
            classroom = classroomRepository.findById(scheduleDto.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", scheduleDto.getClassroomId()));
        }
        
        // Update schedule entity
        existingSchedule.setTeacher(teacher);
        existingSchedule.setClassroom(classroom);
        
        if (scheduleDto.getDay() != null) {
            existingSchedule.setDayOfWeek(scheduleDto.getDay());
        }
        
        if (scheduleDto.getStart() != null) {
            existingSchedule.setStartTime(LocalTime.parse(scheduleDto.getStart()));
        }
        
        if (scheduleDto.getEnd() != null) {
            existingSchedule.setEndTime(LocalTime.parse(scheduleDto.getEnd()));
        }
        
        if (scheduleDto.getRoom() != null) {
            existingSchedule.setRoom(scheduleDto.getRoom());
        }
        
        if (scheduleDto.getSubject() != null) {
            existingSchedule.setSubject(scheduleDto.getSubject());
        }
        
        if (scheduleDto.getMaterialsUrl() != null) {
            existingSchedule.setMaterialsUrl(scheduleDto.getMaterialsUrl());
        }
        
        if (scheduleDto.getMeetUrl() != null) {
            existingSchedule.setMeetUrl(scheduleDto.getMeetUrl());
        }
        
        // Save updated schedule
        Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
        
        // Return DTO of updated schedule
        return convertToDto(updatedSchedule);
    }

    @Override
    public List<TimetableEventDto> getTimetableForUser(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Schedule> userSchedules = new ArrayList<>();
        boolean isTeacher = user.getRole().equals("TEACHER");

        if (isTeacher) {
            userSchedules.addAll(scheduleRepository.findByTeacherId(userId));
        } else {
            List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findById_UserId(userId);
            List<Long> classroomIds = enrollments.stream()
                                                 .map(enrollment -> enrollment.getId().getClassroomId())
                                                 .collect(Collectors.toList());
            if (!classroomIds.isEmpty()) {
                userSchedules.addAll(scheduleRepository.findByClassroomIdIn(classroomIds));
            }
        }

        List<TimetableEventDto> events = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            for (Schedule schedule : userSchedules) {
                // DayOfWeek in Java's LocalDate is 1 (Monday) to 7 (Sunday).
                // Our schedule's dayOfWeek is 0 (Monday) to 6 (Sunday).
                if (currentDate.getDayOfWeek().getValue() -1 == schedule.getDayOfWeek()) {
                    LocalDateTime startDateTime = LocalDateTime.of(currentDate, schedule.getStartTime());
                    LocalDateTime endDateTime = LocalDateTime.of(currentDate, schedule.getEndTime());
                    
                    TimetableEventDto event = new TimetableEventDto();
                    event.setId(schedule.getId());
                    event.setTitle(schedule.getSubject());
                    event.setDescription("Class with " + schedule.getTeacher().getFullName() + " in room " + schedule.getRoom());
                    event.setStartDatetime(startDateTime);
                    event.setEndDatetime(endDateTime);
                    event.setEventType("CLASS_SESSION");
                    event.setClassroomId(schedule.getClassroom().getId());
                    event.setClassroomName(schedule.getClassroom().getName());
                    event.setLocation(schedule.getRoom());
                    event.setColor("#3788d8"); // Blue for classes
                    events.add(event);
                }
            }
        }

        return events;
    }
    
    // Helper method to convert Schedule entity to ScheduleDto
    private ScheduleDto convertToDto(Schedule schedule) {
        int studentCount = 0;
        try {
            studentCount = schedule.getClassroom().getStudents().size();
        } catch (Exception e) {
            // In case students collection is null or empty
            studentCount = 0;
        }
        
        return new ScheduleDto(
            schedule.getId(),
            schedule.getTeacher().getId(),
            schedule.getTeacher().getFullName(),
            schedule.getClassroom().getId(),
            schedule.getClassroom().getName(),
            schedule.getDayOfWeek(),
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.getRoom(),
            schedule.getSubject(),
            schedule.getMaterialsUrl(),
            schedule.getMeetUrl(),
            studentCount
        );
    }

    // Add sample schedule data for a teacher
    @Transactional
    public List<ScheduleDto> addSampleDataForTeacher(Long teacherId) {
        // Find teacher
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Find a classroom (for demo purposes, we'll use the first one)
        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            throw new RuntimeException("No classrooms available for sample data");
        }
        
        // Create sample schedules
        List<Schedule> sampleSchedules = new ArrayList<>();
        
        // Monday - Java Class
        Schedule monSchedule = new Schedule();
        monSchedule.setTeacher(teacher);
        monSchedule.setClassroom(classrooms.get(0));
        monSchedule.setDayOfWeek(0); // Monday
        monSchedule.setStartTime(LocalTime.of(8, 0));
        monSchedule.setEndTime(LocalTime.of(10, 0));
        monSchedule.setRoom("P.101");
        monSchedule.setSubject("Lập trình Java");
        monSchedule.setMaterialsUrl("#");
        monSchedule.setMeetUrl(null);
        sampleSchedules.add(scheduleRepository.save(monSchedule));
        
        // Tuesday - Database Class
        if (classrooms.size() > 1) {
            Schedule tueSchedule = new Schedule();
            tueSchedule.setTeacher(teacher);
            tueSchedule.setClassroom(classrooms.get(1));
            tueSchedule.setDayOfWeek(1); // Tuesday
            tueSchedule.setStartTime(LocalTime.of(14, 0));
            tueSchedule.setEndTime(LocalTime.of(16, 0));
            tueSchedule.setRoom("P.205");
            tueSchedule.setSubject("Thiết kế CSDL");
            tueSchedule.setMaterialsUrl(null);
            tueSchedule.setMeetUrl("#");
            sampleSchedules.add(scheduleRepository.save(tueSchedule));
        }
        
        // Wednesday - Web Development
        if (classrooms.size() > 2) {
            Schedule wedSchedule = new Schedule();
            wedSchedule.setTeacher(teacher);
            wedSchedule.setClassroom(classrooms.get(2));
            wedSchedule.setDayOfWeek(2); // Wednesday
            wedSchedule.setStartTime(LocalTime.of(10, 0));
            wedSchedule.setEndTime(LocalTime.of(12, 0));
            wedSchedule.setRoom("Lab.A1");
            wedSchedule.setSubject("Phát triển Web");
            wedSchedule.setMaterialsUrl("#");
            wedSchedule.setMeetUrl("#");
            sampleSchedules.add(scheduleRepository.save(wedSchedule));
        }
        
        // Return DTOs of saved schedules
        return sampleSchedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
} 