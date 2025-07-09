package com.classroomapp.classroombackend.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.LectureMaterialDto;


public interface LectureMaterialService {
    List<LectureMaterialDto> getMaterialsByLectureId(Long lectureId);
    LectureMaterialDto storeFile(MultipartFile file, Long lectureId, String userEmail);
    void deleteFile(Long materialId, String userEmail);
    ResponseEntity<Resource> getFile(Long materialId);
} 