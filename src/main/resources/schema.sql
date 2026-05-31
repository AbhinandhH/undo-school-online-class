CREATE DATABASE IF NOT EXISTS CLASS_BOOKING_DB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE CLASS_BOOKING_DB;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL,
    role        ENUM('TEACHER','PARENT') NOT NULL,
    timezone    VARCHAR(50)     NOT NULL COMMENT 'IANA timezone e.g. Asia/Kolkata',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS courses (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200)    NOT NULL,
    description TEXT,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS offerings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    course_id       BIGINT          NOT NULL,
    teacher_id      BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT,
    max_capacity    INT             NOT NULL DEFAULT 30,
    status          ENUM('DRAFT','PUBLISHED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_offerings_course  FOREIGN KEY (course_id)  REFERENCES courses(id),
    CONSTRAINT fk_offerings_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
    INDEX idx_offerings_teacher (teacher_id),
    INDEX idx_offerings_status  (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sessions (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    offering_id     BIGINT      NOT NULL,
    teacher_id      BIGINT      NOT NULL,
    start_time_utc  DATETIME    NOT NULL COMMENT 'UTC start time',
    end_time_utc    DATETIME    NOT NULL COMMENT 'UTC end time',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sessions_offering FOREIGN KEY (offering_id) REFERENCES offerings(id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_teacher  FOREIGN KEY (teacher_id)  REFERENCES users(id),
    INDEX idx_sessions_offering (offering_id),
    INDEX idx_sessions_time     (start_time_utc, end_time_utc),
    CONSTRAINT chk_session_times CHECK (end_time_utc > start_time_utc)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    offering_id     BIGINT      NOT NULL,
    parent_id       BIGINT      NOT NULL,
    status          ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booked_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INT         NOT NULL DEFAULT 0 COMMENT 'optimistic lock version',
    PRIMARY KEY (id),
    CONSTRAINT fk_bookings_offering FOREIGN KEY (offering_id) REFERENCES offerings(id),
    CONSTRAINT fk_bookings_parent   FOREIGN KEY (parent_id)   REFERENCES users(id),
    UNIQUE KEY uq_booking_parent_offering (parent_id, offering_id),
    INDEX idx_bookings_parent   (parent_id),
    INDEX idx_bookings_offering (offering_id)
) ENGINE=InnoDB;
