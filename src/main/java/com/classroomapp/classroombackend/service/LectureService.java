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

    /**
     * Creates a new lecture for a given classroom.
     *
     * @param classroomId The ID of the classroom where the lecture will be created.
     * @param createLectureDto DTO containing lecture information.
     * @param userEmail The email of the user attempting to create the lecture (for authorization).
     * @return The created lecture as a DTO.
     */
    LectureDto createLecture(Long classroomId, CreateLectureDto createLectureDto, String userEmail);

    /**
     * Retrieves all lectures for a given classroom.
     *
     * @param classroomId The ID of the classroom.
     * @return A list of lectures for the classroom.
     */
    List<LectureDto> getLecturesByClassroomId(Long classroomId);

    LectureDetailsDto getLectureById(Long lectureId, Principal principal);

    LectureDto updateLecture(Long lectureId, UpdateLectureDto updateLectureDto, String userEmail);

    void deleteLecture(Long lectureId, String userEmail);

    List<LectureMaterialDto> addMaterials(Long lectureId, List<FileUploadResponse> files, String teacherUsername);
} 