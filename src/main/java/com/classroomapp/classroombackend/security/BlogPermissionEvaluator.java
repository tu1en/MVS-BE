package com.classroomapp.classroombackend.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Blog;
import com.classroomapp.classroombackend.repository.BlogRepository;

@Component
public class BlogPermissionEvaluator {

    private final BlogRepository blogRepository;

    public BlogPermissionEvaluator(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public boolean isAuthor(Authentication authentication, Long blogId) {
        String userEmail = authentication.getName();
        Blog blog = blogRepository.findById(blogId).orElse(null);
        return blog != null && blog.getAuthor().getEmail().equals(userEmail);
    }
} 