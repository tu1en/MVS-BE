package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.BlogDto;
import com.classroomapp.classroombackend.dto.CreateBlogDto;

public interface BlogService {
    
    BlogDto createBlog(CreateBlogDto createBlogDto, Long authorId);
    
    BlogDto getBlogById(Long id);

    BlogDto getBlogBySlug(String slug);
    
    List<BlogDto> getAllBlogs();
    
    List<BlogDto> getPublishedBlogs();
    
    List<BlogDto> getBlogsByAuthor(Long authorId);
    
    BlogDto updateBlog(Long id, CreateBlogDto updateBlogDto, Long editorId);
    
    void deleteBlog(Long id);
    
    BlogDto publishBlog(Long id, Long publisherId);
    
    BlogDto unpublishBlog(Long id, Long unpublisherId);
    
    List<BlogDto> searchBlogs(String keyword);
    
    List<BlogDto> getBlogsByTag(String tag);
} 