package com.platform.booking.service;

import com.platform.booking.dto.request.CourseRequest;
import com.platform.booking.dto.response.CourseResponse;
import com.platform.booking.entity.Course;
import com.platform.booking.exception.ResourceNotFoundException;
import com.platform.booking.repo.CourseRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepo courseRepo;

    public CourseService(CourseRepo courseRepo) {
        this.courseRepo = courseRepo;
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest courseRequest) {
        Course course = Course.builder()
                .createdAt(courseRequest.getCreatedAt())
                .description(courseRequest.getDescription())
                .title(courseRequest.getTitle())
                .build();
        return CourseResponse.buildCourseResponse(courseRepo.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        List<Course> courseList = courseRepo.findAll();
        return courseList.stream().map(c -> CourseResponse.builder()
                .id(c.getId())
                .createdAt(c.getCreatedAt())
                .title(c.getTitle())
                .description(c.getDescription()).build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Course", id));
        return CourseResponse.builder()
                .title(course.getTitle())
                .createdAt(course.getCreatedAt())
                .id(course.getId())
                .description(course.getDescription())
                .build();
    }
}
