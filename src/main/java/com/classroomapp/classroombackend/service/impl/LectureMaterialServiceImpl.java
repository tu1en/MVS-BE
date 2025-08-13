package com.classroomapp.classroombackend.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.LectureMaterialDto;
import com.classroomapp.classroombackend.exception.FileStorageException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.UnauthorizedException;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LectureMaterial;
import com.classroomapp.classroombackend.repository.LectureMaterialRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.service.LectureMaterialService;

import jakarta.annotation.PostConstruct;

@Service
public class LectureMaterialServiceImpl implements LectureMaterialService {

    private final Path fileStorageLocation;
    private final LectureRepository lectureRepository;
    private final LectureMaterialRepository lectureMaterialRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public LectureMaterialServiceImpl(@Value("${file.upload-dir:uploads/materials}") String uploadDir,
                                      LectureRepository lectureRepository,
                                      LectureMaterialRepository lectureMaterialRepository,
                                      ModelMapper modelMapper) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.lectureRepository = lectureRepository;
        this.lectureMaterialRepository = lectureMaterialRepository;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Không thể tạo thư mục để lưu trữ file tải lên.", ex);
        }
    }
    
    @Override
    public LectureMaterialDto storeFile(MultipartFile file, Long lectureId, String userEmail) {
        Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài giảng với id: " + lectureId));

        if (!lecture.getClassroom().getTeacher().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Bạn không có quyền tải tài liệu lên cho bài giảng này.");
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Xin lỗi! Tên file chứa chuỗi đường dẫn không hợp lệ " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            LectureMaterial lectureMaterial = new LectureMaterial();
            lectureMaterial.setLecture(lecture);
            lectureMaterial.setFileName(fileName);
            lectureMaterial.setContentType(file.getContentType());
            lectureMaterial.setFileSize(file.getSize());
            lectureMaterial.setFilePath(targetLocation.toString());
            
            LectureMaterial savedMaterial = lectureMaterialRepository.save(lectureMaterial);
            return modelMapper.map(savedMaterial, LectureMaterialDto.class);

        } catch (IOException ex) {
            throw new FileStorageException("Không thể lưu file " + fileName + ". Vui lòng thử lại!", ex);
        }
    }

    @Override
    public ResponseEntity<Resource> getFile(Long materialId) {
        LectureMaterial material = lectureMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy file với id " + materialId));

        try {
            Path filePath = Paths.get(material.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                 return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(material.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                throw new ResourceNotFoundException("Không tìm thấy file " + material.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Không tìm thấy file " + material.getFileName(), ex);
        }
    }

    @Override
    public List<LectureMaterialDto> getMaterialsByLectureId(Long lectureId) {
        List<LectureMaterial> materials = lectureMaterialRepository.findByLectureId(lectureId);
        return materials.stream()
                .map(material -> modelMapper.map(material, LectureMaterialDto.class))
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteFile(Long materialId, String userEmail) {
        LectureMaterial material = lectureMaterialRepository.findById(materialId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu với id: " + materialId));

        if (!material.getLecture().getClassroom().getTeacher().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("Bạn không có quyền xóa tài liệu này.");
        }

        try {
            Path filePath = Paths.get(material.getFilePath());
            Files.deleteIfExists(filePath);
            lectureMaterialRepository.delete(material);
        } catch (IOException ex) {
            throw new FileStorageException("Không thể xóa file " + material.getFileName() + ". Vui lòng thử lại!", ex);
        }
    }
} 