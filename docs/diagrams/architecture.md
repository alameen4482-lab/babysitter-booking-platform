# Architecture Diagram

> **Status:** Implemented

The platform follows a layered Spring Boot REST architecture:

```text
[ Client / API Consumer ]
          |
          v
[ Spring Security Filter (JWT) ]
          |
          v
[ REST Controllers ]
          |
          v
[ Service Layer ]
  |       |       |
  |       |       +--> Transaction / Concurrency control
  |       |
  |       +----------> Business rules / authorization
  |
  v
[ Repository Layer (Spring Data JPA) ]
          |
          v
[ MySQL Database ]
```

## Layers

- **Security:** Stateless JWT authentication and role-based authorization.
- **Controller:** REST endpoints, request validation, Swagger/OpenAPI metadata.
- **Service:** Business rules, transaction boundaries, booking concurrency control and review calculations.
- **Repository:** Spring Data JPA queries and pessimistic-locking access for availability slots.
- **Entity:** Relational domain model for users, babysitters, availability, bookings, reviews and audit logs.
- **Exception handling:** Centralized API error responses through `GlobalExceptionHandler`.

## Concurrency

The booking path uses a database row lock (`PESSIMISTIC_WRITE`) before checking and reserving an availability slot. `AvailabilitySlot` also contains a JPA `@Version` field for optimistic-lock detection.

## Audit Logging

Booking status changes are recorded through a separate transaction using `REQUIRES_NEW`, so the audit record can survive rollback of the surrounding booking transaction.

## API Security

Public endpoints are limited to registration, login and Swagger/OpenAPI documentation. Business endpoints require JWT authentication, with method-level `@PreAuthorize` restrictions for PARENT, BABYSITTER and ADMIN operations.
