package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.AssignmentDto;
import com.classroomapp.classroombackend.dto.AssignmentWithFilesDto;
import com.classroomapp.classroombackend.dto.CreateAssignmentDto;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for teacher-specific assignment operations
 */
@RestController
@RequestMapping("/api/teacher/assignments")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TEACHER')")
public class TeacherAssignmentController {

    private final AssignmentService assignmentService;
    private final FileStorageService fileStorageService;
    
    /**
     * Create a new assignment with file attachments
     * @param title Assignment title
     * @param description Assignment description
     * @param dueDate Assignment due date
     * @param maxScore Maximum score for the assignment
     * @param classId Classroom ID
     * @param files File attachments
     * @return Created assignment
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentDto> assignHomework(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("dueDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
            @RequestParam(value = "maxScore", defaultValue = "10") Integer maxScore,
            @RequestParam("classId") Long classId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            log.info("Creating new assignment with title: {}, for class: {}", title, classId);
            
            // Store files if any
            String fileAttachmentUrl = null;
            if (files != null && !files.isEmpty()) {
                List<String> fileUrls = fileStorageService.storeFiles(files);
                // For backward compatibility, store the first file URL in the assignment
                if (!fileUrls.isEmpty()) {
                    fileAttachmentUrl = fileUrls.get(0);
                    log.info("Stored {} files for assignment, primary URL: {}", fileUrls.size(), fileAttachmentUrl);
                }
            }
            
            // Create assignment DTO
            CreateAssignmentDto createAssignmentDto = new CreateAssignmentDto();
            createAssignmentDto.setTitle(title);
            createAssignmentDto.setDescription(description);
            createAssignmentDto.setDueDate(dueDate);
            createAssignmentDto.setPoints(maxScore);
            createAssignmentDto.setClassroomId(classId);
            createAssignmentDto.setFileAttachmentUrl(fileAttachmentUrl);
            
            // Create assignment
            AssignmentDto createdAssignment = assignmentService.CreateAssignment(createAssignmentDto);
            log.info("Assignment created successfully with ID: {}", createdAssignment.getId());
            
            return new ResponseEntity<>(createdAssignment, HttpStatus.CREATED);
        } catch (IOException e) {
            log.error("Error storing files for assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error creating assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an assignment with file attachments
     * @param id Assignment ID
     * @param title Assignment title
     * @param description Assignment description
     * @param dueDate Assignment due date
     * @param maxScore Maximum score for the assignment
     * @param classId Classroom ID
     * @param files File attachments
     * @return Updated assignment
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentDto> updateAssignment(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("dueDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
            @RequestParam(value = "maxScore", defaultValue = "10") Integer maxScore,
            @RequestParam("classId") Long classId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            log.info("Updating assignment with ID: {}", id);
            
            // Get existing assignment to check if we need to delete old files
            AssignmentDto existingAssignment = assignmentService.GetAssignmentById(id);
            String oldFileUrl = existingAssignment.getFileAttachmentUrl();
            
            // Store new files if any
            String fileAttachmentUrl = oldFileUrl;
            if (files != null && !files.isEmpty()) {
                List<String> fileUrls = fileStorageService.storeFiles(files);
                // For backward compatibility, store the first file URL in the assignment
                if (!fileUrls.isEmpty()) {
                    fileAttachmentUrl = fileUrls.get(0);
                    log.info("Stored {} new files for assignment, primary URL: {}", fileUrls.size(), fileAttachmentUrl);
                    
                    // Delete old file if it exists and we have a new one
                    if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
                        fileStorageService.deleteFile(oldFileUrl);
                        log.info("Deleted old file: {}", oldFileUrl);
                    }
                }
            }
            
            // Create assignment DTO for update
            CreateAssignmentDto updateAssignmentDto = new CreateAssignmentDto();
            updateAssignmentDto.setTitle(title);
            updateAssignmentDto.setDescription(description);
            updateAssignmentDto.setDueDate(dueDate);
            updateAssignmentDto.setPoints(maxScore);
            updateAssignmentDto.setClassroomId(classId);
            updateAssignmentDto.setFileAttachmentUrl(fileAttachmentUrl);
            
            // Update assignment
            AssignmentDto updatedAssignment = assignmentService.UpdateAssignment(id, updateAssignmentDto);
            log.info("Assignment updated successfully with ID: {}", updatedAssignment.getId());
            
            return ResponseEntity.ok(updatedAssignment);
        } catch (IOException e) {
            log.error("Error storing files for assignment update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error updating assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
