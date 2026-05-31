package com.platform.booking.repo;

import com.platform.booking.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OfferRepo extends JpaRepository<Offering, Long> {

    List<Offering> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    @Query("SELECT DISTINCT o FROM Offering o JOIN o.sessions s WHERE o.status = 'PUBLISHED' AND s.startTimeUtc > :now ORDER BY o.id")
    List<Offering> findPublishedWithUpcomingSessions(@Param("now") Instant now);
}
