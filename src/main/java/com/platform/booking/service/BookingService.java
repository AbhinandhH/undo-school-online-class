package com.platform.booking.service;

import com.platform.booking.dto.response.BookingResponse;
import com.platform.booking.dto.response.OfferingResponse;
import com.platform.booking.dto.response.SessionResponse;
import com.platform.booking.entity.*;
import com.platform.booking.exception.AccessDeniedException;
import com.platform.booking.exception.BookingConflictException;
import com.platform.booking.exception.ResourceNotFoundException;
import com.platform.booking.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    private final UserRepo userRepo;
    private final OfferRepo offerRepo;
    private final SessionRepo sessionRepo;
    private final BookingRepo bookingRepo;

    public  BookingService(UserRepo userRepo, OfferRepo offerRepo, SessionRepo sessionRepo, BookingRepo bookingRepo) {
        this.userRepo = userRepo;
        this.offerRepo = offerRepo;
        this.sessionRepo = sessionRepo;
        this.bookingRepo = bookingRepo;
    }


    @Transactional(readOnly = true)
    public List<OfferingResponse> getAvailableOfferings(Long parentId) {
        User parent = resolveParent(parentId);
        List<Offering> offerings = offerRepo.findPublishedWithUpcomingSessions(Instant.now());

        return offerings.stream()
                .map(o -> buildOfferingResponse(o, parent.getTimezone()))
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingResponse bookOffering(Long parentId, Long offeringId) {
        User parent = resolveParent(parentId);

        Offering offering = offerRepo.findById(offeringId)
                .orElseThrow(() -> ResourceNotFoundException.of("Offering", offeringId));

        if (offering.getStatus() != Offering.Status.PUBLISHED) {
            throw new IllegalStateException("Offering is not available for booking.");
        }

        // ── Already booked? ───────────────────────────────────────────────────
        bookingRepo.findByParentIdAndOfferingId(parentId, offeringId)
                .ifPresent(b -> {
                    if (b.getStatus() == Booking.Status.CONFIRMED) {
                        throw new IllegalStateException("You have already booked this offering.");
                    }
                });


        long confirmed = bookingRepo.countConfirmedByOfferingId(offeringId);
        if (confirmed >= offering.getMaxCapacity()) {
            throw new IllegalStateException("Offering is fully booked (capacity: "
                    + offering.getMaxCapacity() + ").");
        }


        List<Session> targetSessions = sessionRepo
                .findByOfferingIdOrderByStartTimeUtcAsc(offeringId);
        if (targetSessions.isEmpty()) {
            throw new IllegalStateException("Offering has no sessions scheduled.");
        }


        List<Session> existingSessions = sessionRepo.findLockedSessionsForParent(parentId);


        for (Session target : targetSessions) {
            for (Session existing : existingSessions) {
                if (overlaps(target, existing)) {
                    throw new BookingConflictException(
                            "Session conflict: the offering you are trying to book has a session on "
                            + target.getStartTimeUtc()
                            + " that overlaps with your existing booking for offering id="
                            + existing.getOffering().getId() + ".");
                }
            }
        }


        Booking booking = Booking.builder()
                .offering(offering)
                .parent(parent)
                .status(Booking.Status.CONFIRMED)
                .build();

        booking = bookingRepo.save(booking);
        log.info("Booking created: bookingId={} parentId={} offeringId={}", booking.getId(), parentId, offeringId);

        return buildBookingResponse(booking, parent.getTimezone());
    }



    @Transactional(readOnly = true)
    public List<BookingResponse> getParentBookings(Long parentId) {
        User parent = resolveParent(parentId);
        List<Booking> bookings = bookingRepo.findByParentIdOrderByBookedAtDesc(parentId);
        return bookings.stream()
                .map(b -> buildBookingResponse(b, parent.getTimezone()))
                .collect(Collectors.toList());
    }


    @Transactional
    public BookingResponse cancelBooking(Long parentId, Long bookingId) {
        resolveParent(parentId);
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

        if (!booking.getParent().getId().equals(parentId)) {
            throw new AccessDeniedException("You can only cancel your own bookings.");
        }
        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled.");
        }

        booking.setStatus(Booking.Status.CANCELLED);
        booking = bookingRepo.save(booking);
        log.info("Booking cancelled: bookingId={} parentId={}", bookingId, parentId);

        User parent = userRepo.findById(parentId).orElseThrow();
        return buildBookingResponse(booking, parent.getTimezone());
    }


    private boolean overlaps(Session a, Session b) {
        return a.getStartTimeUtc().isBefore(b.getEndTimeUtc())
                && a.getEndTimeUtc().isAfter(b.getStartTimeUtc());
    }

    private User resolveParent(Long parentId) {
        User user = userRepo.findById(parentId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", parentId));
        if (user.getRole() != User.Role.PARENT) {
            throw new AccessDeniedException("User " + parentId + " is not a parent.");
        }
        return user;
    }

    private BookingResponse buildBookingResponse(Booking booking, String viewerTimezone) {
        List<Session> sessions = sessionRepo
                .findByOfferingIdOrderByStartTimeUtcAsc(booking.getOffering().getId());
        List<SessionResponse> sessionResponses = sessions.stream()
                .map(s -> SessionResponse.buildSessionResponse(s, viewerTimezone))
                .collect(Collectors.toList());
        return BookingResponse.buildBookingResponse(booking, sessionResponses);
    }

    private OfferingResponse buildOfferingResponse(Offering offering, String viewerTimezone) {
        List<Session> sessions = sessionRepo
                .findByOfferingIdOrderByStartTimeUtcAsc(offering.getId());
        List<SessionResponse> sessionResponses = sessions.stream()
                .map(s -> SessionResponse.buildSessionResponse(s, viewerTimezone))
                .collect(Collectors.toList());
        long confirmed = bookingRepo.countConfirmedByOfferingId(offering.getId());
        return OfferingResponse.buildOfferingResponse(offering, sessionResponses, confirmed);
    }
}
