package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.classroomapp.classroombackend.dto.AssessmentDto;
import com.classroomapp.classroombackend.dto.CreateAssessmentDto;
import com.classroomapp.classroombackend.dto.CreateLectureDto;
import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.LectureMaterialDto;
import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.dto.LiveStreamStatusDto;
import com.classroomapp.classroombackend.dto.RecordingSessionDto;
import com.classroomapp.classroombackend.dto.UpdateLectureDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lectures")
@CrossOrigin(origins = "http://localhost:3000")
public class LectureController {
    
    private final Map<Long, List<LectureDto>> lecturesByCourseDb; 
    private final AtomicLong materialIdCounter = new AtomicLong(1000); 

    public LectureController(CourseController courseController) {
        this.lecturesByCourseDb = courseController.lecturesByCourse; 
    }

    // Extended functionality for LectureCreator.jsx
    
    // Get single lecture details
    @GetMapping("/{id}")
    public ResponseEntity<LectureDto> getLecture(@PathVariable Long id) {
        // Search for lecture across all courses
        for (List<LectureDto> lecturesInCourse : lecturesByCourseDb.values()) {
            for (LectureDto lecture : lecturesInCourse) {
                if (lecture.getId().equals(id)) {
                    return ResponseEntity.ok(lecture);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    // Create new lecture
    @PostMapping
    public ResponseEntity<LectureDto> createLecture(@Valid @RequestBody CreateLectureDto createLectureDto) {
        LectureDto lecture = new LectureDto();
        lecture.setId(System.currentTimeMillis());
        lecture.setTitle(createLectureDto.getTitle());
        lecture.setDescription(createLectureDto.getDescription());
        lecture.setCreatedDate(LocalDateTime.now());
        lecture.setStatus("DRAFT");
        
        // Add to course lectures list (assuming courseId exists)
        List<LectureDto> courseLectures = lecturesByCourseDb.get(createLectureDto.getCourseId());
        if (courseLectures != null) {
            courseLectures.add(lecture);
        }
        
        return new ResponseEntity<>(lecture, HttpStatus.CREATED);
    }
    
    // Update lecture
    @PutMapping("/{id}")
    public ResponseEntity<LectureDto> updateLecture(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLectureDto updateLectureDto) {
        
        // Find and update lecture
        for (List<LectureDto> lecturesInCourse : lecturesByCourseDb.values()) {
            for (LectureDto lecture : lecturesInCourse) {
                if (lecture.getId().equals(id)) {
                    lecture.setTitle(updateLectureDto.getTitle());
                    lecture.setDescription(updateLectureDto.getDescription());
                    return ResponseEntity.ok(lecture);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    // Delete lecture
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long id) {
        for (List<LectureDto> lecturesInCourse : lecturesByCourseDb.values()) {
            lecturesInCourse.removeIf(lecture -> lecture.getId().equals(id));
        }
        return ResponseEntity.noContent().build();
    }
    
    // Get lectures by course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LectureDto>> getLecturesByCourse(@PathVariable Long courseId) {
        List<LectureDto> lectures = lecturesByCourseDb.get(courseId);
        if (lectures != null) {
            return ResponseEntity.ok(lectures);
        }
        return ResponseEntity.ok(List.of()); // Return empty list if no lectures found
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
    
    // Assessment integration for LectureCreator.jsx
    @PostMapping("/{lectureId}/assessments")
    public ResponseEntity<AssessmentDto> createLectureAssessment(
            @PathVariable Long lectureId,
            @Valid @RequestBody CreateAssessmentDto createAssessmentDto) {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(System.currentTimeMillis());
        assessment.setLectureId(lectureId);
        assessment.setTitle(createAssessmentDto.getTitle());
        assessment.setDescription(createAssessmentDto.getDescription());
        assessment.setAssessmentType(createAssessmentDto.getAssessmentType());
        assessment.setTimeLimit(createAssessmentDto.getTimeLimit());
        assessment.setCreatedDate(LocalDateTime.now());
        return new ResponseEntity<>(assessment, HttpStatus.CREATED);
    }
    
    @GetMapping("/{lectureId}/assessments")
    public ResponseEntity<List<AssessmentDto>> getLectureAssessments(@PathVariable Long lectureId) {
        // Mock implementation - return sample assessments
        List<AssessmentDto> assessments = List.of(
            createMockAssessment(1L, lectureId, "Quick Quiz", "QUIZ"),
            createMockAssessment(2L, lectureId, "Understanding Check", "POLL")
        );
        return ResponseEntity.ok(assessments);
    }
    
    // Helper method for mock assessment
    private AssessmentDto createMockAssessment(Long id, Long lectureId, String title, String assessmentType) {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(id);
        assessment.setLectureId(lectureId);
        assessment.setTitle(title);
        assessment.setDescription("Sample assessment for lecture");
        assessment.setAssessmentType(assessmentType);
        assessment.setTimeLimit(15); // 15 minutes
        assessment.setCreatedDate(LocalDateTime.now());
        return assessment;
    }

    // Task 31: Upload tài liệu bài giảng
    @PostMapping("/{lectureId}/materials/upload")
    public ResponseEntity<LectureMaterialDto> uploadMaterial(
            @PathVariable Long lectureId,
            @RequestParam("file") MultipartFile file) {
        
        System.out.println("Yêu cầu upload tài liệu cho bài giảng ID: " + lectureId);
        System.out.println("Tên file: " + file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        
        LectureMaterialDto materialDto = new LectureMaterialDto(
            materialIdCounter.getAndIncrement(),
            file.getOriginalFilename(),
            file.getContentType(),
            "/api/lectures/materials/download/" + file.getOriginalFilename(),
            lectureId
        );

        boolean lectureFoundAndUpdate = false;
        // Tìm bài giảng và thêm tài liệu
        outerloop:
        for (List<LectureDto> lecturesInCourse : lecturesByCourseDb.values()) {
            for (LectureDto lecture : lecturesInCourse) {
                if (lecture.getId().equals(lectureId)) {
                    lecture.addMaterial(materialDto);
                    lectureFoundAndUpdate = true;
                    break outerloop;
                }
            }
        }

        if (!lectureFoundAndUpdate) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(materialDto);
    }
}
