# Problem Statement: Babysitter Booking Platform

---

## Title

**Babysitter Booking Platform** — A concurrent, transaction-safe scheduling system connecting parents with qualified babysitters.

---

## Domain

**Service Marketplace / Childcare Scheduling**

This platform operates in the on-demand home services domain, specifically focused on childcare. It digitizes the process of finding, vetting, and booking babysitters — replacing informal arrangements (word-of-mouth, messaging apps) with a structured, reliable, and auditable booking system.

---

## Real-Life Problem

Parents frequently struggle to find trustworthy, available babysitters on short notice. The current landscape relies heavily on informal networks, text messages, and manual scheduling — all of which suffer from:

- **Double-booking conflicts**: A babysitter may receive simultaneous booking requests from multiple parents, leading to scheduling collisions with no automated conflict resolution.
- **No availability transparency**: Parents cannot see real-time babysitter availability before initiating contact.
- **No transaction integrity**: Payments or deposit confirmations are handled outside the platform, creating disputes and inconsistencies.
- **Unverified babysitters**: There is no structured process for credential review or parent-facing ratings.
- **No audit trail**: Cancelled or modified bookings leave no history, making dispute resolution difficult.

These problems are amplified during peak demand periods (weekends, holidays) when concurrent booking attempts spike and race conditions in availability management become critical.

---

## Proposed Solution

Build a **Java Spring Boot-based web platform** that provides:

- A structured **booking workflow** where parents can search babysitters by availability, location, and ratings, and submit booking requests.
- **Concurrency-safe scheduling** using database-level locking (pessimistic or optimistic locking via JPA) to prevent double-booking when multiple parents simultaneously attempt to book the same babysitter for overlapping time slots.
- **Optimized database transactions** using Spring's `@Transactional` management to ensure atomicity across booking creation, slot reservation, and payment record creation.
- **Role-based access** for three distinct user types: Parents, Babysitters, and Administrators.
- A **booking history and audit log** for all state transitions (pending -> confirmed -> completed / cancelled).

---

## User Types and Roles

### 1. Parent (Client)
The primary consumer of the platform. Searches for and books babysitters.

### 2. Babysitter (Service Provider)
Registers and offers childcare services. Manages their own availability and responds to booking requests.

### 3. Administrator (Platform Manager)
Manages platform integrity. Reviews disputes, manages user accounts, and monitors booking activity.

---

## User Roles and Permissions

| Permission / Action                        | Parent | Babysitter | Admin |
|--------------------------------------------|:------:|:----------:|:-----:|
| Register / Login                           | YES    | YES        | YES   |
| Search babysitters by availability         | YES    | NO         | YES   |
| Create a booking request                   | YES    | NO         | NO    |
| View own booking history                   | YES    | YES        | YES   |
| Accept / Decline booking requests          | NO     | YES        | NO    |
| Manage personal availability slots        | NO     | YES        | NO    |
| Submit a review for a completed booking    | YES    | NO         | NO    |
| View all bookings platform-wide            | NO     | NO         | YES   |
| Deactivate / suspend user accounts         | NO     | NO         | YES   |
| View audit logs of booking state changes   | NO     | NO         | YES   |

---

## Minimum 5 Related Database Entities

The following core entities form the relational data model of the platform:

### 1. `User`
Represents any registered user on the platform. Common fields are stored here; role determines access.

| Field           | Type        | Notes                              |
|-----------------|-------------|------------------------------------|
| user_id         | UUID / Long | Primary Key                        |
| full_name       | VARCHAR     |                                    |
| email           | VARCHAR     | Unique, used for authentication    |
| password_hash   | VARCHAR     | BCrypt hashed                      |
| role            | ENUM        | PARENT, BABYSITTER, ADMIN          |
| is_active       | BOOLEAN     | Admin can deactivate               |
| created_at      | TIMESTAMP   |                                    |

---

### 2. `BabysitterProfile`
Extended profile for users with the BABYSITTER role. Stores service-specific data.

| Field              | Type       | Notes                                  |
|--------------------|------------|----------------------------------------|
| profile_id         | Long       | Primary Key                            |
| user_id            | Long (FK)  | References User                        |
| bio                | TEXT       |                                        |
| hourly_rate        | DECIMAL    |                                        |
| years_experience   | INT        |                                        |
| average_rating     | DECIMAL    | Computed from Review records           |
| is_verified        | BOOLEAN    | Set by Admin                           |

---

### 3. `AvailabilitySlot`
Represents time windows when a babysitter is available for booking. Central to concurrency handling.

| Field          | Type       | Notes                                            |
|----------------|------------|--------------------------------------------------|
| slot_id        | Long       | Primary Key                                      |
| babysitter_id  | Long (FK)  | References User (BABYSITTER role)                |
| start_time     | DATETIME   |                                                  |
| end_time       | DATETIME   |                                                  |
| is_booked      | BOOLEAN    | Locked during concurrent booking attempts        |
| version        | INT        | Used for optimistic locking (@Version in JPA)    |

---

### 4. `Booking`
The core transactional entity. Records a parent's confirmed or pending engagement with a babysitter.

