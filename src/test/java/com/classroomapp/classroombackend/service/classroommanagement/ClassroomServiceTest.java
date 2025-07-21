package com.classroomapp.classroombackend.service.classroommanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.service.classroommanagement.impl.ClassroomServiceImpl;
import com.classroomapp.classroombackend.service.firebase.FirebaseClassroomService;

/**
 * Unit tests cho ClassroomService
 */
@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private FirebaseClassroomService firebaseClassroomService;

    @InjectMocks
    private ClassroomServiceImpl classroomService;

    private Classroom testClassroom;
    private CreateClassroomDto createDto;
    private UpdateClassroomDto updateDto;

    @BeforeEach
    void setUp() {
        testClassroom = new Classroom();
        testClassroom.setId(1L);
        testClassroom.setClassroomName("Test Classroom");
        testClassroom.setDescription("Test Description");
        testClassroom.setTeacher(null); // Mock teacher if needed
        testClassroom.setCreatedAt(LocalDateTime.now());
        testClassroom.setUpdatedAt(LocalDateTime.now());

        createDto = new CreateClassroomDto();
        createDto.setClassroomName("New Classroom");
        createDto.setDescription("New Description");
        createDto.setTeacherId(1L);

        updateDto = new UpdateClassroomDto();
        updateDto.setName("Updated Classroom");
        updateDto.setDescription("Updated Description");
    }

    @Test
    void getAllClassrooms_ShouldReturnPageOfClassrooms() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Classroom> classrooms = Arrays.asList(testClassroom);
        Page<Classroom> classroomPage = new PageImpl<>(classrooms, pageable, 1);

        when(classroomRepository.findAll(pageable)).thenReturn(classroomPage);

        // When
        Page<ClassroomDto> result = classroomService.getAllClassrooms(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Classroom", result.getContent().get(0).getClassroomName());
        verify(classroomRepository).findAll(pageable);
    }

    @Test
    void getClassroomById_WhenExists_ShouldReturnClassroom() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));

        // When
        ClassroomDto result = classroomService.getClassroomById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Classroom", result.getClassroomName());
        verify(classroomRepository).findById(1L);
    }

    @Test
    void getClassroomById_WhenNotExists_ShouldThrowException() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            classroomService.getClassroomById(1L);
        });
        verify(classroomRepository).findById(1L);
    }

    @Test
    void getClassroomById_WhenIdIsNull_ShouldThrowValidationException() {
        // When & Then
        assertThrows(ValidationException.class, () -> {
            classroomService.getClassroomById(null);
        });
        verify(classroomRepository, never()).findById(any());
    }

    @Test
    void createClassroom_WhenValid_ShouldReturnCreatedClassroom() {
        // Given
        when(classroomRepository.existsByClassroomNameAndTeacherId(anyString(), anyLong())).thenReturn(false);
        when(classroomRepository.save(any(Classroom.class))).thenReturn(testClassroom);

        // When
        ClassroomDto result = classroomService.createClassroom(createDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Classroom", result.getClassroomName());
        verify(classroomRepository).existsByClassroomNameAndTeacherId(createDto.getClassroomName(), createDto.getTeacherId());
        verify(classroomRepository).save(any(Classroom.class));
    }

    @Test
    void createClassroom_WhenDuplicateName_ShouldThrowValidationException() {
        // Given
        when(classroomRepository.existsByClassroomNameAndTeacherId(anyString(), anyLong())).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            classroomService.createClassroom(createDto);
        });
        verify(classroomRepository).existsByClassroomNameAndTeacherId(createDto.getClassroomName(), createDto.getTeacherId());
        verify(classroomRepository, never()).save(any());
    }

    @Test
    void createClassroom_WhenNullDto_ShouldThrowValidationException() {
        // When & Then
        assertThrows(ValidationException.class, () -> {
            classroomService.createClassroom(null);
        });
        verify(classroomRepository, never()).save(any());
    }

    @Test
    void updateClassroom_WhenValid_ShouldReturnUpdatedClassroom() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));
        when(classroomRepository.save(any(Classroom.class))).thenReturn(testClassroom);

        // When
        ClassroomDto result = classroomService.updateClassroom(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(classroomRepository).findById(1L);
        verify(classroomRepository).save(any(Classroom.class));
    }

    @Test
    void updateClassroom_WhenNotExists_ShouldThrowException() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            classroomService.updateClassroom(1L, updateDto);
        });
        verify(classroomRepository).findById(1L);
        verify(classroomRepository, never()).save(any());
    }

    @Test
    void deleteClassroom_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));
        doNothing().when(classroomRepository).delete(testClassroom);

        // When
        assertDoesNotThrow(() -> {
            classroomService.deleteClassroom(1L);
        });

        // Then
        verify(classroomRepository).findById(1L);
        verify(classroomRepository).delete(testClassroom);
    }

    @Test
    void deleteClassroom_WhenNotExists_ShouldThrowException() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            classroomService.deleteClassroom(1L);
        });
        verify(classroomRepository).findById(1L);
        verify(classroomRepository, never()).delete(any());
    }

    @Test
    void searchClassrooms_ShouldReturnMatchingClassrooms() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Classroom> classrooms = Arrays.asList(testClassroom);
        Page<Classroom> classroomPage = new PageImpl<>(classrooms, pageable, 1);

        when(classroomRepository.findByClassroomNameContainingIgnoreCase("test", pageable)).thenReturn(classroomPage);

        // When
        Page<ClassroomDto> result = classroomService.searchClassrooms("test", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(classroomRepository).findByClassroomNameContainingIgnoreCase("test", pageable);
    }

    @Test
    void getClassroomsByTeacher_ShouldReturnTeacherClassrooms() {
        // Given
        List<Classroom> classrooms = Arrays.asList(testClassroom);
        when(classroomRepository.findByTeacherId(1L)).thenReturn(classrooms);

        // When
        List<ClassroomDto> result = classroomService.getClassroomsByTeacher(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Classroom", result.get(0).getClassroomName());
        verify(classroomRepository).findByTeacherId(1L);
    }
}
