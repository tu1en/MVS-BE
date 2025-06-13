package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.BlogDto;
import com.classroomapp.classroombackend.dto.CreateBlogDto;
import com.classroomapp.classroombackend.model.Blog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.BlogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Autowired
    public BlogServiceImpl(BlogRepository blogRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BlogDto createBlog(CreateBlogDto createBlogDto, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Blog blog = new Blog();
        blog.setTitle(createBlogDto.getTitle());
        blog.setDescription(createBlogDto.getDescription());
        blog.setImageUrl(createBlogDto.getImageUrl());
        blog.setVideoUrl(createBlogDto.getVideoUrl());
        blog.setTags(createBlogDto.getTags());
        blog.setThumbnailUrl(createBlogDto.getThumbnailUrl());
        blog.setAuthor(author);
        blog.setIsPublished(createBlogDto.getIsPublished());
        blog.setStatus(createBlogDto.getIsPublished() ? "published" : "draft");
        
        LocalDateTime now = LocalDateTime.now();
        blog.setLastEditedDate(now);
        blog.setLastEditedBy(author);
        
        if (createBlogDto.getIsPublished()) {
            blog.setPublishedDate(now);
        }
        
        Blog savedBlog = blogRepository.save(blog);
        return convertToDto(savedBlog);
    }

    @Override
    public BlogDto getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        return convertToDto(blog);
    }

    @Override
    public List<BlogDto> getAllBlogs() {
        List<Blog> blogs = blogRepository.findAll();
        return blogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogDto> getPublishedBlogs() {
        List<Blog> blogs = blogRepository.findByIsPublishedTrue();
        return blogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogDto> getBlogsByAuthor(Long authorId) {
        List<Blog> blogs = blogRepository.findByAuthorId(authorId);
        return blogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BlogDto updateBlog(Long id, CreateBlogDto updateBlogDto, Long editorId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new RuntimeException("Editor user not found"));
        
        // Only allow managers or the original author to edit
        if (!isManager(editor) && !blog.getAuthor().getId().equals(editorId)) {
            throw new RuntimeException("Not authorized to edit this blog");
        }
        
        blog.setTitle(updateBlogDto.getTitle());
        blog.setDescription(updateBlogDto.getDescription());
        blog.setImageUrl(updateBlogDto.getImageUrl());
        blog.setVideoUrl(updateBlogDto.getVideoUrl());
        blog.setTags(updateBlogDto.getTags());
        blog.setThumbnailUrl(updateBlogDto.getThumbnailUrl());
        
        // If publishing status is changing
        if (blog.getIsPublished() != updateBlogDto.getIsPublished()) {
            blog.setIsPublished(updateBlogDto.getIsPublished());
            blog.setStatus(updateBlogDto.getIsPublished() ? "published" : "draft");
            
            if (updateBlogDto.getIsPublished()) {
                blog.setPublishedDate(LocalDateTime.now());
            }
        }
        
        blog.setLastEditedDate(LocalDateTime.now());
        blog.setLastEditedBy(editor);
        
        Blog updatedBlog = blogRepository.save(blog);
        return convertToDto(updatedBlog);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new RuntimeException("Blog not found");
        }
        blogRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BlogDto publishBlog(Long id, Long publisherId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        User publisher = userRepository.findById(publisherId)
                .orElseThrow(() -> new RuntimeException("Publisher user not found"));
        
        // Only managers or the original author can publish
        if (!isManager(publisher) && !blog.getAuthor().getId().equals(publisherId)) {
            throw new RuntimeException("Not authorized to publish this blog");
        }
        
        if (!blog.getIsPublished()) {
            blog.setIsPublished(true);
            blog.setStatus("published");
            blog.setPublishedDate(LocalDateTime.now());
            blog.setLastEditedDate(LocalDateTime.now());
            blog.setLastEditedBy(publisher);
            
            Blog updatedBlog = blogRepository.save(blog);
            return convertToDto(updatedBlog);
        }
        
        return convertToDto(blog);
    }

    @Override
    @Transactional
    public BlogDto unpublishBlog(Long id, Long unpublisherId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        User unpublisher = userRepository.findById(unpublisherId)
                .orElseThrow(() -> new RuntimeException("Unpublisher user not found"));
        
        // Only managers or the original author can unpublish
        if (!isManager(unpublisher) && !blog.getAuthor().getId().equals(unpublisherId)) {
            throw new RuntimeException("Not authorized to unpublish this blog");
        }
        
        if (blog.getIsPublished()) {
            blog.setIsPublished(false);
            blog.setStatus("draft");
            blog.setLastEditedDate(LocalDateTime.now());
            blog.setLastEditedBy(unpublisher);
            
            Blog updatedBlog = blogRepository.save(blog);
            return convertToDto(updatedBlog);
        }
        
        return convertToDto(blog);
    }

    @Override
    public List<BlogDto> searchBlogs(String keyword) {
        List<Blog> blogs = blogRepository.searchBlogs(keyword);
        return blogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogDto> getBlogsByTag(String tag) {
        List<Blog> blogs = blogRepository.findByTag(tag);
        return blogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Helper method to check if a user is a manager
    private boolean isManager(User user) {
        // Assuming role_id = 1 is for manager role
        return user.getRoleId() != null && user.getRoleId() == 1;
    }
    
    // Helper method to convert Blog entity to BlogDto
    private BlogDto convertToDto(Blog blog) {
        BlogDto dto = new BlogDto();
        dto.setId(blog.getId());
        dto.setTitle(blog.getTitle());
        dto.setDescription(blog.getDescription());
        dto.setImageUrl(blog.getImageUrl());
        dto.setVideoUrl(blog.getVideoUrl());
        dto.setPublishedDate(blog.getPublishedDate());
        dto.setLastEditedDate(blog.getLastEditedDate());
        
        // Set author information
        dto.setAuthorId(blog.getAuthor().getId());
        dto.setAuthorName(blog.getAuthor().getFullName());
        
        // Set last editor information if available
        if (blog.getLastEditedBy() != null) {
            dto.setLastEditedById(blog.getLastEditedBy().getId());
            dto.setLastEditedByName(blog.getLastEditedBy().getFullName());
        }
        
        dto.setIsPublished(blog.getIsPublished());
        dto.setStatus(blog.getStatus());
        dto.setTags(blog.getTags());
        dto.setThumbnailUrl(blog.getThumbnailUrl());
        dto.setViewCount(blog.getViewCount());
        
        return dto;
    }
} 