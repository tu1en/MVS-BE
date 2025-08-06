package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ChildDto;
import com.classroomapp.classroombackend.model.usermanagement.ParentChildRelationship;
import com.classroomapp.classroombackend.model.usermanagement.User;

import java.util.List;

public interface ParentService {
    
    /**
     * Get all children for a parent
     * @param parentId the parent's ID
     * @return list of children
     */
    List<ChildDto> getChildrenForParent(Long parentId);
    
    /**
     * Get all parents for a child
     * @param childId the child's ID
     * @return list of parents
     */
    List<User> getParentsForChild(Long childId);
    
    /**
     * Add a child to a parent
     * @param parentId the parent's ID
     * @param childId the child's ID
     * @param relationshipType the type of relationship
     * @return the created relationship
     */
    ParentChildRelationship addChildToParent(Long parentId, Long childId, ParentChildRelationship.RelationshipType relationshipType);
    
    /**
     * Remove a child from a parent
     * @param parentId the parent's ID
     * @param childId the child's ID
     */
    void removeChildFromParent(Long parentId, Long childId);
    
    /**
     * Check if a parent-child relationship exists
     * @param parentId the parent's ID
     * @param childId the child's ID
     * @return true if relationship exists
     */
    boolean hasParentChildRelationship(Long parentId, Long childId);
    
    /**
     * Get parent-child relationship
     * @param parentId the parent's ID
     * @param childId the child's ID
     * @return the relationship if exists
     */
    ParentChildRelationship getParentChildRelationship(Long parentId, Long childId);
    
    /**
     * Update relationship status
     * @param parentId the parent's ID
     * @param childId the child's ID
     * @param status the new status
     */
    void updateRelationshipStatus(Long parentId, Long childId, String status);
} 