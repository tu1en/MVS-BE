package com.classroomapp.classroombackend.security;

import com.classroomapp.classroombackend.model.Blog;
import com.classroomapp.classroombackend.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("blogPermissionEvaluator")
public class BlogPermissionEvaluator {

    private final BlogRepository blogRepository;

    @Autowired
    public BlogPermissionEvaluator(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public boolean isAuthor(Long blogId, UserDetails userDetails) {
        if (blogId == null || userDetails == null) {
            return false;
        }

        Optional<Blog> blogOpt = blogRepository.findById(blogId);
        if (blogOpt.isEmpty()) {
            return false;
        }

        Blog blog = blogOpt.get();
        
        // The username should match the user ID in your UserDetails implementation
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            return blog.getAuthor().getId().equals(userId);
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 