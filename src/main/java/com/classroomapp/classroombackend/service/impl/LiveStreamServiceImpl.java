package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LiveStream;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.LiveStreamRepository;
import com.classroomapp.classroombackend.service.LiveStreamService;

/**
 * Implementation of LiveStreamService
 */
@Service
@Transactional
public class LiveStreamServiceImpl implements LiveStreamService {
    
    private static final Logger logger = LoggerFactory.getLogger(LiveStreamServiceImpl.class);
    
    @Autowired
    private LiveStreamRepository liveStreamRepository;
    
    @Autowired
    private LectureRepository lectureRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Override
    public LiveStreamDto createLiveStream(Long lectureId, LiveStreamDto streamConfig) {
        logger.info("Creating live stream for lecture ID: {}", lectureId);
        
        // Verify lecture exists
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with id: " + lectureId));
        
        // Check if there's already an active stream for this lecture
        if (hasActiveLiveStream(lectureId)) {
            throw new IllegalStateException("Lecture already has an active live stream");
        }
        
        // Create new live stream
        LiveStream liveStream = new LiveStream();
        liveStream.setLectureId(lectureId);
        liveStream.setStreamKey(generateStreamKey(lectureId));
        liveStream.setStreamUrl(generateStreamUrl(liveStream.getStreamKey()));
        liveStream.setChatEnabled(streamConfig.getChatEnabled() != null ? streamConfig.getChatEnabled() : true);
        liveStream.setMaxViewers(streamConfig.getMaxViewers() != null ? streamConfig.getMaxViewers() : 100);
        liveStream.setCurrentViewers(0);
        liveStream.setStatus(LiveStream.StreamStatus.SCHEDULED);
        liveStream.setRecordingEnabled(streamConfig.getIsRecording() != null ? streamConfig.getIsRecording() : false);
        liveStream.setStartTime(streamConfig.getStartTime() != null ? streamConfig.getStartTime() : LocalDateTime.now());
        
        LiveStream savedStream = liveStreamRepository.save(liveStream);
        logger.info("Created live stream with ID: {} for lecture: {}", savedStream.getId(), lectureId);
        
        return convertToDto(savedStream);
    }
    
    @Override
    public LiveStreamDto startLiveStream(Long streamId) {
        logger.info("Starting live stream with ID: {}", streamId);
        
        LiveStream liveStream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with id: " + streamId));
        
        if (liveStream.getStatus() == LiveStream.StreamStatus.LIVE) {
            throw new IllegalStateException("Live stream is already active");
        }
        
        liveStream.setStatus(LiveStream.StreamStatus.LIVE);
        liveStream.setStartTime(LocalDateTime.now());
        
        LiveStream savedStream = liveStreamRepository.save(liveStream);
        logger.info("Started live stream with ID: {}", streamId);
        
        return convertToDto(savedStream);
    }
    
    @Override
    public LiveStreamDto stopLiveStream(Long streamId) {
        logger.info("Stopping live stream with ID: {}", streamId);
        
        LiveStream liveStream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with id: " + streamId));
        
        liveStream.setStatus(LiveStream.StreamStatus.ENDED);
        liveStream.setEndTime(LocalDateTime.now());
        liveStream.setCurrentViewers(0);
        
        LiveStream savedStream = liveStreamRepository.save(liveStream);
        logger.info("Stopped live stream with ID: {}", streamId);
        
        return convertToDto(savedStream);
    }
    
    @Override
    @Transactional(readOnly = true)
    public LiveStreamDto getLiveStreamById(Long streamId) {
        LiveStream liveStream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with id: " + streamId));
        return convertToDto(liveStream);
    }
    
