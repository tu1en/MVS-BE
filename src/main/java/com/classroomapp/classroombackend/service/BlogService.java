package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.BlogDto;
import com.classroomapp.classroombackend.dto.CreateBlogDto;

public interface BlogService {
    
    BlogDto createBlog(CreateBlogDto createBlogDto);
    
    BlogDto getBlogById(Long id);

    BlogDto getBlogBySlug(String slug);
    
    List<BlogDto> getAllBlogs();
    
    List<BlogDto> getPublishedBlogs();
    

    
    BlogDto updateBlog(Long id, CreateBlogDto updateBlogDto);
    
    void deleteBlog(Long id);
    
    BlogDto publishBlog(Long id);
    
    BlogDto unpublishBlog(Long id);
    
    List<BlogDto> searchBlogs(String keyword);
    
    List<BlogDto> getBlogsByTag(String tag);
} 