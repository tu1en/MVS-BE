package com.classroomapp.classroombackend.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ScheduleService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;

    @Override
    public List<ScheduleDto> getSchedulesByClassroomId(Long classroomId) {
        log.info("Fetching schedules for classroom ID: {}", classroomId);
        
        List<Schedule> schedules = scheduleRepository.findByClassroomId(classroomId);
        log.info("Found {} schedules for classroom ID: {}", schedules.size(), classroomId);
        
        // Thêm logging chi tiết
        for (Schedule schedule : schedules) {
            log.debug("Schedule: day={}, start={}, end={}, room={}, subject={}", 
                getDayOfWeekName(schedule.getDayOfWeek()),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoom(),
                schedule.getSubject());
        }
        
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherId(Long teacherId) {
        log.info("Fetching schedules for teacher ID: {}", teacherId);
        
        List<Schedule> schedules = scheduleRepository.findByTeacherId(teacherId);
        log.info("Found {} schedules for teacher ID: {}", schedules.size(), teacherId);
        
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getSchedulesByStudentId(Long studentId) {
        log.info("Fetching schedules for student ID: {}", studentId);
        
        // Get the classrooms the student is enrolled in
        List<Long> enrolledClassroomIds = classroomRepository.findClassroomsIdsByStudentId(studentId);
        log.info("Student ID {} is enrolled in {} classrooms", studentId, enrolledClassroomIds.size());
        
        if (enrolledClassroomIds.isEmpty()) {
            log.warn("Student ID {} is not enrolled in any classrooms", studentId);
            return new ArrayList<>();
        }
        
        List<Schedule> schedules = scheduleRepository.findByClassroomIdIn(enrolledClassroomIds);
        log.info("Found {} schedules for student ID: {}", schedules.size(), studentId);
        
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        log.info("Creating new schedule: {}", scheduleDto);
        
        Schedule schedule = convertToEntity(scheduleDto);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        
        log.info("Created schedule with ID: {}", savedSchedule.getId());
        return convertToDto(savedSchedule);
    }

    @Override
    public ScheduleDto updateSchedule(Long id, ScheduleDto scheduleDto) {
        log.info("Updating schedule ID: {}", id);
        
        if (!scheduleRepository.existsById(id)) {
            log.error("Schedule not found with ID: {}", id);
            throw new EntityNotFoundException("Schedule not found with ID: " + id);
        }
        
        Schedule schedule = convertToEntity(scheduleDto);
        schedule.setId(id);
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        
        log.info("Updated schedule with ID: {}", updatedSchedule.getId());
        return convertToDto(updatedSchedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule ID: {}", id);
        
        if (!scheduleRepository.existsById(id)) {
            log.error("Schedule not found with ID: {}", id);
            throw new EntityNotFoundException("Schedule not found with ID: " + id);
        }
        
        scheduleRepository.deleteById(id);
        log.info("Deleted schedule with ID: {}", id);
    }

    @Override
    public List<ScheduleDto> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleDto getScheduleById(Long id) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        return schedule.map(this::convertToDto).orElse(null);
    }

    @Override
    public List<ScheduleDto> getSchedulesByTeacherAndDay(Long teacherId, Integer dayOfWeek) {
        List<Schedule> schedules = scheduleRepository.findByTeacherIdAndDayOfWeek(teacherId, dayOfWeek);
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleDto createScheduleEntry(ScheduleDto scheduleDto) {
        Schedule schedule = convertFromDto(scheduleDto);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToDto(savedSchedule);
    }

    @Override
    public List<TimetableEventDto> getTimetableForUser(Long userId, LocalDate startDate, LocalDate endDate) {
        // This method converts Schedule entities to TimetableEventDto for calendar display
        // For now, return empty list - this would need proper implementation based on requirements
        log.info("Getting timetable for user {} from {} to {}", userId, startDate, endDate);
        return new ArrayList<>();
    }

    private ScheduleDto convertToDto(Schedule schedule) {
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setClassroomId(schedule.getClassroom() != null ? schedule.getClassroom().getId() : null);
        dto.setTeacherId(schedule.getTeacher() != null ? schedule.getTeacher().getId() : null);

        // Convert Integer dayOfWeek to DayOfWeek enum for consolidated DTO
        Integer dayOfWeekInt = schedule.getDayOfWeek();
        if (dayOfWeekInt != null) {
            dto.setDay(dayOfWeekInt); // Set legacy integer format
            dto.setDayOfWeek(convertIntegerToDayOfWeek(dayOfWeekInt)); // Set modern enum format
        }

        dto.setDayName(getDayOfWeekName(schedule.getDayOfWeek()));
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());

        // Set both legacy string format and modern LocalTime format
        if (schedule.getStartTime() != null) {
            dto.setStart(schedule.getStartTime().toString().substring(0, 5)); // "HH:MM"
        }
        if (schedule.getEndTime() != null) {
            dto.setEnd(schedule.getEndTime().toString().substring(0, 5)); // "HH:MM"
        }

        dto.setRoom(schedule.getRoom());
        dto.setLocation(schedule.getRoom()); // Map room to location for compatibility
        dto.setSubject(schedule.getSubject());
        dto.setMaterialsUrl(schedule.getMaterialsUrl());
        dto.setMeetUrl(schedule.getMeetUrl());
        
        // Add classroom and teacher details
        if (schedule.getClassroom() != null) {
            dto.setClassroomName(schedule.getClassroom().getName());
        }
        
        if (schedule.getTeacher() != null) {
            dto.setTeacherName(schedule.getTeacher().getFullName());
        }
        
        return dto;
    }

    private Schedule convertToEntity(ScheduleDto dto) {
        Schedule schedule = new Schedule();
        schedule.setId(dto.getId());
        
        // Set classroom if classroomId is provided
        if (dto.getClassroomId() != null) {
            Optional<Classroom> classroom = classroomRepository.findById(dto.getClassroomId());
            classroom.ifPresent(schedule::setClassroom);
        }
        
        // Set teacher if teacherId is provided
        if (dto.getTeacherId() != null) {
            Optional<User> teacher = userRepository.findById(dto.getTeacherId());
            teacher.ifPresent(schedule::setTeacher);
        }
        
        // Convert DayOfWeek enum to Integer for entity
        if (dto.getDayOfWeek() != null) {
            schedule.setDayOfWeek(convertDayOfWeekToInteger(dto.getDayOfWeek()));
        } else if (dto.getDay() != null) {
            schedule.setDayOfWeek(dto.getDay()); // Use legacy integer format
        }

        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setRoom(dto.getRoom());
        schedule.setSubject(dto.getSubject());
        schedule.setMaterialsUrl(dto.getMaterialsUrl());
        schedule.setMeetUrl(dto.getMeetUrl());
        
        return schedule;
    }

    private Schedule convertFromDto(ScheduleDto dto) {
        Schedule schedule = new Schedule();

        if (dto.getId() != null) {
            // For updates, load existing schedule
            Optional<Schedule> existingSchedule = scheduleRepository.findById(dto.getId());
            if (existingSchedule.isPresent()) {
                schedule = existingSchedule.get();
            }
        }

        // Set classroom
        if (dto.getClassroomId() != null) {
            Optional<Classroom> classroom = classroomRepository.findById(dto.getClassroomId());
            classroom.ifPresent(schedule::setClassroom);
        }

        // Set teacher
        if (dto.getTeacherId() != null) {
            Optional<User> teacher = userRepository.findById(dto.getTeacherId());
            teacher.ifPresent(schedule::setTeacher);
        }

        // Convert DayOfWeek enum to Integer for entity
        if (dto.getDayOfWeek() != null) {
            schedule.setDayOfWeek(convertDayOfWeekToInteger(dto.getDayOfWeek()));
        } else if (dto.getDay() != null) {
            schedule.setDayOfWeek(dto.getDay()); // Use legacy integer format
        }

        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setRoom(dto.getRoom() != null ? dto.getRoom() : dto.getLocation());
        schedule.setSubject(dto.getSubject());
        schedule.setMaterialsUrl(dto.getMaterialsUrl());
        schedule.setMeetUrl(dto.getMeetUrl());

        return schedule;
    }

    private String getDayOfWeekName(Integer dayOfWeek) {
        if (dayOfWeek == null) {
            return "Unknown";
        }
        
        // Dayofweek is 0-based (0 = Monday, 6 = Sunday)
        DayOfWeek day;
        if (dayOfWeek >= 0 && dayOfWeek <= 6) {
            day = DayOfWeek.of(dayOfWeek == 0 ? 1 : (dayOfWeek == 6 ? 7 : dayOfWeek + 1));
        } else {
            return "Invalid day (" + dayOfWeek + ")";
        }
        
        return day.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("vi-VN"));
    }

    @Override
    public void createSampleSchedules(Long classroomId) {
        log.info("Creating sample schedules for classroom ID: {}", classroomId);
        
        Optional<Classroom> classroomOpt = classroomRepository.findById(classroomId);
        if (!classroomOpt.isPresent()) {
            log.error("Classroom not found with ID: {}", classroomId);
            throw new EntityNotFoundException("Classroom not found with ID: " + classroomId);
        }
        
        Classroom classroom = classroomOpt.get();
        User teacher = classroom.getTeacher();
        
        if (teacher == null) {
            log.error("Classroom has no assigned teacher. ID: {}", classroomId);
            throw new EntityNotFoundException("Classroom has no assigned teacher");
        }
        
        // Check if schedules already exist for this classroom
        List<Schedule> existingSchedules = scheduleRepository.findByClassroomId(classroomId);
        if (!existingSchedules.isEmpty()) {
            log.info("Schedules already exist for classroom ID: {}. Count: {}", classroomId, existingSchedules.size());
            return;
        }
        
        // Create sample schedules for Monday, Wednesday, and Friday
        createAndSaveSchedule(classroom, teacher, 0, LocalTime.of(8, 0), LocalTime.of(9, 30), 
                "Phòng 101", "Bài giảng lý thuyết");
        
        createAndSaveSchedule(classroom, teacher, 2, LocalTime.of(13, 30), LocalTime.of(15, 0), 
                "Phòng Lab 2", "Bài tập thực hành");
        
        createAndSaveSchedule(classroom, teacher, 4, LocalTime.of(10, 0), LocalTime.of(11, 30), 
                "Phòng 203", "Ôn tập + Kiểm tra");
        
        log.info("Created 3 sample schedules for classroom ID: {}", classroomId);
    }
    
    private void createAndSaveSchedule(Classroom classroom, User teacher, int dayOfWeek, 
            LocalTime startTime, LocalTime endTime, String room, String subject) {
        
        Schedule schedule = new Schedule();
        schedule.setClassroom(classroom);
        schedule.setTeacher(teacher);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setRoom(room);
        schedule.setSubject(subject);
        schedule.setMaterialsUrl("https://drive.google.com/materials");
        schedule.setMeetUrl("https://meet.google.com/class-" + classroom.getId());
        
        scheduleRepository.save(schedule);
        log.info("Created schedule: {}-{} on day {} in room {}",
                startTime, endTime, getDayOfWeekName(dayOfWeek), room);
    }

    // Helper methods for DayOfWeek conversion (for consolidated ScheduleDto compatibility)
    private DayOfWeek convertIntegerToDayOfWeek(Integer day) {
        if (day == null) return null;
        switch (day) {
            case 0: return DayOfWeek.MONDAY;
            case 1: return DayOfWeek.TUESDAY;
            case 2: return DayOfWeek.WEDNESDAY;
            case 3: return DayOfWeek.THURSDAY;
            case 4: return DayOfWeek.FRIDAY;
            case 5: return DayOfWeek.SATURDAY;
            case 6: return DayOfWeek.SUNDAY;
            default: return DayOfWeek.MONDAY;
        }
    }

    private Integer convertDayOfWeekToInteger(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) return null;
        switch (dayOfWeek) {
            case MONDAY: return 0;
            case TUESDAY: return 1;
            case WEDNESDAY: return 2;
            case THURSDAY: return 3;
            case FRIDAY: return 4;
            case SATURDAY: return 5;
            case SUNDAY: return 6;
            default: return 0;
        }
    }

    @Override
    public List<LectureDto> getLecturesByScheduleId(Long scheduleId) {
        log.info("Fetching lectures for schedule ID: {}", scheduleId);

        // Find lectures that are associated with this schedule
        List<Lecture> lectures = lectureRepository.findAll().stream()
                .filter(lecture -> lecture.getSchedule() != null && lecture.getSchedule().getId().equals(scheduleId))
                .collect(Collectors.toList());

        log.info("Found {} lectures for schedule ID: {}", lectures.size(), scheduleId);

        return lectures.stream()
                .map(this::convertLectureToDto)
                .collect(Collectors.toList());
    }

    private LectureDto convertLectureToDto(Lecture lecture) {
        LectureDto dto = new LectureDto();
        dto.setId(lecture.getId());
        dto.setTitle(lecture.getTitle());
        dto.setContent(lecture.getContent());
        dto.setLectureDate(lecture.getLectureDate());
        dto.setClassroomId(lecture.getClassroom() != null ? lecture.getClassroom().getId() : null);
        return dto;
    }
}