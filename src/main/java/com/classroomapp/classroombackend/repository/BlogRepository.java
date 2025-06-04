package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    List<Blog> findByAuthorId(Long authorId);
    
    List<Blog> findByIsPublishedTrue();
    
    @Query("SELECT b FROM Blog b WHERE b.isPublished = true ORDER BY b.publishedDate DESC")
    List<Blog> findRecentPublishedBlogs();
    
    @Query("SELECT b FROM Blog b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Blog> searchBlogs(String keyword);
    
    @Query("SELECT b FROM Blog b WHERE b.tags LIKE CONCAT('%', :tag, '%')")
    List<Blog> findByTag(String tag);
} 