| Field            | Type       | Notes                                                  |
|------------------|------------|--------------------------------------------------------|
| booking_id       | Long       | Primary Key                                            |
| parent_id        | Long (FK)  | References User (PARENT role)                          |
| babysitter_id    | Long (FK)  | References User (BABYSITTER role)                      |
| slot_id          | Long (FK)  | References AvailabilitySlot                            |
| status           | ENUM       | PENDING, CONFIRMED, COMPLETED, CANCELLED               |
| total_amount     | DECIMAL    | Computed at booking creation                           |
| notes            | TEXT       | Parent's instructions                                  |
| created_at       | TIMESTAMP  |                                                        |
| updated_at       | TIMESTAMP  |                                                        |

---

### 5. `Review`
Allows parents to leave feedback after a completed booking. Feeds into babysitter ratings.

| Field        | Type       | Notes                                          |
|--------------|------------|------------------------------------------------|
| review_id    | Long       | Primary Key                                    |
| booking_id   | Long (FK)  | References Booking; one review per booking     |
| parent_id    | Long (FK)  | References User (PARENT role)                  |
| rating       | INT        | 1-5 scale                                      |
| comment      | TEXT       | Optional                                       |
| created_at   | TIMESTAMP  |                                                |

---

### 6. `BookingAuditLog` (Supporting Entity)
Records every state transition a booking goes through. Required for admin oversight and dispute resolution.

| Field          | Type       | Notes                                       |
|----------------|------------|---------------------------------------------|
| log_id         | Long       | Primary Key                                 |
| booking_id     | Long (FK)  | References Booking                          |
| changed_by     | Long (FK)  | References User who triggered the change    |
| old_status     | ENUM       |                                             |
| new_status     | ENUM       |                                             |
| changed_at     | TIMESTAMP  |                                             |
| remarks        | TEXT       | Optional reason for change                  |

---

## CSE/IT Specialization Requirement

> **Chosen Specialization:** Implement booking system with concurrency handling and optimized DB transactions.

### Technical Requirements this Specialization Imposes

#### 1. Concurrency Handling
- **Problem to solve**: When two or more parents simultaneously submit a booking request for the same babysitter slot, the system must guarantee that only one succeeds and the other(s) receive a clear conflict response, with no silent double-booking.
- **Implementation approach**:
  - Use JPA **Optimistic Locking** (`@Version` on `AvailabilitySlot`) as the primary strategy to detect concurrent write conflicts without holding long-lived database locks.
  - Where strict serialization is required, fall back to **Pessimistic Locking** (`LockModeType.PESSIMISTIC_WRITE`) on critical slot reads.
  - Handle `OptimisticLockException` and surface a user-friendly conflict response (HTTP 409 Conflict).

#### 2. Optimized Database Transactions
- **Problem to solve**: A booking creation involves multiple write operations (marking a slot as booked, creating a Booking record, writing an AuditLog entry). All must succeed or all must roll back — no partial commits.
- **Implementation approach**:
  - Use Spring's `@Transactional` annotation with appropriate **isolation levels** (e.g., `REPEATABLE_READ` or `SERIALIZABLE` for slot reservation).
  - Apply **transaction propagation** rules (e.g., `REQUIRES_NEW` for audit logging to persist even on rollback of the parent transaction).
  - Use **batch writes** and **lazy loading** strategies in JPA/Hibernate to minimize round-trips during high-load operations.

---

## Success Criteria

The project will be considered successful if the following are demonstrably met:

1. A parent can search for babysitters filtered by date/time availability and view profiles.
2. A parent can submit a booking request that atomically reserves an availability slot.
3. When two concurrent booking requests target the same slot, only one is accepted and the other receives a conflict error — verified via a concurrency test.
4. A babysitter can accept or decline pending booking requests.
5. A parent can submit a review only after a booking reaches COMPLETED status.
6. An administrator can view all bookings, audit logs, and deactivate user accounts.
7. All multi-step database operations (booking creation, cancellation) are wrapped in transactions with verified rollback on failure.

---

## Out of Scope

The following features are intentionally excluded from this project and are not part of the capstone requirements:

- **Real payment processing** (no Stripe, PayPal, or payment gateway integration)
- **Real-time notifications** (no WebSocket push notifications or SMS/email alerts)
- **Mobile application** (no Android/iOS client)
- **Background job scheduling** (no cron-based tasks, reminders, or automated status transitions)
- **AI/ML features** (no recommendation engine, no smart matching beyond filters)
- **Third-party identity providers** (no OAuth2 / social login)
- **File uploads** (no profile photo or document verification uploads)
- **Multi-language / internationalization support**

---

## Chosen Track

> **Java (Spring Boot)**

| Technology           | Choice                          |
|----------------------|---------------------------------|
| Language             | Java 17+                        |
| Framework            | Spring Boot 3.x                 |
| ORM                  | Spring Data JPA / Hibernate     |
| Database             | MySQL / PostgreSQL               |
| Security             | Spring Security (JWT-based)     |
| Build Tool           | Maven                           |
| API Style            | RESTful (JSON)                  |
| Locking Strategy     | JPA Optimistic + Pessimistic    |
| Transaction Manager  | Spring @Transactional           |

---

*Document version: 1.0 | Date: August 2026*
