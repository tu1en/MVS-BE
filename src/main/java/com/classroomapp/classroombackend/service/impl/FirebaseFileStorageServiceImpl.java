package com.classroomapp.classroombackend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.firebase.FirebaseStorageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@Slf4j
public class FirebaseFileStorageServiceImpl implements FileStorageService {

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        log.info("Firebase File Storage Service: Uploading file {} to folder {}", file.getOriginalFilename(), folder);
        try {
            // Gọi trực tiếp service upload của Firebase
            com.classroomapp.classroombackend.dto.common.FileUploadResponse firebaseResponse =
                firebaseStorageService.uploadFile(file, folder);

            // Trả về response đúng định dạng FileUploadResponse
            return new FileUploadResponse(
                firebaseResponse.getFileName(),
                firebaseResponse.getFileUrl(),
                firebaseResponse.getFileType(),
                firebaseResponse.getFileSize()
            );
        } catch (Exception e) {
            log.error("Firebase File Storage Service: Error uploading file", e);
            throw new RuntimeException("Failed to upload file to Firebase Storage", e);
        }
    }

    @Override
    public void delete(String fileName) {
        log.info("Firebase File Storage Service: Deleting file {}", fileName);
        try {
            firebaseStorageService.deleteFile(fileName);
            log.info("Firebase File Storage Service: File deleted successfully");
        } catch (Exception e) {
            log.error("Firebase File Storage Service: Error deleting file", e);
            throw new RuntimeException("Failed to delete file from Firebase Storage", e);
        }
    }
} 