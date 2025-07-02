package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CreateEventDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.service.TimetableService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimetableServiceImpl implements TimetableService {

    private final TimetableEventRepository timetableEventRepository;

    @Override
    @Transactional
    public TimetableEventDto createEvent(CreateEventDto createDto, Long createdBy) {
        TimetableEvent event = new TimetableEvent();
        event.setTitle(createDto.getTitle());
        event.setDescription(createDto.getDescription());
        event.setStartDatetime(createDto.getStartDatetime());
        event.setEndDatetime(createDto.getEndDatetime());
        event.setEventType(TimetableEvent.EventType.valueOf(createDto.getEventType()));
        event.setClassroomId(createDto.getClassroomId());
        event.setLocation(createDto.getLocation());
        event.setIsAllDay(createDto.getIsAllDay());
        event.setReminderMinutes(createDto.getReminderMinutes());
        event.setColor(createDto.getColor());
        event.setRecurringRule(createDto.getRecurringRule());
        event.setParentEventId(createDto.getParentEventId());
        event.setCreatedBy(createdBy);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        event.setIsCancelled(false);
        
        TimetableEvent savedEvent = timetableEventRepository.save(event);
        return convertToDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableEventDto getEventById(Long eventId) {
        TimetableEvent event = timetableEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return convertToDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getEventsByClassroomAndDateRange(Long classroomId, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimetableEvent> events = timetableEventRepository.findEventsByClassroomAndDateRange(
                classroomId, startDate, endDate);
        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<TimetableEvent> events = timetableEventRepository.findEventsByDateRange(startDate, endDate);
        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getUpcomingEvents(Long classroomId) {
        // Get upcoming events for the next 30 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);
        
        List<TimetableEvent> events;
        if (classroomId != null) {
            events = timetableEventRepository.findEventsByClassroomAndDateRange(
                    classroomId, now, thirtyDaysLater);
        } else {
            events = timetableEventRepository.findUpcomingEvents(now);
        }
        
        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getEventsByType(Long classroomId, String eventType) {
        TimetableEvent.EventType type = TimetableEvent.EventType.valueOf(eventType);
        List<TimetableEvent> events = timetableEventRepository.findByEventType(type);
        
        // Filter by classroom if provided
        List<TimetableEvent> filteredEvents;
        if (classroomId != null) {
            filteredEvents = events.stream()
                    .filter(event -> event.getClassroomId() != null && event.getClassroomId().equals(classroomId))
                    .collect(Collectors.toList());
        } else {
            filteredEvents = events;
        }
        
        return filteredEvents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getAllDayEvents(Long classroomId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthLater = now.plusMonths(1);
        
        List<TimetableEvent> events = timetableEventRepository.findAllDayEventsByDateRange(now, oneMonthLater);
        
        // Filter by classroom if provided
        List<TimetableEvent> filteredEvents;
        if (classroomId != null) {
            filteredEvents = events.stream()
                    .filter(event -> event.getClassroomId() != null && event.getClassroomId().equals(classroomId))
                    .collect(Collectors.toList());
        } else {
            filteredEvents = events;
        }
        
        return filteredEvents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getRecurringEvents(Long classroomId) {
        // Get all events with a recurring rule
        List<TimetableEvent> allEvents = timetableEventRepository.findAll();
        List<TimetableEvent> recurringEvents = allEvents.stream()
                .filter(event -> event.getRecurringRule() != null && !event.getRecurringRule().isEmpty())
                .collect(Collectors.toList());
        
        // Filter by classroom if provided
        List<TimetableEvent> filteredEvents;
        if (classroomId != null) {
            filteredEvents = recurringEvents.stream()
                    .filter(event -> event.getClassroomId() != null && event.getClassroomId().equals(classroomId))
                    .collect(Collectors.toList());
        } else {
            filteredEvents = recurringEvents;
        }
        
        return filteredEvents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TimetableEventDto updateEvent(Long eventId, CreateEventDto updateDto) {
        TimetableEvent event = timetableEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        // Update fields
        if (updateDto.getTitle() != null) {
            event.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            event.setDescription(updateDto.getDescription());
        }
        if (updateDto.getStartDatetime() != null) {
            event.setStartDatetime(updateDto.getStartDatetime());
        }
        if (updateDto.getEndDatetime() != null) {
            event.setEndDatetime(updateDto.getEndDatetime());
        }
        if (updateDto.getEventType() != null) {
            event.setEventType(TimetableEvent.EventType.valueOf(updateDto.getEventType()));
        }
        if (updateDto.getClassroomId() != null) {
            event.setClassroomId(updateDto.getClassroomId());
        }
        if (updateDto.getLocation() != null) {
            event.setLocation(updateDto.getLocation());
        }
        if (updateDto.getIsAllDay() != null) {
            event.setIsAllDay(updateDto.getIsAllDay());
        }
        if (updateDto.getReminderMinutes() != null) {
            event.setReminderMinutes(updateDto.getReminderMinutes());
        }
        if (updateDto.getColor() != null) {
            event.setColor(updateDto.getColor());
        }
        if (updateDto.getRecurringRule() != null) {
            event.setRecurringRule(updateDto.getRecurringRule());
        }
        
        event.setUpdatedAt(LocalDateTime.now());
        
        TimetableEvent updatedEvent = timetableEventRepository.save(event);
        return convertToDto(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        timetableEventRepository.deleteById(eventId);
    }

    @Override
    @Transactional
    public TimetableEventDto cancelEvent(Long eventId) {
        TimetableEvent event = timetableEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        event.setIsCancelled(true);
        event.setUpdatedAt(LocalDateTime.now());
        
        TimetableEvent updatedEvent = timetableEventRepository.save(event);
        return convertToDto(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> checkConflicts(Long classroomId, LocalDateTime startTime, LocalDateTime endTime, Long excludeEventId) {
        List<TimetableEvent> events = timetableEventRepository.findEventsByClassroomAndDateRange(
                classroomId, startTime, endTime);
        
        // Filter out the excluded event if provided
        List<TimetableEvent> filteredEvents;
        if (excludeEventId != null) {
            filteredEvents = events.stream()
                    .filter(event -> !event.getId().equals(excludeEventId))
                    .collect(Collectors.toList());
        } else {
            filteredEvents = events;
        }
        
        return filteredEvents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getEventsByClassrooms(List<Long> classroomIds, LocalDateTime startDate, LocalDateTime endDate) {
        // Get events for each classroom and combine
        List<TimetableEvent> allEvents = new ArrayList<>();
        for (Long classroomId : classroomIds) {
            List<TimetableEvent> classroomEvents = timetableEventRepository.findEventsByClassroomAndDateRange(
                    classroomId, startDate, endDate);
            allEvents.addAll(classroomEvents);
        }
        
        return allEvents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addAttendee(Long eventId, Long userId) {
        // Implementation for adding attendees would be here
        // This would require additional repository methods and models
    }

    @Override
    @Transactional
    public void removeAttendee(Long eventId, Long userId) {
        // Implementation for removing attendees would be here
    }

    @Override
    @Transactional
    public void updateAttendanceStatus(Long eventId, Long userId, String status) {
        // Implementation for updating attendance status would be here
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getEventAttendees(Long eventId) {
        // Implementation for getting event attendees would be here
        return List.of();
    }

    @Override
    @Transactional
    public List<TimetableEventDto> createRecurringInstances(Long parentEventId, LocalDateTime endDate) {
        // Implementation for creating recurring instances would be here
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEventDto> getEventsByCreator(Long createdBy) {
        List<TimetableEvent> events = timetableEventRepository.findByCreatedBy(createdBy);
        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Helper method to convert entity to DTO
    private TimetableEventDto convertToDto(TimetableEvent event) {
        TimetableEventDto dto = new TimetableEventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartDatetime(event.getStartDatetime());
        dto.setEndDatetime(event.getEndDatetime());
        dto.setEventType(event.getEventType().name());
        dto.setClassroomId(event.getClassroomId());
        dto.setCreatedBy(event.getCreatedBy());
        dto.setLocation(event.getLocation());
        dto.setIsAllDay(event.getIsAllDay());
        dto.setReminderMinutes(event.getReminderMinutes());
        dto.setColor(event.getColor());
        dto.setRecurringRule(event.getRecurringRule());
        dto.setParentEventId(event.getParentEventId());
        dto.setIsCancelled(event.getIsCancelled());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        
        // If the classroom is loaded, include its name
        if (event.getClassroom() != null) {
            dto.setClassroomName(event.getClassroom().getName());
        }
        
        return dto;
    }
} 