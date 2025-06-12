package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.CourseMaterialDto;
import com.classroomapp.classroombackend.dto.UploadMaterialDto;
import com.classroomapp.classroombackend.model.CourseMaterial;
import com.classroomapp.classroombackend.repository.CourseMaterialRepository;
import com.classroomapp.classroombackend.service.CourseMaterialService;
import com.classroomapp.classroombackend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final FileStorageService fileStorageService;

    @Override
    public CourseMaterialDto uploadMaterial(UploadMaterialDto uploadDto, MultipartFile file, Long uploadedBy) {
        try {
            // Store file using FileStorageService
            String fileName = fileStorageService.storeFile(file);
            String filePath = fileStorageService.getFileStorageLocation() + "/" + fileName;

            CourseMaterial material = new CourseMaterial();
            material.setTitle(uploadDto.getTitle());
            material.setDescription(uploadDto.getDescription());
            material.setFilePath(filePath);
            material.setFileName(fileName);
            material.setFileSize(file.getSize());
            material.setFileType(file.getContentType());
            material.setClassroomId(uploadDto.getClassroomId());
            material.setUploadedBy(uploadedBy);
            material.setIsPublic(uploadDto.getIsPublic() != null ? uploadDto.getIsPublic() : true);
            material.setUploadDate(LocalDateTime.now());

            CourseMaterial savedMaterial = courseMaterialRepository.save(material);
            return convertToDto(savedMaterial);
        } catch (Exception e) {
            log.error("Error uploading material: {}", e.getMessage());
            throw new RuntimeException("Failed to upload material", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMaterialDto> getMaterialsByClassroom(Long classroomId) {
        return courseMaterialRepository.findByClassroomIdOrderByUploadDateDesc(classroomId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMaterialDto> getPublicMaterialsByClassroom(Long classroomId) {
        return courseMaterialRepository.findByClassroomIdAndIsPublicTrueOrderByUploadDateDesc(classroomId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseMaterialDto getMaterialById(Long materialId) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found with id: " + materialId));
        return convertToDto(material);
    }

    @Override
    public CourseMaterialDto updateMaterial(Long materialId, UploadMaterialDto updateDto) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found with id: " + materialId));

        material.setTitle(updateDto.getTitle());
        material.setDescription(updateDto.getDescription());
        if (updateDto.getIsPublic() != null) {
            material.setIsPublic(updateDto.getIsPublic());
        }

        CourseMaterial updatedMaterial = courseMaterialRepository.save(material);
        return convertToDto(updatedMaterial);
    }

    @Override
    public void deleteMaterial(Long materialId) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found with id: " + materialId));

        try {
            // Delete physical file
            if (material.getFilePath() != null) {
                Path filePath = Paths.get(material.getFilePath());
                Files.deleteIfExists(filePath);
            }

            courseMaterialRepository.delete(material);
            log.info("Material deleted successfully: {}", materialId);
        } catch (IOException e) {
            log.error("Error deleting material file: {}", e.getMessage());
            throw new RuntimeException("Failed to delete material file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadMaterial(Long materialId) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found with id: " + materialId));

        try {
            // Increment download count
            material.setDownloadCount(material.getDownloadCount() + 1);
            courseMaterialRepository.save(material);

            // Read and return file content
            Path filePath = Paths.get(material.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error downloading material: {}", e.getMessage());
            throw new RuntimeException("Failed to download material", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMaterialDto> searchMaterials(Long classroomId, String searchTerm) {
        return courseMaterialRepository.searchMaterials(classroomId, searchTerm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMaterialDto> getMaterialsByFileType(Long classroomId, String fileType) {
        return courseMaterialRepository.findByClassroomIdAndFileTypeContainingIgnoreCaseOrderByUploadDateDesc(classroomId, fileType)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseMaterialDto> getMaterialsByUploader(Long uploadedBy) {
        return courseMaterialRepository.findByUploadedByOrderByUploadDateDesc(uploadedBy)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalDownloadsByClassroom(Long classroomId) {
        Long totalDownloads = courseMaterialRepository.getTotalDownloadsByClassroom(classroomId);
        return totalDownloads != null ? totalDownloads : 0L;
    }

    private CourseMaterialDto convertToDto(CourseMaterial material) {
        CourseMaterialDto dto = new CourseMaterialDto();
        BeanUtils.copyProperties(material, dto);
        return dto;
    }
}
