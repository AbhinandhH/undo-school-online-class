# Global Class Offering Booking System

A production-ready Spring Boot backend for a live-learning platform where teachers create
class offerings across timezones and parents book them globally.

---

## Quick Start

```bash
# 1. Create MySQL database and run schema
mysql -u root -p < src/main/resources/schema.sql

# 2. Update credentials in application.properties if needed
#    spring.datasource.username / password

# 3. Run
./mvnw spring-boot:run

# 4. Run tests (uses H2 in-memory — no MySQL needed)
./mvnw test
```

---

## Architecture

### Entity Model

```
Course ──< Offering ──< Session
                 └──< Booking >── User (PARENT)
User (TEACHER) ──< Offering
```

| Entity   | Purpose |
|----------|---------|
| `Course`   | Catalog item (Minecraft Coding, Python, etc.) |
| `Offering` | Schedulable section of a course by a specific teacher |
| `Session`  | Individual time slot (UTC); shown locally to viewer |
| `Booking`  | Parent books an entire offering (all sessions) |
| `User`     | Teachers and parents; carries IANA `timezone` field |

### Database Design Decisions

- **All times stored as UTC** (`DATETIME` columns named `*_utc`).  
  No ambiguity across daylight saving transitions.
- **IANA timezone on User** (`America/New_York`, `Asia/Kolkata`).  
  Conversion to/from local time happens in the service layer, not the DB.
- **Unique constraint** `(parent_id, offering_id)` — database-level duplicate prevention.
- **`version` column** on `Booking` — JPA optimistic lock for status update races.
- **InnoDB** throughout — row-level locking for `SELECT ... FOR UPDATE`.

---

## Timezone Handling

```
Teacher submits:  LocalDateTime (their timezone, from User.timezone)
                       │
                  TimezoneConverter.toUtc()
                       │
Storage:          Instant (UTC)  ← always UTC in DB
                       │
                  TimezoneConverter.toLocal() / Instant.atZone()
                       │
Response:         startTimeUtc  (raw UTC — always present)
                  startTimeLocal (ZonedDateTime in viewer's tz)
                  displayTimezone (IANA string)
```

Parent in `Asia/Kolkata` viewing a session the teacher scheduled at
`2025-06-07T18:00 America/New_York`:

| Step | Value |
|------|-------|
| Teacher submits | `2025-06-07T18:00` (assumed EDT = UTC−4) |
| Stored UTC | `2025-06-07T22:00:00Z` |
| Parent sees local | `2025-06-08T03:30+05:30[Asia/Kolkata]` |

---

## Concurrency Strategy (Booking)

Defence-in-depth with 4 layers:

### Layer 1 — Transaction isolation: `REPEATABLE_READ`
Prevents phantom reads within the booking transaction.

### Layer 2 — Pessimistic write lock on parent's existing sessions
```sql
SELECT s.* FROM sessions s
WHERE s.offering_id IN (
    SELECT b.offering_id FROM bookings b
    WHERE b.parent_id = ? AND b.status = 'CONFIRMED'
)
FOR UPDATE
```
Any concurrent booking attempt for the **same parent** blocks here until the
first transaction commits, eliminating the check-then-act race condition.

### Layer 3 — DB unique constraint `(parent_id, offering_id)`
If two concurrent requests for the **same parent + offering** somehow both
pass the duplicate check (e.g. first-ever booking with no rows to lock),
MySQL's unique constraint fires. `GlobalExceptionHandler` returns HTTP 409.

### Layer 4 — `@Version` optimistic lock on Booking
Guards concurrent **status mutations** (cancel vs re-book) on the same row.
`ObjectOptimisticLockingFailureException` → HTTP 409 "please retry".

---

## API Reference

### Teacher APIs

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/teachers/{teacherId}/offerings` | Create offering (DRAFT) |
| `POST` | `/api/teachers/{teacherId}/offerings/{id}/sessions` | Add sessions (teacher local time) |
| `POST` | `/api/teachers/{teacherId}/offerings/{id}/publish` | Publish offering |
| `GET`  | `/api/teachers/{teacherId}/offerings` | List teacher's offerings |
| `GET`  | `/api/teachers/{teacherId}/sessions/upcoming` | Upcoming sessions |

### Parent APIs

| Method | URL | Description |
|--------|-----|-------------|
| `GET`  | `/api/parents/{parentId}/offerings` | Browse available offerings |
| `POST` | `/api/parents/{parentId}/bookings?offeringId={id}` | Book an offering |
| `GET`  | `/api/parents/{parentId}/bookings` | My bookings |
| `DELETE` | `/api/parents/{parentId}/bookings/{bookingId}` | Cancel booking |

---

## Sample Requests

### Create an offering
```json
POST /api/teachers/1/offerings
{
  "courseId": 1,
  "title": "Saturday Batch",
  "description": "8-week Minecraft coding",
  "maxCapacity": 20
}
```

### Add sessions (teacher in America/New_York — submit local time)
```json
POST /api/teachers/1/offerings/1/sessions
{
  "sessions": [
    { "startTime": "2025-06-07T18:00:00", "endTime": "2025-06-07T19:00:00" },
    { "startTime": "2025-06-14T18:00:00", "endTime": "2025-06-14T19:00:00" },
    { "startTime": "2025-06-21T18:00:00", "endTime": "2025-06-21T19:00:00" }
  ]
}
```

### Session response to parent in Asia/Kolkata
```json
{
  "id": 1,
  "startTimeUtc": "2025-06-07T22:00:00Z",
  "endTimeUtc":   "2025-06-07T23:00:00Z",
  "startTimeLocal": "2025-06-08T03:30:00+05:30[Asia/Kolkata]",
  "endTimeLocal":   "2025-06-08T04:30:00+05:30[Asia/Kolkata]",
  "displayTimezone": "Asia/Kolkata"
}
```

---

## Error Responses

All errors follow a consistent envelope:

```json
{
  "timestamp": "2025-06-07T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Session conflict: ..."
}
```

| HTTP | Scenario |
|------|----------|
| 400  | Validation failure, invalid timezone, end ≤ start |
| 403  | Wrong role (teacher acting as parent, etc.) |
| 404  | Entity not found |
| 409  | Booking conflict, duplicate booking, optimistic lock, capacity full |
| 500  | Unexpected error |

---

## What's Out of Scope (Production TODOs)

- **Authentication/Authorization** — JWT, Spring Security
- **Pagination** — offerings and sessions lists
- **Notifications** — email/push on booking confirmation
- **Flyway/Liquibase** — managed DB migrations (schema.sql is DDL only)
- **Observability** — metrics, tracing, structured logging
