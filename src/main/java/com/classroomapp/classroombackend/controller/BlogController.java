package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.BlogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;
    private final UserRepository userRepository;


    @Autowired
    public BlogController(BlogService blogService, UserRepository userRepository) {
        this.blogService = blogService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<BlogDto> createBlog(
            @Valid @RequestBody CreateBlogDto createBlogDto,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        BlogDto createdBlog = blogService.createBlog(createBlogDto, userId);
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

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BlogDto>> getBlogsByAuthor(@PathVariable Long authorId) {
        List<BlogDto> blogs = blogService.getBlogsByAuthor(authorId);
        return ResponseEntity.ok(blogs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @blogPermissionEvaluator.isAuthor(#id, authentication.principal)")
    public ResponseEntity<BlogDto> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody CreateBlogDto updateBlogDto,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        BlogDto updatedBlog = blogService.updateBlog(id, updateBlogDto, userId);
        return ResponseEntity.ok(updatedBlog);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or @blogPermissionEvaluator.isAuthor(#id, authentication.principal)")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('MANAGER') or @blogPermissionEvaluator.isAuthor(#id, authentication.principal)")
    public ResponseEntity<BlogDto> publishBlog(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        BlogDto publishedBlog = blogService.publishBlog(id, userId);
        return ResponseEntity.ok(publishedBlog);
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('MANAGER') or @blogPermissionEvaluator.isAuthor(#id, authentication.principal)")
    public ResponseEntity<BlogDto> unpublishBlog(
            @PathVariable Long id,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        BlogDto unpublishedBlog = blogService.unpublishBlog(id, userId);
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
    
    // Helper method to extract user ID from Authentication
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            // Or throw an exception if authentication is required
            throw new RuntimeException("User is not authenticated or user details are not available.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); // This is typically the email

        // Find the user by email (username) and return their ID
        return userRepository.findByEmail(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database: " + username));
    }
} 