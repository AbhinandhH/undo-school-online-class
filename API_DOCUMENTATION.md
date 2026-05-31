## API Documentation

### User APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/undo/user` | Create a teacher or parent |
| GET | `/undo/user/allusers` | get all users |
| GET | `/undo/user/{userId}` | Get user by ID |

###Course APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/undo/course` | Create a Course |
| GET | `/undo/course/allcourses` | get all courses |
| GET | `/undo/course/getcourse/{courseId}` | Get course by ID |

### Teacher APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/undo/teachers/{teacherId}/offerings` | Create offering |
| POST | `/undo/teachers/{teacherId}/offerings/{offeringId}/sessions` | Add sessions |
| POST | `/undo/teachers/{teacherId}/offerings/{offeringId}/publish` | Publish offering |
| GET | `/undo/teachers/{teacherId}/offerings` | List teacher's offerings |
| GET | `/undo/teachers/{teacherId}/sessions/upcoming` | Upcoming sessions |

### Parent APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/undo/parents/{parentId}/offerings` | Browse available offerings |
| POST | `/undo/parents/{parentId}/bookings?offeringId={offeringId}` | Book an offering |
| GET | `/undo/parents/{parentId}/bookings` | My bookings |
| DELETE | `/undo/parents/{parentId}/bookings/{bookingId}` | Cancel booking |



---

## Timezone Handling

All session times are stored in UTC in the database. Timezone conversion happens in the service layer:

- **Teacher submits:** `LocalDateTime` in their own timezone (from their profile)
- **Stored:** `Instant` (UTC)
- **Response to viewer:** Both `startTimeUtc` (raw UTC) and `startTimeLocal` (in viewer's IANA timezone)

Example:
```
Teacher (America/New_York) submits: 2025-06-07T18:00:00
Stored in DB:                       2025-06-07T22:00:00Z  (UTC)
Parent (Asia/Kolkata) sees:         2025-06-08T03:30:00+05:30
```

---

## Concurrency Handling

Booking uses defence-in-depth with 4 layers:

| Layer | Mechanism | Protects Against |
|-------|-----------|-----------------|
| 1 | `REPEATABLE_READ` transaction isolation | Phantom reads |
| 2 | `SELECT ... FOR UPDATE` on parent's existing sessions | TOCTOU race in overlap check |
| 3 | DB unique constraint `(parent_id, offering_id)` | Duplicate booking slip-through |
| 4 | `@Version` optimistic lock on Booking entity | Concurrent status update collision |

---

## Assumptions

- Authentication/authorization is out of scope. User identity is passed as a path variable (`teacherId`, `parentId`).
- A parent books an **entire offering** — not individual sessions.
- Session times submitted by teachers are in their profile timezone (no timezone in request body).
- An offering must be **PUBLISHED** before parents can book it.
- An offering must have at least one session before it can be published.

---

## Steps to Run Locally

```bash
# 1. Start MySQL
brew services start mysql        # macOS
sudo systemctl start mysql       # Linux

# 2. Create database
mysql -u root -p -e "CREATE DATABASE class_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. Update credentials in src/main/resources/application.properties

# 4. Run
./mvnw spring-boot:run

# 5. Test APIs at http://localhost:8080
```
