CREATE DATABASE IF NOT EXISTS CLASS_BOOKING_DB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE CLASS_BOOKING_DB;

INSERT IGNORE INTO users (name, email, role, timezone) VALUES
    ('Alice Teacher',  'alice@teach.com',  'TEACHER', 'America/New_York'),
    ('Bob Teacher',    'bob@teach.com',    'TEACHER', 'Asia/Kolkata'),
    ('Carol Parent',   'carol@home.com',   'PARENT',  'Europe/London'),
    ('Dave Parent',    'dave@home.com',    'PARENT',  'Asia/Tokyo');

INSERT IGNORE INTO courses (title, description) VALUES
    ('Minecraft Coding',    'Learn coding through Minecraft'),
    ('Python Fundamentals', 'Beginner Python programming'),
    ('Art Drawing',         'Creative drawing for kids'),
    ('Public Speaking',     'Build confidence through speech');
