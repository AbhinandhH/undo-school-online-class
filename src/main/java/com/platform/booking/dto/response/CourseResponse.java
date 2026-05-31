package com.platform.booking.dto.response;

import com.platform.booking.entity.Course;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CourseResponse {

    private Long id;

    private String title;

    private String description;

    private Instant createdAt;

    public static CourseResponse buildCourseResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
