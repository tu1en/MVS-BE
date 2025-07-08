package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    Optional<Blog> findBySlug(String slug);

    List<Blog> findByAuthorId(Long authorId);
    
    List<Blog> findByIsPublishedTrue();
    
    @Query("SELECT b FROM Blog b WHERE b.isPublished = true ORDER BY b.publishedDate DESC")
    List<Blog> findRecentPublishedBlogs();
    
    @Query(value = "SELECT * FROM blogs b WHERE " +
           "LOWER(CAST(b.title AS nvarchar(max))) LIKE LOWER(N'%' + :keyword + '%') OR " + 
           "LOWER(CAST(b.description AS nvarchar(max))) LIKE LOWER(N'%' + :keyword + '%')", 
           nativeQuery = true)
    List<Blog> searchBlogs(@Param("keyword") String keyword);
    
    @Query("SELECT b FROM Blog b WHERE b.tags LIKE CONCAT('%', :tag, '%')")
    List<Blog> findByTag(String tag);
} 