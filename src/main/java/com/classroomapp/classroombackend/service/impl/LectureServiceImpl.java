package com.classroomapp.classroombackend.service.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CreateLectureDto;
import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.dto.LectureDetailsDto;
import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.LectureMaterialDto;
import com.classroomapp.classroombackend.dto.UpdateLectureDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.UnauthorizedException;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LectureMaterial;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureMaterialRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;
import com.classroomapp.classroombackend.service.LectureService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final ClassroomRepository classroomRepository;
    private final ModelMapper modelMapper;
    private final ClassroomSecurityService classroomSecurityService;
    private final LectureMaterialRepository lectureMaterialRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LectureDto createLecture(Long classroomId, CreateLectureDto createLectureDto, String userEmail) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + classroomId));

        // Authorization Check: Ensure the user is the teacher of the classroom
        if (!classroom.getTeacher().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User is not authorized to add lectures to this classroom.");
        }

        Lecture lecture = new Lecture();
        lecture.setTitle(createLectureDto.getTitle());
        lecture.setContent(createLectureDto.getContent());
        lecture.setClassroom(classroom);

        Lecture savedLecture = lectureRepository.save(lecture);

        // Handle materials if provided
        if (createLectureDto.getMaterials() != null && !createLectureDto.getMaterials().isEmpty()) {
            for (var materialDto : createLectureDto.getMaterials()) {
                LectureMaterial material = new LectureMaterial();
                material.setFileName(materialDto.getFileName());
                material.setContentType(materialDto.getFileType());
                material.setDownloadUrl(materialDto.getFileUrl());
                material.setFileSize(materialDto.getFileSize());
                material.setLecture(savedLecture);
                
                // Set file path for local files
                if (materialDto.isLocalFile()) {
                    material.setFilePath(materialDto.getFileUrl()); // For local files, URL is the file path
                }
                
                lectureMaterialRepository.save(material);
            }
        }

        return modelMapper.map(savedLecture, LectureDto.class);
    }

    @Override
    public List<LectureDto> getLecturesByClassroomId(Long classroomId) {
        System.out.println("📚 LectureService: Getting lectures for classroomId: " + classroomId);

        if (!classroomRepository.existsById(classroomId)) {
            System.out.println("❌ LectureService: Classroom not found with id: " + classroomId);
            throw new ResourceNotFoundException("Classroom not found with id: " + classroomId);
        }

        System.out.println("✅ LectureService: Classroom exists, fetching lectures...");
        List<Lecture> lectures = lectureRepository.findByClassroomId(classroomId);
        System.out.println("📊 LectureService: Found " + lectures.size() + " lectures");

        List<LectureDto> lectureDtos = lectures.stream()
                .map(lecture -> {
                    LectureDto dto = modelMapper.map(lecture, LectureDto.class);
                    System.out.println("🔄 LectureService: Mapped lecture: " + dto.getTitle());
                    return dto;
                })
                .collect(Collectors.toList());

        System.out.println("✅ LectureService: Returning " + lectureDtos.size() + " lecture DTOs");
        return lectureDtos;
    }

    @Override
    public LectureDetailsDto getLectureById(Long lectureId, Principal principal) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));

        if (!classroomSecurityService.isMember(lecture.getClassroom().getId(), principal)) {
            throw new AccessDeniedException("User is not a member of the classroom for this lecture.");
        }

        return modelMapper.map(lecture, LectureDetailsDto.class);
    }

    @Override
    public LectureDto updateLecture(Long lectureId, UpdateLectureDto updateLectureDto, String userEmail) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));

        if (!lecture.getClassroom().getTeacher().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User is not authorized to update this lecture.");
        }

        lecture.setTitle(updateLectureDto.getTitle());
        lecture.setContent(updateLectureDto.getDescription());

        Lecture updatedLecture = lectureRepository.save(lecture);
        return modelMapper.map(updatedLecture, LectureDto.class);
    }

    @Override
    public void deleteLecture(Long lectureId, String userEmail) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));
        
        if (!lecture.getClassroom().getTeacher().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("User is not authorized to delete this lecture.");
        }

        lectureRepository.delete(lecture);
    }

    @Override
    @Transactional
    public List<LectureMaterialDto> addMaterials(Long lectureId, List<FileUploadResponse> files, String teacherUsername) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));

        User teacher = userRepository.findByEmail(teacherUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", teacherUsername));

        if (!classroomSecurityService.isTeacherOfClassroom(teacher, lecture.getClassroom().getId())) {
            throw new AccessDeniedException("User is not the teacher of the class for this lecture.");
        }

        List<LectureMaterial> newMaterials = new ArrayList<>();
        for (FileUploadResponse fileInfo : files) {
            LectureMaterial material = new LectureMaterial();
            material.setFileName(fileInfo.getFileName());
            material.setDownloadUrl(fileInfo.getFileUrl());
            material.setContentType(fileInfo.getFileType());
            material.setFileSize(fileInfo.getSize());
            material.setLecture(lecture);
            newMaterials.add(lectureMaterialRepository.save(material));
        }

        return newMaterials.stream()
                .map(material -> modelMapper.map(material, LectureMaterialDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public LectureDto addMaterialToLecture(Long lectureId, LectureMaterialDto materialDto) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));

        LectureMaterial material = modelMapper.map(materialDto, LectureMaterial.class);
        lecture.getLectureMaterials().add(material);
        material.setLecture(lecture);

        Lecture updatedLecture = lectureRepository.save(lecture);
        return modelMapper.map(updatedLecture, LectureDto.class);
    }
} 