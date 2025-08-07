package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.entity.ClassEntity.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    
    List<ClassEntity> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    
    List<ClassEntity> findByCourseTemplateIdOrderByCreatedAtDesc(Long courseTemplateId);
    
    List<ClassEntity> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    
    Optional<ClassEntity> findByClassName(String className);
    
    boolean existsByClassName(String className);
    
    List<ClassEntity> findByStatusOrderByCreatedAtDesc(ClassStatus status);
    
    List<ClassEntity> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT c FROM ClassEntity c WHERE c.room.id = :roomId AND NOT (c.endDate < :startDate OR c.startDate > :endDate)")
    List<ClassEntity> findConflictingClassesByRoom(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT c FROM ClassEntity c WHERE c.teacher.id = :teacherId AND NOT (c.endDate < :startDate OR c.startDate > :endDate)")
    List<ClassEntity> findConflictingClassesByTeacher(
        @Param("teacherId") Long teacherId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT c FROM ClassEntity c WHERE c.room.id = :roomId AND c.startDate <= :startDate AND c.endDate >= :endDate")
    List<ClassEntity> findActiveClassesByRoom(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    List<ClassEntity> findByCourseTemplateIdAndStatus(Long courseTemplateId, ClassStatus status);
    
    @Query("SELECT c FROM ClassEntity c ORDER BY c.createdAt DESC")
    Page<ClassEntity> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    long countByStatus(ClassStatus status);
    
    long countByCourseTemplateId(Long courseTemplateId);
    
    @Query("SELECT c FROM ClassEntity c WHERE c.status IN ('PLANNING', 'ACTIVE')")
    List<ClassEntity> findActiveAndPlanningClasses();
    
    List<ClassEntity> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    List<ClassEntity> findTop10ByOrderByCreatedAtDesc();
    
    // Additional methods for room management
    @Query("SELECT c FROM ClassEntity c WHERE c.room.id = :roomId " +
           "AND c.startDate BETWEEN :startDate AND :endDate " +
           "AND c.status IN ('PLANNING', 'ACTIVE') " +
           "ORDER BY c.startDate ASC")
    List<ClassEntity> findByRoomIdAndDateRange(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT c FROM ClassEntity c WHERE c.room.id IN :roomIds " +
           "AND c.startDate BETWEEN :startDate AND :endDate " +
           "AND c.status IN ('PLANNING', 'ACTIVE') " +
           "ORDER BY c.startDate ASC")
    List<ClassEntity> findByRoomIdsAndDateRange(
        @Param("roomIds") List<Long> roomIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT c FROM ClassEntity c WHERE c.room.id = :roomId " +
           "AND c.startDate = :date " +
           "AND c.status IN ('PLANNING', 'ACTIVE') " +
           "AND (:excludeClassId IS NULL OR c.id != :excludeClassId)")
    List<ClassEntity> findConflictingClasses(
        @Param("roomId") Long roomId,
        @Param("date") LocalDate date,
        @Param("excludeClassId") Long excludeClassId
    );
}