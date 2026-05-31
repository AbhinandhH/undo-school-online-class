# Undo School Global Class Offering Booking System

A production-ready backend service for a global live-learning platform where teachers conduct online classes for students across different countries and timezones.

-----
## Project Overview

Teachers create course offerings with multiple sessions. Parents browse and book these offerings. The system handles timezone conversion, booking conflict detection, and concurrent booking requests safely.

-----

### Key Features
- Can add Teachers and Parents/Students
- Teachers create offerings and add sessions in their local timezone
- Parents view session timings in their own local timezone
- Booking conflict detection — prevents overlapping session bookings
- Concurrency-safe booking with pessimistic locking
- Capacity management per offering

---

## Tech Stack

- Language -  Java 17
- Framework - Spring Boot 3.2.0
- Database - MySQL 8.x 
- ORM - Spring Data JPA / Hibernate
- Validation - Jakarta Bean Validation
- Build Tool - Maven
- Testing - JUnit 5, Spring Boot Test, H2 (in-memory)

---

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.x

---
## Environment Setup

### 1. Clone the Repository

bash
git clone <your-repo-url>
cd class-booking

### 2. Create MySQL Database

bash
mysql -u root -P

sql
CREATE DATABASE class_booking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

### 3. Configure Environment Variables

Copy and update 'application.properties':

properties
spring.datasource.url=jdbc:mysql://localhost:3306/class_booking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD


### 4. Run the Application

bash
./mvnw spring-boot:run

App starts at: http://localhost:8080
