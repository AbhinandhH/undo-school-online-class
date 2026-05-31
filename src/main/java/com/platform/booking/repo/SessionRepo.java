package com.platform.booking.repo;

import com.platform.booking.entity.Session;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SessionRepo extends JpaRepository<Session, Long> {

    List<Session> findByOfferingIdOrderByStartTimeUtcAsc(Long offeringId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Session s WHERE s.offering.id IN (SELECT b.offering.id FROM Booking b WHERE b.parent.id = :parentId AND b.status = 'CONFIRMED')")
    List<Session> findLockedSessionsForParent(@Param("parentId") Long parentId);


    @Query("SELECT s FROM Session s WHERE s.offering.id = :offeringId AND s.startTimeUtc < :end AND s.endTimeUtc   > :start")
    List<Session> findOverlappingInOffering(
            @Param("offeringId") Long offeringId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT s FROM Session s  WHERE s.teacher.id = :teacherId AND s.startTimeUtc > :now ORDER BY s.startTimeUtc ASC ")
    List<Session> findUpcomingByTeacher(@Param("teacherId") Long teacherId, @Param("now") Instant now);
}
