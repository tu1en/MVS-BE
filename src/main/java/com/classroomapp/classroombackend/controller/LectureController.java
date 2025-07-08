package com.classroomapp.classroombackend.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.AddMaterialsRequest;
import com.classroomapp.classroombackend.dto.AssessmentDto;
import com.classroomapp.classroombackend.dto.CreateAssessmentDto;
import com.classroomapp.classroombackend.dto.CreateLectureDto;
import com.classroomapp.classroombackend.dto.LectureDetailsDto;
import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.LectureMaterialDto;
import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.dto.LiveStreamStatusDto;
import com.classroomapp.classroombackend.dto.RecordingSessionDto;
import com.classroomapp.classroombackend.dto.UpdateLectureDto;
import com.classroomapp.classroombackend.service.AssessmentService;
import com.classroomapp.classroombackend.service.LectureMaterialService;
import com.classroomapp.classroombackend.service.LectureService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lectures")
@CrossOrigin(origins = "http://localhost:3000")
public class LectureController {
    
    @Autowired
    private LectureService lectureService;

    @Autowired
    private LectureMaterialService lectureMaterialService;

    @Autowired
    private AssessmentService assessmentService;

    // Get single lecture details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<LectureDetailsDto> getLectureById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(lectureService.getLectureById(id, principal));
    }
    
    // Create new lecture
    @PostMapping("/classrooms/{classroomId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LectureDto> createLecture(@PathVariable Long classroomId,
                                                    @Valid @RequestBody CreateLectureDto createLectureDto,
                                                    Principal principal) {
        String userEmail = principal.getName();
        LectureDto createdLecture = lectureService.createLecture(classroomId, createLectureDto, userEmail);
        return new ResponseEntity<>(createdLecture, HttpStatus.CREATED);
    }
    
    // Update lecture
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LectureDto> updateLecture(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLectureDto updateLectureDto,
            Principal principal) {
        String userEmail = principal.getName();
        LectureDto updatedLecture = lectureService.updateLecture(id, updateLectureDto, userEmail);
        return ResponseEntity.ok(updatedLecture);
    }
    
    // Delete lecture
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long id, Principal principal) {
        String userEmail = principal.getName();
        lectureService.deleteLecture(id, userEmail);
        return ResponseEntity.noContent().build();
    }
    
    // Get lectures by course
    @GetMapping("/classrooms/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<List<LectureDto>> getLecturesByClassroom(@PathVariable Long classroomId) {
        List<LectureDto> lectures = lectureService.getLecturesByClassroomId(classroomId);
        return ResponseEntity.ok(lectures);
    }
    
    // Video recording operations for LectureCreator.jsx
    @PostMapping("/{lectureId}/recording/start")
    public ResponseEntity<RecordingSessionDto> startRecording(@PathVariable Long lectureId) {
        RecordingSessionDto session = new RecordingSessionDto();
        session.setId(System.currentTimeMillis());
        session.setLectureId(lectureId);
        session.setStatus("RECORDING");
        session.setStartTime(LocalDateTime.now());
        session.setRecordingUrl("rtmp://recording-server/live/" + session.getId());
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }
    
    @PostMapping("/{lectureId}/recording/stop")
    public ResponseEntity<RecordingSessionDto> stopRecording(
            @PathVariable Long lectureId,
            @RequestParam Long sessionId) {
        RecordingSessionDto session = new RecordingSessionDto();
        session.setId(sessionId);
        session.setLectureId(lectureId);
        session.setStatus("COMPLETED");
        session.setStartTime(LocalDateTime.now().minusHours(1));
        session.setEndTime(LocalDateTime.now());
        session.setRecordingUrl("/api/lectures/" + lectureId + "/recordings/" + sessionId + "/playback");
        return ResponseEntity.ok(session);
    }
    
    // Live streaming operations
    @PostMapping("/{lectureId}/livestream/start")
    public ResponseEntity<LiveStreamDto> startLiveStream(@PathVariable Long lectureId) {
        LiveStreamDto stream = new LiveStreamDto();
        stream.setId(System.currentTimeMillis());
        stream.setLectureId(lectureId);
        stream.setStatus("LIVE");
        stream.setStartTime(LocalDateTime.now());
        stream.setStreamUrl("https://streaming-server/live/" + stream.getId());
        stream.setViewerCount(0);
        return new ResponseEntity<>(stream, HttpStatus.CREATED);
    }
    
    @PostMapping("/{lectureId}/livestream/stop")
    public ResponseEntity<LiveStreamDto> stopLiveStream(
            @PathVariable Long lectureId,
            @RequestParam Long streamId) {
        LiveStreamDto stream = new LiveStreamDto();
        stream.setId(streamId);
        stream.setLectureId(lectureId);
        stream.setStatus("ENDED");
        stream.setStartTime(LocalDateTime.now().minusHours(1));
        stream.setEndTime(LocalDateTime.now());
        return ResponseEntity.ok(stream);
    }
    
    @GetMapping("/{lectureId}/livestream/status")
    public ResponseEntity<LiveStreamStatusDto> getLiveStreamStatus(@PathVariable Long lectureId) {
        LiveStreamStatusDto status = new LiveStreamStatusDto();
        status.setLectureId(lectureId);
        status.setIsLive(false);
        status.setViewerCount(0);
        status.setStreamQuality("HD");
        return ResponseEntity.ok(status);
    }

    // Assessment Endpoints
    @PostMapping("/{lectureId}/assessments")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssessmentDto> createLectureAssessment(
            @PathVariable Long lectureId,
            @Valid @RequestBody CreateAssessmentDto createAssessmentDto,
            Principal principal) {
        AssessmentDto assessment = assessmentService.createAssessment(lectureId, createAssessmentDto, principal.getName());
        return new ResponseEntity<>(assessment, HttpStatus.CREATED);
    }
    
    @GetMapping("/{lectureId}/assessments")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<List<AssessmentDto>> getLectureAssessments(@PathVariable Long lectureId) {
        List<AssessmentDto> assessments = assessmentService.getAssessmentsByLectureId(lectureId);
        return ResponseEntity.ok(assessments);
    }

    @DeleteMapping("/assessments/{assessmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long assessmentId, Principal principal) {
        assessmentService.deleteAssessment(assessmentId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // Material Endpoints
    @PostMapping("/{lectureId}/materials")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<LectureMaterialDto>> addMaterialsToLecture(
            @PathVariable Long lectureId,
            @Valid @RequestBody AddMaterialsRequest request,
            Principal principal) {
        List<LectureMaterialDto> newMaterials = lectureService.addMaterials(lectureId, request.getFiles(), principal.getName());
        return new ResponseEntity<>(newMaterials, HttpStatus.CREATED);
    }

    /**
     * @deprecated This endpoint is deprecated in favor of the new two-step upload process.
     * Use POST /api/files/upload and then POST /api/lectures/{lectureId}/materials.
     */
    @Deprecated
    @PostMapping("/{lectureId}/materials/upload")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LectureMaterialDto> uploadMaterial(
            @PathVariable Long lectureId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        LectureMaterialDto materialDto = lectureMaterialService.storeFile(file, lectureId, principal.getName());
        return new ResponseEntity<>(materialDto, HttpStatus.CREATED);
    }
    
    @GetMapping("/{lectureId}/materials")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<List<LectureMaterialDto>> getLectureMaterials(@PathVariable Long lectureId) {
        List<LectureMaterialDto> materials = lectureMaterialService.getMaterialsByLectureId(lectureId);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/materials/{materialId}/download")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long materialId) {
        return lectureMaterialService.getFile(materialId);
    }

    @DeleteMapping("/materials/{materialId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId, Principal principal) {
        lectureMaterialService.deleteFile(materialId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
