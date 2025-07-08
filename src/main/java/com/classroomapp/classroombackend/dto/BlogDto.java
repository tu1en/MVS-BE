package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BlogDto {
    
    private Long id;
    private String slug;
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    private String description;
    
    private String imageUrl;
    
    private String videoUrl;
    
    private LocalDateTime publishedDate;
    
    private LocalDateTime lastEditedDate;
    
    private Long authorId;
    
    private String authorName;
    
    private Long lastEditedById;
    
    private String lastEditedByName;
    
    private Boolean isPublished;
    
    private String status;
    
    private String tags;
    
    private String thumbnailUrl;
    
    private Integer viewCount;

    // Constructors
    public BlogDto() {}

    public BlogDto(Long id, String title, String description, String imageUrl, String videoUrl,
                  LocalDateTime publishedDate, LocalDateTime lastEditedDate, Long authorId,
                  String authorName, Long lastEditedById, String lastEditedByName,
                  Boolean isPublished, String status, String tags, String thumbnailUrl,
                  Integer viewCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.publishedDate = publishedDate;
        this.lastEditedDate = lastEditedDate;
        this.authorId = authorId;
        this.authorName = authorName;
        this.lastEditedById = lastEditedById;
        this.lastEditedByName = lastEditedByName;
        this.isPublished = isPublished;
        this.status = status;
        this.tags = tags;
        this.thumbnailUrl = thumbnailUrl;
        this.viewCount = viewCount;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LocalDateTime getLastEditedDate() {
        return lastEditedDate;
    }

    public void setLastEditedDate(LocalDateTime lastEditedDate) {
        this.lastEditedDate = lastEditedDate;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Long getLastEditedById() {
        return lastEditedById;
    }

    public void setLastEditedById(Long lastEditedById) {
        this.lastEditedById = lastEditedById;
    }

    public String getLastEditedByName() {
        return lastEditedByName;
    }

    public void setLastEditedByName(String lastEditedByName) {
        this.lastEditedByName = lastEditedByName;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
} 