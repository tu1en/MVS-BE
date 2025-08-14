package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.BlogDto;
import com.classroomapp.classroombackend.dto.CreateBlogDto;

import com.classroomapp.classroombackend.service.BlogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;

    @Autowired
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogDto> createBlog(
            @Valid @RequestBody CreateBlogDto createBlogDto) {
        
        BlogDto createdBlog = blogService.createBlog(createBlogDto);
        return new ResponseEntity<>(createdBlog, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogDto> getBlogById(@PathVariable Long id) {
        BlogDto blog = blogService.getBlogById(id);
        return ResponseEntity.ok(blog);
    }

    @GetMapping
    public ResponseEntity<List<BlogDto>> getAllBlogs() {
        List<BlogDto> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/published")
    public ResponseEntity<List<BlogDto>> getPublishedBlogs() {
        List<BlogDto> blogs = blogService.getPublishedBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlogDto> getBlogBySlug(@PathVariable String slug) {
        BlogDto blog = blogService.getBlogBySlug(slug);
        return ResponseEntity.ok(blog);
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<BlogDto> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody CreateBlogDto updateBlogDto) {
        
        BlogDto updatedBlog = blogService.updateBlog(id, updateBlogDto);
        return ResponseEntity.ok(updatedBlog);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<BlogDto> publishBlog(
            @PathVariable Long id) {
        
        BlogDto publishedBlog = blogService.publishBlog(id);
        return ResponseEntity.ok(publishedBlog);
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<BlogDto> unpublishBlog(
            @PathVariable Long id) {
        
        BlogDto unpublishedBlog = blogService.unpublishBlog(id);
        return ResponseEntity.ok(unpublishedBlog);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BlogDto>> searchBlogs(@RequestParam String keyword) {
        try {
            // Log the received search keyword
            System.out.println("Received search request with keyword: " + keyword);
            
            // Sanitize input - prevent potential injection or bad requests
            if (keyword == null || keyword.isEmpty()) {
                // Return published blogs if no keyword provided
                return ResponseEntity.ok(blogService.getPublishedBlogs());
            }
            
            List<BlogDto> blogs = blogService.searchBlogs(keyword);
            System.out.println("Search completed, returning " + blogs.size() + " results");
            return ResponseEntity.ok(blogs);
        } catch (Exception e) {
            // Log the error with more details
            System.err.println("Error in /search endpoint for keyword '" + keyword + "': " + e.getMessage());
            e.printStackTrace();
            
            // Return empty list instead of error
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<BlogDto>> getBlogsByTag(@PathVariable String tag) {
        List<BlogDto> blogs = blogService.getBlogsByTag(tag);
        return ResponseEntity.ok(blogs);
    }
    

} 