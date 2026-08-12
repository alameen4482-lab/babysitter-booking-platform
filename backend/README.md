# Backend — Babysitter Booking Platform

> **Java 17 | Spring Boot 3.4.1 | Maven**

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK         | 17+     |
| Maven       | 3.9+    |
| MySQL       | 8.0+    |

---

## Running Locally

### 1. Create the MySQL Database

```sql
CREATE DATABASE babysitter_booking_db;
```

### 2. Configure Environment Variables (or edit application.properties)

```bash
DB_URL=jdbc:mysql://localhost:3306/babysitter_booking_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your-base64-encoded-256-bit-secret
```

### 3. Run the Application

```bash
cd backend
mvn spring-boot:run
```

### 4. Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## Running Tests (No MySQL Required)

Tests use an H2 in-memory database. No MySQL connection needed.

```bash
cd backend
mvn test
```

---

## Building the JAR

```bash
mvn clean package -DskipTests
java -jar target/babysitter-booking-backend-1.0.0-SNAPSHOT.jar
```

---

## Project Structure

```
src/main/java/com/babysitterbooking/
├── BabysitterBookingApplication.java   # Entry point
├── config/
│   ├── ApplicationConfig.java          # PasswordEncoder, AuthProvider, AuthManager
│   ├── JwtUtil.java                    # JWT generation & validation (jjwt 0.12.x)
│   ├── JwtAuthFilter.java              # JWT request filter
│   ├── SecurityConfig.java             # SecurityFilterChain
│   └── OpenApiConfig.java              # Swagger / OpenAPI 3
├── controller/                         # REST controllers (Phase 3+)
├── service/
│   └── UserDetailsServiceImpl.java     # Placeholder — replaced in Phase 3
├── repository/                         # Spring Data JPA repositories (Phase 3+)
├── model/entity/
│   └── BaseEntity.java                 # Auditing base class
├── dto/
│   └── ApiResponse.java                # Standard JSON response wrapper
└── exception/
    ├── ApiException.java               # Base exception with HTTP status
    ├── ResourceNotFoundException.java  # 404
    ├── ConflictException.java          # 409 (double-booking / optimistic lock)
    └── GlobalExceptionHandler.java     # @RestControllerAdvice
```

---

## API Response Format

All endpoints return responses in this format:

```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": { }
}
```

Error responses:

```json
{
  "success": false,
  "message": "The requested slot is no longer available due to a concurrent booking."
}
```

---

## Specialization: Concurrency Handling

This backend implements the CSE/IT specialization requirement:

| Mechanism | Class/Annotation | Purpose |
|-----------|------------------|---------|
| Optimistic Locking | `@Version` on `AvailabilitySlot` | Detect concurrent slot writes |
| Pessimistic Locking | `LockModeType.PESSIMISTIC_WRITE` | Slot reservation critical path |
| Transaction Atomicity | `@Transactional` | Booking + slot + audit in one unit |
| 409 Conflict | `ConflictException` | Inform clients of lock failures |

---

## Development Phases

| Phase | Status |
|-------|--------|
| Phase 1: Documentation | Done |
| Phase 2: Backend Foundation | **In Progress** |
| Phase 3: User & Auth Module | Pending |
| Phase 4: Babysitter Profile & Slots | Pending |
| Phase 5: Booking & Concurrency | Pending |
| Phase 6: Reviews & Admin | Pending |
| Phase 7: Testing | Pending |
