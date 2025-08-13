package com.classroomapp.classroombackend.repository.parentmanagement;

import com.classroomapp.classroombackend.model.ParentNotificationPrefs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ParentNotificationPrefs entity
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@Repository
public interface ParentNotificationPrefsRepository extends JpaRepository<ParentNotificationPrefs, Long> {

    /**
     * Find preferences by parent ID
     */
    Optional<ParentNotificationPrefs> findByParentId(Long parentId);

    /**
     * Check if preferences exist for parent
     */
    boolean existsByParentId(Long parentId);

    /**
     * Find parents with specific digest frequency
     */
    List<ParentNotificationPrefs> findByDigestFrequency(ParentNotificationPrefs.DigestFrequency digestFrequency);

    /**
     * Find parents with email notifications enabled
     */
    @Query("SELECT pnp FROM ParentNotificationPrefs pnp WHERE JSON_EXTRACT(pnp.channels, '$.email') = true")
    List<ParentNotificationPrefs> findParentsWithEmailEnabled();

    /**
     * Find parents with SMS notifications enabled
     */
    @Query("SELECT pnp FROM ParentNotificationPrefs pnp WHERE JSON_EXTRACT(pnp.channels, '$.sms') = true")
    List<ParentNotificationPrefs> findParentsWithSmsEnabled();

    /**
     * Find parents with in-app notifications enabled
     */
    @Query("SELECT pnp FROM ParentNotificationPrefs pnp WHERE JSON_EXTRACT(pnp.channels, '$.inapp') = true")
    List<ParentNotificationPrefs> findParentsWithInAppEnabled();

    /**
     * Find parents with specific event notifications enabled
     */
    @Query("SELECT pnp FROM ParentNotificationPrefs pnp WHERE JSON_EXTRACT(pnp.eventToggles, :eventPath) = true")
    List<ParentNotificationPrefs> findParentsWithEventEnabled(@Param("eventPath") String eventPath);

    /**
     * Find parents by language preference
     */
    List<ParentNotificationPrefs> findByLanguagePreference(String languagePreference);

    /**
     * Find parents by timezone
     */
    List<ParentNotificationPrefs> findByTimezone(String timezone);

    /**
     * Find parents eligible for digest notifications
     */
    @Query("SELECT pnp FROM ParentNotificationPrefs pnp WHERE pnp.digestFrequency != 'NONE'")
    List<ParentNotificationPrefs> findParentsWithDigestEnabled();

    /**
     * Find parents with daily digest
     */
    @Query("SELECT pnp FROM ParentNotificationPrefs pnp WHERE pnp.digestFrequency = 'DAILY'")
    List<ParentNotificationPrefs> findParentsWithDailyDigest();

    /**
     * Find parents with weekly digest
     */
    @Query("SELECT pnp FROM ParentNotificationPrefs pnp WHERE pnp.digestFrequency = 'WEEKLY'")
    List<ParentNotificationPrefs> findParentsWithWeeklyDigest();
}