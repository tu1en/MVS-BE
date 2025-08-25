package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.CourseMaterialDto;
import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
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

    @Override
    public CourseMaterialDto uploadMaterial(UploadMaterialDto uploadDto, MultipartFile file, Long uploadedBy) {
        try {
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
            log.error("Lỗi khi tải lên tài liệu: {}", e.getMessage());
            throw new RuntimeException("Tải lên tài liệu thất bại", e);
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với id: " + materialId));
        return convertToDto(material);
    }

    @Override
    public CourseMaterialDto updateMaterial(Long materialId, UploadMaterialDto updateDto) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với id: " + materialId));

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với id: " + materialId));

        try {
            if (material.getFilePath() != null) {
                fileStorageService.delete(material.getFileName());
            }
            courseMaterialRepository.delete(material);
            log.info("Xóa tài liệu thành công: {}", materialId);
        } catch (Exception e) {
            log.error("Lỗi khi xóa file tài liệu: {}", e.getMessage());
            throw new RuntimeException("Xóa file tài liệu thất bại", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadMaterial(Long materialId) {
        CourseMaterial material = courseMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với id: " + materialId));

        try {
            // TODO: Implement actual file download logic
            // For now, increment download count and return empty byte array
            log.warn("Tải xuống từ URL chưa được triển khai. Trả về mảng byte rỗng cho materialId: {}", materialId);
            material.setDownloadCount(material.getDownloadCount() + 1);
            courseMaterialRepository.save(material);
            
            // When implementing actual download, you might do something like:
            // Path filePath = Paths.get(material.getFilePath());
            // return Files.readAllBytes(filePath);
            return new byte[0];
        } catch (Exception e) {
            log.error("Lỗi khi tải xuống tài liệu: {}", e.getMessage());
            throw new RuntimeException("Tải xuống tài liệu thất bại", e);
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