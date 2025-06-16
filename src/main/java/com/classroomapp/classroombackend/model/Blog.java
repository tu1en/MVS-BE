package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.classroomapp.classroombackend.model.usermanagement.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "last_edited_date")
    private LocalDateTime lastEditedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = true)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_edited_by", nullable = true)
    private User lastEditedBy;

    @Column(name = "is_published", columnDefinition = "bit default 0")
    private Boolean isPublished = false;
    
    @Column(columnDefinition = "nvarchar(50) default 'draft'")
    private String status;
    
    @Column(name = "tags", length = 255)
    private String tags;
    
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(User lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
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