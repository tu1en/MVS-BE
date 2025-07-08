package com.classroomapp.classroombackend.service;

import java.security.Principal;
import java.util.List;

import com.classroomapp.classroombackend.dto.CreateLectureDto;
import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.dto.LectureDetailsDto;
import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.LectureMaterialDto;
import com.classroomapp.classroombackend.dto.UpdateLectureDto;

public interface LectureService {
    List<LectureDto> getLecturesByClassroomId(Long classroomId);
    LectureDto createLecture(Long classroomId, CreateLectureDto createLectureDto, String userEmail);
    LectureDetailsDto getLectureById(Long lectureId, Principal principal);
    LectureDto updateLecture(Long lectureId, UpdateLectureDto updateLectureDto, String userEmail);
    void deleteLecture(Long lectureId, String userEmail);
    List<LectureMaterialDto> addMaterials(Long lectureId, List<FileUploadResponse> files, String teacherUsername);
    LectureDto addMaterialToLecture(Long lectureId, LectureMaterialDto lectureMaterialDto);
} 