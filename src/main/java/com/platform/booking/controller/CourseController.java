package com.platform.booking.controller;

import com.platform.booking.dto.request.CourseRequest;
import com.platform.booking.dto.response.CourseResponse;
import com.platform.booking.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/undo/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest courseRequest) {
        CourseResponse courseResponse = courseService.createCourse(courseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(courseResponse);
    }

    @GetMapping("/getcourse/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId) {
        CourseResponse courseResponse = courseService.getCourseById(courseId);
        return ResponseEntity.ok(courseResponse);
    }

    @GetMapping("/allcourses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> courseList = courseService.getAllCourses();
        return ResponseEntity.ok(courseList);
    }

}
