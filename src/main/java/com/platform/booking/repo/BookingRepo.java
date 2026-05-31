package com.platform.booking.repo;

import com.platform.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long> {

    List<Booking> findByParentIdOrderByBookedAtDesc(Long parentId);

    Optional<Booking> findByParentIdAndOfferingId(Long parentId, Long offeringId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.offering.id = :offeringId AND b.status = 'CONFIRMED'")
    long countConfirmedByOfferingId(@Param("offeringId") Long offeringId);
}
