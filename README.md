# Babysitter Booking Platform

> **Capstone Project** | Java Track | CSE/IT Specialization: Concurrency Handling & Optimized DB Transactions

## Project Overview

The **Babysitter Booking Platform** is a Spring Boot REST backend that connects parents with babysitters while protecting availability and booking operations from concurrent double-booking.

### Implemented capabilities

- JWT-based authentication and BCrypt password hashing
- Role-based access for **PARENT**, **BABYSITTER**, and **ADMIN**
- Babysitter profiles and availability slots
- Booking workflow: pending, confirmed, declined, completed, cancelled
- Pessimistic locking on the critical slot-reservation path
- Optimistic locking with JPA `@Version`
- Transactional booking and cancellation operations
- Booking audit logs, including `REQUIRES_NEW` status-change logging
- Reviews for completed bookings, one review per booking
- Babysitter average-rating calculation
- Centralized API error handling and validation
- Swagger/OpenAPI documentation
- JUnit/Spring Boot tests with an H2 in-memory database
- Maven build with Java 17

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8+ |
| Security | Spring Security + JWT |
| Build Tool | Maven 3.9+ |
| API | REST / JSON |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Testing | JUnit 5 + Spring Boot Test + H2 |

## Local Configuration

**Do not commit database passwords or JWT signing secrets.** Runtime configuration is read from environment variables.

Required variables:

```text
DB_URL=jdbc:mysql://localhost:3306/babysitter_booking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=<your-local-mysql-password>
JWT_SECRET=<long-random-secret-at-least-32-bytes>
```

Optional:

```text
JWT_EXPIRATION_MS=86400000
```

On Windows PowerShell, for the current terminal session:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/babysitter_booking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-local-mysql-password"
$env:JWT_SECRET="your-long-random-secret"
$env:JWT_EXPIRATION_MS="86400000"
```

Never commit these values to Git.

## Run the Application

```powershell
cd backend
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Run Tests

Tests use H2 and do not require MySQL:

```powershell
cd backend
mvn clean test
```

## Build the JAR

```powershell
cd backend
mvn clean package -DskipTests
java -jar target/babysitter-booking-backend-1.0.0-SNAPSHOT.jar
```

## Core Roles

| Role | Main responsibilities |
|---|---|
| PARENT | Search babysitters, create/cancel bookings, submit reviews |
| BABYSITTER | Manage availability, confirm/decline bookings, complete bookings |
| ADMIN | View all bookings/audit logs and activate/deactivate users |

## Concurrency and Transaction Design

The booking critical path uses:

1. **Pessimistic locking** (`PESSIMISTIC_WRITE`) to serialize competing reservations for the same availability slot.
2. **Optimistic locking** (`@Version`) on `AvailabilitySlot` to detect concurrent writes.
3. **`@Transactional`** so slot reservation and booking creation are atomic.
4. **Audit logging** with `REQUIRES_NEW` for booking status changes.

When a slot is already booked or a concurrency conflict occurs, the API returns a conflict response instead of silently creating a duplicate booking.

## Project Structure

```text
babysitter-booking-platform/
├── Problem_Statement.md
├── README.md
├── .gitignore
├── docs/
│   └── diagrams/
└── backend/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/babysitterbooking/
        │   │   ├── config/
        │   │   ├── controller/
        │   │   ├── dto/
        │   │   ├── exception/
        │   │   ├── model/entity/
        │   │   ├── repository/
        │   │   └── service/
        │   └── resources/
        │       └── application.properties
        └── test/
            ├── java/com/babysitterbooking/
            └── resources/application.properties
```

## Repository Hygiene

Maven build output under `backend/target/` is ignored by Git. Compiled `.class` files and generated build artifacts are not source-controlled.

## Project Status

| Area | Status |
|---|---|
| Project documentation | Done |
| Backend foundation | Done |
| User & JWT authentication | Done |
| Babysitter profiles & availability | Done |
| Booking & concurrency | Done |
| Reviews & admin | Done |
| Automated tests | Implemented and passing locally |
| Swagger/OpenAPI | Done |
| Final local end-to-end verification | Remaining |

## Out of Scope

- Real payment gateway integration
- Real-time WebSocket/SMS/email notifications
- Mobile application
- Background job scheduling
- AI/ML recommendation features
- Third-party OAuth/social login
- File uploads
- Multi-language support

## Author

> Capstone Team — 2026
