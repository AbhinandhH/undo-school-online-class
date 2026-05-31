package com.platform.booking.controller;

import com.platform.booking.dto.response.BookingResponse;
import com.platform.booking.dto.response.OfferingResponse;
import com.platform.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*Parent Related APIs*/

@RestController
@RequestMapping("/undo/parents/{parentId}")
public class ParentController {

    private final BookingService bookingService;

    public ParentController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/offerings")
    public ResponseEntity<List<OfferingResponse>> getAvailableOfferings(@PathVariable Long parentId) {
        return ResponseEntity.ok(bookingService.getAvailableOfferings(parentId));
    }
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> bookOffering(@PathVariable Long parentId, @RequestParam Long offeringId) {
        BookingResponse response = bookingService.bookOffering(parentId, offeringId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getBookings(@PathVariable Long parentId) {
        return ResponseEntity.ok(bookingService.getParentBookings(parentId));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long parentId, @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(parentId, bookingId));
    }

}
