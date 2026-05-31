package com.platform.booking.service;

import com.platform.booking.timezoneconfig.TimezoneConverter;
import com.platform.booking.dto.request.AddSessionsRequest;
import com.platform.booking.dto.request.CreateOfferingRequest;
import com.platform.booking.dto.response.OfferingResponse;
import com.platform.booking.dto.response.SessionResponse;
import com.platform.booking.entity.*;
import com.platform.booking.exception.AccessDeniedException;
import com.platform.booking.exception.ResourceNotFoundException;
import com.platform.booking.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final UserRepo userRepo;
    private final CourseRepo courseRepo;
    private final OfferRepo offerRepo;
    private final SessionRepo sessionRepo;
    private final BookingRepo bookingRepo;
    private final TimezoneConverter timezoneConverter;

    public TeacherService(UserRepo userRepo, CourseRepo courseRepo,OfferRepo offerRepo,SessionRepo sessionRepo,BookingRepo bookingRepo, TimezoneConverter timezoneConverter) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.offerRepo = offerRepo;
        this.sessionRepo = sessionRepo;
        this.bookingRepo = bookingRepo;
        this.timezoneConverter = timezoneConverter;
    }


    @Transactional
    public OfferingResponse createOffering(Long teacherId, CreateOfferingRequest req) {
        User teacher = resolveTeacher(teacherId);
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Course", req.getCourseId()));

        Offering offering = Offering.builder()
                .course(course)
                .teacher(teacher)
                .title(req.getTitle())
                .description(req.getDescription())
                .maxCapacity(req.getMaxCapacity())
                .status(Offering.Status.DRAFT)
                .build();

        offering = offerRepo.save(offering);
        return buildOfferingResponse(offering, teacher.getTimezone());
    }


    @Transactional
    public OfferingResponse addSessions(Long teacherId, Long offeringId, AddSessionsRequest req) {
        User teacher = resolveTeacher(teacherId);
        Offering offering = offerRepo.findById(offeringId)
                .orElseThrow(() -> ResourceNotFoundException.of("Offering", offeringId));

        if (!offering.getTeacher().getId().equals(teacherId)) {
            throw new AccessDeniedException("You can only add sessions to your own offerings.");
        }
        if (offering.getStatus() == Offering.Status.CANCELLED) {
            throw new IllegalStateException("Cannot add sessions to a cancelled offering.");
        }

        String teacherTz = teacher.getTimezone();

        List<Session> newSessions = req.getSessions().stream().map(slot -> {
            Instant start = timezoneConverter.toUtc(slot.getStartTime(), teacherTz);
            Instant end   = timezoneConverter.toUtc(slot.getEndTime(), teacherTz);

            if (!end.isAfter(start)) {
                throw new IllegalArgumentException(
                        "Session end must be after start: " + slot.getStartTime());
            }

            List<Session> overlaps = sessionRepo.findOverlappingInOffering(offeringId, start, end);
            if (!overlaps.isEmpty()) {
                throw new IllegalArgumentException(
                        "Session overlaps with an existing session in this offering: " + slot.getStartTime());
            }

            return Session.builder()
                    .offering(offering)
                    .teacher(teacher)
                    .startTimeUtc(start)
                    .endTimeUtc(end)
                    .build();
        }).collect(Collectors.toList());

        sessionRepo.saveAll(newSessions);
        return buildOfferingResponse(offering, teacherTz);
    }


    @Transactional
    public OfferingResponse publishOffering(Long teacherId, Long offeringId) {
        User teacher = resolveTeacher(teacherId);
        Offering offering = offerRepo.findById(offeringId)
                .orElseThrow(() -> ResourceNotFoundException.of("Offering", offeringId));

        if (!offering.getTeacher().getId().equals(teacherId)) {
            throw new AccessDeniedException("You can only publish your own offerings.");
        }

        List<Session> sessions = sessionRepo.findByOfferingIdOrderByStartTimeUtcAsc(offeringId);
        if (sessions.isEmpty()) {
            throw new IllegalStateException("Cannot publish an offering with no sessions.");
        }

        offering.setStatus(Offering.Status.PUBLISHED);
        offerRepo.save(offering);
        return buildOfferingResponse(offering, teacher.getTimezone());
    }


    @Transactional(readOnly = true)
    public List<OfferingResponse> getTeacherOfferings(Long teacherId) {
        User teacher = resolveTeacher(teacherId);
        List<Offering> offerings = offerRepo.findByTeacherIdOrderByCreatedAtDesc(teacherId);

        return offerings.stream()
                .map(o -> buildOfferingResponse(o, teacher.getTimezone()))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<SessionResponse> getUpcomingSessions(Long teacherId) {
        User teacher = resolveTeacher(teacherId);
        List<Session> sessions = sessionRepo.findUpcomingByTeacher(teacherId, Instant.now());
        String tz = teacher.getTimezone();
        return sessions.stream()
                .map(s -> SessionResponse.buildSessionResponse(s, tz))
                .collect(Collectors.toList());
    }


    private User resolveTeacher(Long teacherId) {
        User user = userRepo.findById(teacherId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", teacherId));
        if (user.getRole() != User.Role.TEACHER) {
            throw new AccessDeniedException("User " + teacherId + " is not a teacher.");
        }
        return user;
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
