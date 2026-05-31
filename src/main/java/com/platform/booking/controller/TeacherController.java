package com.platform.booking.controller;

import com.platform.booking.dto.request.AddSessionsRequest;
import com.platform.booking.dto.request.CreateOfferingRequest;
import com.platform.booking.dto.response.OfferingResponse;
import com.platform.booking.dto.response.SessionResponse;
import com.platform.booking.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/undo/teachers/{teacherId}")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/offerings")
    public ResponseEntity<OfferingResponse> createOffering(@PathVariable Long teacherId, @Valid @RequestBody CreateOfferingRequest request) {
        OfferingResponse response = teacherService.createOffering(teacherId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/offerings/{offeringId}/sessions")
    public ResponseEntity<OfferingResponse> addSessions(@PathVariable Long teacherId, @PathVariable Long offeringId, @Valid @RequestBody AddSessionsRequest request) {
        OfferingResponse response = teacherService.addSessions(teacherId, offeringId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offerings/{offeringId}/publish")
    public ResponseEntity<OfferingResponse> publishOffering(@PathVariable Long teacherId, @PathVariable Long offeringId) {
        OfferingResponse response = teacherService.publishOffering(teacherId, offeringId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/offerings")
    public ResponseEntity<List<OfferingResponse>> getOfferings(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getTeacherOfferings(teacherId));
    }

    @GetMapping("/sessions/upcoming")
    public ResponseEntity<List<SessionResponse>> getUpcomingSessions(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getUpcomingSessions(teacherId));
    }






}
