package com.classroomapp.classroombackend.service.firebase.impl;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.service.firebase.FirebaseStorageService;

import lombok.extern.slf4j.Slf4j;

@Service("firebaseStorageService")
@Profile("local")
@Slf4j
public class LocalFirebaseStorageServiceImpl implements FirebaseStorageService {

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        log.info("Local Firebase Storage Service: Simulating file upload for {}", file.getOriginalFilename());
        
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileUrl = "http://localhost:8088/local-files/" + folder + "/" + fileName;
        
        return FileUploadResponse.builder()
            .success(true)
            .filename(fileName)
            .originalFilename(file.getOriginalFilename())
            .fileUrl(fileUrl)
            .fileType(file.getContentType())
            .size(file.getSize())
            .build();
    }

    @Override
    public boolean deleteFile(String filePath) {
        log.info("Local Firebase Storage Service: Simulating file deletion for {}", filePath);
        return true;
    }

    @Override
    public String getDownloadUrl(String filePath) {
        log.info("Local Firebase Storage Service: Getting download URL for {}", filePath);
        return "http://localhost:8088/local-files/" + filePath;
    }

    @Override
    public boolean fileExists(String filePath) {
        log.info("Local Firebase Storage Service: Checking if file exists: {}", filePath);
        return true;
    }

    @Override
    public String generateSignedUrl(String filePath, int expirationMinutes) {
        log.info("Local Firebase Storage Service: Generating signed URL for {}", filePath);
        return "http://localhost:8088/local-files/" + filePath + "?expires=" + expirationMinutes;
    }
}