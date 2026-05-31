package com.platform.booking;

import com.platform.booking.dto.request.AddSessionsRequest;
import com.platform.booking.dto.request.CreateOfferingRequest;
import com.platform.booking.dto.response.BookingResponse;
import com.platform.booking.entity.Course;
import com.platform.booking.entity.User;
import com.platform.booking.exception.BookingConflictException;
import com.platform.booking.repo.CourseRepo;
import com.platform.booking.repo.UserRepo;
import com.platform.booking.service.BookingService;
import com.platform.booking.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceTest {

    @Autowired
    UserRepo userRepo;
    @Autowired
    CourseRepo courseRepo;
    @Autowired TeacherService teacherService;
    @Autowired BookingService bookingService;

    Long teacherId;
    Long parentId;
    Long parent2Id;
    Long courseId;

    @BeforeEach
    void setUp() {
        User teacher = userRepo.save(User.builder()
                .name("Teacher T").email("teacher+" + System.nanoTime() + "@t.com")
                .role(User.Role.TEACHER).timezone("America/New_York").build());
        User parent = userRepo.save(User.builder()
                .name("Parent P").email("parent+" + System.nanoTime() + "@p.com")
                .role(User.Role.PARENT).timezone("Asia/Kolkata").build());
        User parent2 = userRepo.save(User.builder()
                .name("Parent Q").email("parent2+" + System.nanoTime() + "@p.com")
                .role(User.Role.PARENT).timezone("Europe/London").build());
        Course course = courseRepo.save(Course.builder()
                .title("Test Course").build());

        teacherId = teacher.getId();
        parentId  = parent.getId();
        parent2Id = parent2.getId();
        courseId  = course.getId();
    }


    private Long createPublishedOffering(String title, LocalDateTime start, LocalDateTime end) {
        CreateOfferingRequest cr = new CreateOfferingRequest();
        cr.setCourseId(courseId);
        cr.setTitle(title);
        cr.setMaxCapacity(20);
        var offering = teacherService.createOffering(teacherId, cr);

        AddSessionsRequest.SessionSlot slot = new AddSessionsRequest.SessionSlot();
        slot.setStartTime(start);
        slot.setEndTime(end);
        AddSessionsRequest ar = new AddSessionsRequest();
        ar.setSessions(List.of(slot));
        teacherService.addSessions(teacherId, offering.getId(), ar);
        teacherService.publishOffering(teacherId, offering.getId());

        return offering.getId();
    }


    @Test
    @DisplayName("Parent can book a published offering with sessions")
    void bookOffering_success() {
        Long offeringId = createPublishedOffering(
                "Python Sat",
                LocalDateTime.of(2030, 6, 7, 18, 0),
                LocalDateTime.of(2030, 6, 7, 19, 0));

        BookingResponse booking = bookingService.bookOffering(parentId, offeringId);

        assertThat(booking.getStatus()).isEqualTo("CONFIRMED");
        assertThat(booking.getOfferingId()).isEqualTo(offeringId);
        assertThat(booking.getSessions()).isNotEmpty();
        assertThat(booking.getSessions().get(0).getDisplayTimezone()).isEqualTo("Asia/Kolkata");
    }

    @Test
    @DisplayName("Booking conflict: overlapping sessions rejected")
    void bookOffering_conflictDetected() {
        Long offeringA = createPublishedOffering(
                "Minecraft Sat",
                LocalDateTime.of(2030, 6, 7, 18, 0),
                LocalDateTime.of(2030, 6, 7, 19, 0));

        // Offering B: June 7, 6:30–7:30 PM NY (overlaps with A)
        Long offeringB = createPublishedOffering(
                "Roblox Sat",
                LocalDateTime.of(2030, 6, 7, 18, 30),
                LocalDateTime.of(2030, 6, 7, 19, 30));

        bookingService.bookOffering(parentId, offeringA);

        assertThatThrownBy(() -> bookingService.bookOffering(parentId, offeringB))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("conflict");
    }

    @Test
    @DisplayName("Non-overlapping sessions: both bookings succeed")
    void bookOffering_noConflict() {
        // Offering A: June 7, 6–7 PM NY
        Long offeringA = createPublishedOffering(
                "Math Morning",
                LocalDateTime.of(2030, 6, 7, 9, 0),
                LocalDateTime.of(2030, 6, 7, 10, 0));

        // Offering B: June 7, 11 AM–12 PM NY (no overlap)
        Long offeringB = createPublishedOffering(
                "Art Afternoon",
                LocalDateTime.of(2030, 6, 7, 11, 0),
                LocalDateTime.of(2030, 6, 7, 12, 0));

        bookingService.bookOffering(parentId, offeringA);
        BookingResponse second = bookingService.bookOffering(parentId, offeringB);

        assertThat(second.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Duplicate booking rejected")
    void bookOffering_duplicateRejected() {
        Long offeringId = createPublishedOffering(
                "Python Eve",
                LocalDateTime.of(2030, 6, 8, 18, 0),
                LocalDateTime.of(2030, 6, 8, 19, 0));

        bookingService.bookOffering(parentId, offeringId);

        assertThatThrownBy(() -> bookingService.bookOffering(parentId, offeringId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("Concurrent: multiple parents booking same offering — all succeed up to capacity")
    void bookOffering_concurrent_multipleParents() throws Exception {
        CreateOfferingRequest cr = new CreateOfferingRequest();
        cr.setCourseId(courseId);
        cr.setTitle("Popular Class");
        cr.setMaxCapacity(5);
        var offering = teacherService.createOffering(teacherId, cr);

        AddSessionsRequest.SessionSlot slot = new AddSessionsRequest.SessionSlot();
        slot.setStartTime(LocalDateTime.of(2030, 7, 1, 10, 0));
        slot.setEndTime(LocalDateTime.of(2030, 7, 1, 11, 0));
        AddSessionsRequest ar = new AddSessionsRequest();
        ar.setSessions(List.of(slot));
        teacherService.addSessions(teacherId, offering.getId(), ar);
        teacherService.publishOffering(teacherId, offering.getId());
        Long offeringId = offering.getId();

        List<Long> parentIds = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 8; i++) {
            User p = userRepo.save(User.builder()
                    .name("ConcP" + i)
                    .email("concp" + i + System.nanoTime() + "@p.com")
                    .role(User.Role.PARENT)
                    .timezone("UTC")
                    .build());
            parentIds.add(p.getId());
        }

        ExecutorService exec = Executors.newFixedThreadPool(8);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures  = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(8);

        for (Long pid : parentIds) {
            exec.submit(() -> {
                try {
                    bookingService.bookOffering(pid, offeringId);
                    successes.incrementAndGet();
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        exec.shutdown();

        assertThat(successes.get()).isEqualTo(5);
        assertThat(failures.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Concurrent: same parent booking same offering twice — only one succeeds")
    void bookOffering_concurrent_sameParent() throws Exception {
        Long offeringId = createPublishedOffering(
                "Race Class",
                LocalDateTime.of(2030, 8, 1, 10, 0),
                LocalDateTime.of(2030, 8, 1, 11, 0));

        ExecutorService exec = Executors.newFixedThreadPool(2);
        AtomicInteger successes = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(2);

        for (int i = 0; i < 2; i++) {
            exec.submit(() -> {
                try {
                    bookingService.bookOffering(parentId, offeringId);
                    successes.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        exec.shutdown();

        assertThat(successes.get()).isEqualTo(1);
    }
}
