package com.platform.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "bookings",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_booking_parent_offering",
        columnNames = {"parent_id", "offering_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "booked_at", nullable = false, updatable = false)
    private Instant bookedAt;


    @Version
    @Column(nullable = false)
    private int version;

    @PrePersist
    void prePersist() {
        this.bookedAt = Instant.now();
        if (this.status == null) this.status = Status.CONFIRMED;
    }

    public enum Status {
        CONFIRMED, CANCELLED
    }
}
