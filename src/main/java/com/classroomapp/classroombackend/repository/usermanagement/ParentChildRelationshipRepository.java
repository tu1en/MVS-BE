package com.classroomapp.classroombackend.repository.usermanagement;

import com.classroomapp.classroombackend.model.usermanagement.ParentChildRelationship;
import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentChildRelationshipRepository extends JpaRepository<ParentChildRelationship, Long> {
    List<ParentChildRelationship> findByParent(User parent);
    List<ParentChildRelationship> findByChild(User child);
    Optional<ParentChildRelationship> findByParentAndChild(User parent, User child);
    List<ParentChildRelationship> findByParentId(Long parentId);
    List<ParentChildRelationship> findByChildId(Long childId);
}