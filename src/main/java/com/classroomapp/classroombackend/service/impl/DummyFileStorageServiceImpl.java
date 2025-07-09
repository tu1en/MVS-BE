package com.classroomapp.classroombackend.service.impl;

import java.util.Objects;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.exception.FileStorageException;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Profile("dev") // This service will only be active when the 'dev' profile is active
public class DummyFileStorageServiceImpl implements FileStorageService {

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        // Get original filename
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        log.info("Received file to upload to folder {}: {}", folder, originalFilename);

        // Basic validation
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFilename);
        }

        // Generate a unique filename
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;

        // Construct a dummy URL, incorporating the folder for realism
        String dummyFileUrl = "https://dummy-storage-provider.com/files/" + folder + "/" + uniqueFileName;
        log.info("Simulating file upload. Storing as {} with URL {}", uniqueFileName, dummyFileUrl);

        // Return the response object
        return new FileUploadResponse(
                uniqueFileName,
                dummyFileUrl,
                file.getContentType(),
                file.getSize()
        );
    }

    @Override
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("Attempted to delete a file with null or blank name.");
            return;
        }
        log.info("Simulating deletion of file from storage: {}", fileName);
        // In a real implementation, this would contain logic to delete the file from S3, Firebase, etc.
    }
} 