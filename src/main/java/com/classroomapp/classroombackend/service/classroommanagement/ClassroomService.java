package com.classroomapp.classroombackend.service.classroommanagement;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;

public interface ClassroomService {

    Page<ClassroomDto> getAllClassrooms(Pageable pageable);

    ClassroomDto getClassroomById(Long id);

    ClassroomDto createClassroom(CreateClassroomDto createDto);

    ClassroomDto getClassroomDetails(Long id);

    ClassroomDto updateClassroom(Long id, UpdateClassroomDto updateDto);

    void deleteClassroom(Long id);

    void enrollStudent(Long classroomId, Long studentId);

    void unenrollStudent(Long classroomId, Long studentId);

    Page<ClassroomDto> searchClassrooms(String name, Pageable pageable);

    List<ClassroomDto> getClassroomsByCurrentTeacher();

    List<ClassroomDto> getClassroomsByCurrentStudent();

    List<UserDto> getStudentsInClassroom(Long classroomId);

    List<ClassroomDto> getClassroomsByStudentId(Long studentId);
}
