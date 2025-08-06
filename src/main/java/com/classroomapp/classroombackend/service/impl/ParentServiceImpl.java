package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.ChildDto;
import com.classroomapp.classroombackend.model.usermanagement.ParentChildRelationship;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.ParentChildRelationshipRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentChildRelationshipRepository parentChildRelationshipRepository;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(ParentServiceImpl.class);

    @Override
    public List<ChildDto> getChildrenForParent(Long parentId) {
        List<ParentChildRelationship> relationships = parentChildRelationshipRepository.findByParentId(parentId);
        return relationships.stream()
                .filter(rel -> "ACTIVE".equals(rel.getStatus()))
                .map(rel -> {
                    User child = rel.getChild();
                    return new ChildDto(child.getId(), child.getUsername(), child.getFullName(), child.getEmail());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getParentsForChild(Long childId) {
        List<ParentChildRelationship> relationships = parentChildRelationshipRepository.findByChildId(childId);
        return relationships.stream()
                .filter(rel -> "ACTIVE".equals(rel.getStatus()))
                .map(ParentChildRelationship::getParent)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParentChildRelationship addChildToParent(Long parentId, Long childId, ParentChildRelationship.RelationshipType relationshipType) {
        // Validate that parent and child exist
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found with ID: " + parentId));
        
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found with ID: " + childId));

        // Check if relationship already exists
        Optional<ParentChildRelationship> existingRelationship = parentChildRelationshipRepository.findByParentAndChild(parent, child);
        if (existingRelationship.isPresent()) {
            throw new RuntimeException("Parent-child relationship already exists");
        }

        // Create new relationship
        ParentChildRelationship relationship = new ParentChildRelationship();
        relationship.setParent(parent);
        relationship.setChild(child);
        relationship.setRelationshipType(relationshipType);
        relationship.setStatus("ACTIVE");

        return parentChildRelationshipRepository.save(relationship);
    }

    @Override
    @Transactional
    public void removeChildFromParent(Long parentId, Long childId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found with ID: " + parentId));
        
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found with ID: " + childId));

        Optional<ParentChildRelationship> relationship = parentChildRelationshipRepository.findByParentAndChild(parent, child);
        if (relationship.isPresent()) {
            parentChildRelationshipRepository.delete(relationship.get());
        } else {
            throw new RuntimeException("Parent-child relationship not found");
        }
    }

    @Override
    public boolean hasParentChildRelationship(Long parentId, Long childId) {
        User parent = userRepository.findById(parentId).orElse(null);
        User child = userRepository.findById(childId).orElse(null);
        
        if (parent == null || child == null) {
            return false;
        }

        Optional<ParentChildRelationship> relationship = parentChildRelationshipRepository.findByParentAndChild(parent, child);
        return relationship.isPresent() && "ACTIVE".equals(relationship.get().getStatus());
    }

    @Override
    public ParentChildRelationship getParentChildRelationship(Long parentId, Long childId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found with ID: " + parentId));
        
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found with ID: " + childId));

        return parentChildRelationshipRepository.findByParentAndChild(parent, child)
                .orElseThrow(() -> new RuntimeException("Parent-child relationship not found"));
    }

    @Override
    @Transactional
    public void updateRelationshipStatus(Long parentId, Long childId, String status) {
        ParentChildRelationship relationship = getParentChildRelationship(parentId, childId);
        relationship.setStatus(status);
        parentChildRelationshipRepository.save(relationship);
    }
} 