    @Override
    @Transactional(readOnly = true)
    public LiveStreamDto getActiveLiveStreamByLectureId(Long lectureId) {
        Optional<LiveStream> liveStream = liveStreamRepository.findActiveLiveStreamByLectureId(lectureId);
        return liveStream.map(this::convertToDto).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LiveStreamDto> getLiveStreamsByLectureId(Long lectureId) {
        List<LiveStream> liveStreams = liveStreamRepository.findByLectureIdOrderByStartTimeDesc(lectureId);
        return liveStreams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LiveStreamDto> getActiveLiveStreams() {
        List<LiveStream> liveStreams = liveStreamRepository.findActiveLiveStreams();
        return liveStreams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateViewerCount(Long streamId, Integer viewerCount) {
        logger.debug("Updating viewer count for stream {}: {}", streamId, viewerCount);
        
        LiveStream liveStream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with id: " + streamId));
        
        liveStream.setCurrentViewers(viewerCount);
        
        // Update max viewers if current exceeds it
        if (viewerCount > liveStream.getMaxViewers()) {
            liveStream.setMaxViewers(viewerCount);
        }
        
        liveStreamRepository.save(liveStream);
    }
    
    @Override
    public void addViewer(Long streamId, String viewerUsername) {
        logger.debug("Adding viewer {} to stream {}", viewerUsername, streamId);
        
        LiveStream liveStream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with id: " + streamId));
        
        liveStream.setCurrentViewers(liveStream.getCurrentViewers() + 1);
        liveStreamRepository.save(liveStream);
    }
    
    @Override
    public void removeViewer(Long streamId, String viewerUsername) {
        logger.debug("Removing viewer {} from stream {}", viewerUsername, streamId);
        
        LiveStream liveStream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with id: " + streamId));
        
        int currentViewers = Math.max(0, liveStream.getCurrentViewers() - 1);
        liveStream.setCurrentViewers(currentViewers);
        liveStreamRepository.save(liveStream);
    }
    
    @Override
    @Transactional(readOnly = true)
    public LiveStreamDto getLiveStreamByStreamKey(String streamKey) {
        LiveStream liveStream = liveStreamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with stream key: " + streamKey));
        return convertToDto(liveStream);
    }
    
    @Override
    public LiveStreamDto updateLiveStream(Long streamId, LiveStreamDto streamConfig) {
        logger.info("Updating live stream with ID: {}", streamId);
        
        LiveStream liveStream = liveStreamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found with id: " + streamId));
        
        // Update configurable fields
        if (streamConfig.getChatEnabled() != null) {
            liveStream.setChatEnabled(streamConfig.getChatEnabled());
        }
        if (streamConfig.getMaxViewers() != null) {
            liveStream.setMaxViewers(streamConfig.getMaxViewers());
        }
        if (streamConfig.getIsRecording() != null) {
            liveStream.setRecordingEnabled(streamConfig.getIsRecording());
        }
        if (streamConfig.getEndTime() != null) {
            liveStream.setEndTime(streamConfig.getEndTime());
        }
        
        LiveStream savedStream = liveStreamRepository.save(liveStream);
        return convertToDto(savedStream);
    }
    
    @Override
    public void deleteLiveStream(Long streamId) {
        logger.info("Deleting live stream with ID: {}", streamId);
        
        if (!liveStreamRepository.existsById(streamId)) {
            throw new ResourceNotFoundException("Live stream not found with id: " + streamId);
        }
        
        liveStreamRepository.deleteById(streamId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveLiveStream(Long lectureId) {
        return liveStreamRepository.hasActiveLiveStream(lectureId);
    }
    
    @Override
    public int endExpiredLiveStreams() {
        logger.info("Ending expired live streams");
        
        List<LiveStream> expiredStreams = liveStreamRepository.findLiveStreamsToEnd(LocalDateTime.now());
        
        for (LiveStream stream : expiredStreams) {
            stream.setStatus(LiveStream.StreamStatus.ENDED);
            stream.setEndTime(LocalDateTime.now());
            stream.setCurrentViewers(0);
        }
        
        liveStreamRepository.saveAll(expiredStreams);
        
        logger.info("Ended {} expired live streams", expiredStreams.size());
        return expiredStreams.size();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getTotalActiveViewers() {
        return liveStreamRepository.getTotalActiveViewers();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LiveStreamDto> getLiveStreamsByClassroomId(Long classroomId) {
        List<LiveStream> liveStreams = liveStreamRepository.findByClassroomId(classroomId);
        return liveStreams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public String generateStreamKey(Long lectureId) {
        return "stream_" + lectureId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Override
    public boolean validateStreamConfig(LiveStreamDto streamConfig) {
        if (streamConfig == null) {
            return false;
        }
        
        if (streamConfig.getMaxViewers() != null && streamConfig.getMaxViewers() < 1) {
            return false;
        }
        
        if (streamConfig.getStartTime() != null && streamConfig.getEndTime() != null) {
            return streamConfig.getStartTime().isBefore(streamConfig.getEndTime());
        }
        
        return true;
    }
    
    /**
     * Convert LiveStream entity to DTO
     */
    private LiveStreamDto convertToDto(LiveStream liveStream) {
        LiveStreamDto dto = modelMapper.map(liveStream, LiveStreamDto.class);
        dto.setStatus(liveStream.getStatus().name());
        dto.setViewerCount(liveStream.getCurrentViewers());
        dto.setStartedAt(liveStream.getStartTime());
        dto.setEndedAt(liveStream.getEndTime());
        return dto;
    }
    
    /**
     * Generate stream URL from stream key
     */
    private String generateStreamUrl(String streamKey) {
        return "rtmp://localhost:1935/live/" + streamKey;
    }
}
