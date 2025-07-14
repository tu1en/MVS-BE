package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.CourseMaterialDto;
import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.dto.UploadMaterialDto;
import com.classroomapp.classroombackend.model.CourseMaterial;
import com.classroomapp.classroombackend.repository.CourseMaterialRepository;
import com.classroomapp.classroombackend.service.CourseMaterialService;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final FileStorageService fileStorageService;
    private final HybridMaterialDownloadService hybridDownloadService;

    @Override
    public CourseMaterialDto uploadMaterial(UploadMaterialDto uploadDto, MultipartFile file, Long uploadedBy) {
        try {
            // Store file using FileStorageService
            FileUploadResponse fileUploadResponse = fileStorageService.save(file);

            CourseMaterial material = new CourseMaterial();
            material.setTitle(uploadDto.getTitle());
            material.setDescription(uploadDto.getDescription());
            material.setFilePath(fileUploadResponse.getFileUrl());
            material.setFileName(fileUploadResponse.getFileName());
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
                // We only have the URL, the file name is what the service uses to delete
                fileStorageService.delete(material.getFileName());
            }

            courseMaterialRepository.delete(material);
            log.info("Material deleted successfully: {}", materialId);
        } catch (Exception e) {
            log.error("Error deleting material file: {}", e.getMessage());
            throw new RuntimeException("Failed to delete material file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadMaterial(Long materialId) {
        log.info("🔽 SERVICE: Starting downloadMaterial for ID: {}", materialId);

        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> {
                    log.error("🔽 SERVICE: Material not found in database for ID: {}", materialId);
                    return new RuntimeException("Tài liệu không tồn tại với ID: " + materialId);
                });

        try {
            log.info("🔽 SERVICE: Found material - ID: {}, Title: {}, File path: {}",
                materialId, material.getTitle(), material.getFilePath());
            log.info("🔽 SERVICE: Material details - Size: {} bytes, Type: {}, Classroom: {}",
                material.getFileSize(), material.getFileType(), material.getClassroomId());

            // Increment download count before reading file
            int currentDownloadCount = material.getDownloadCount();
            material.setDownloadCount(currentDownloadCount + 1);
            courseMaterialRepository.save(material);
            log.info("🔽 SERVICE: Updated download count from {} to {}", currentDownloadCount, currentDownloadCount + 1);

            // Get file path and determine storage type
            String filePath = material.getFilePath();
            if (filePath == null || filePath.trim().isEmpty()) {
                log.error("🔽 SERVICE: Invalid file path for material ID: {} - path is null or empty", materialId);
                throw new RuntimeException("Đường dẫn file không hợp lệ cho tài liệu ID: " + materialId);
            }

            log.info("🔽 SERVICE: Original file path: {}", filePath);
            log.info("🔽 SERVICE: Storage type: {}", hybridDownloadService.getStorageType(filePath));

            // Use hybrid download service to handle both local and Firebase files
            byte[] fileContent = hybridDownloadService.downloadMaterialContent(filePath, materialId);

            // Validate file content
            if (fileContent.length == 0) {
                log.warn("🔽 SERVICE: File content is empty for material: {}", material.getFileName());
            }

            return fileContent;

        } catch (Exception e) {
            log.error("🔽 SERVICE: Error downloading material ID {}: {}", materialId, e.getMessage());
            log.error("🔽 SERVICE: Exception type: {}, Stack trace: ", e.getClass().getSimpleName(), e);
            throw new RuntimeException("Lỗi tải xuống tài liệu: " + e.getMessage(), e);
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
