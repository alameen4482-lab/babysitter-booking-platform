# Architecture Diagram

> **Status:** Pending — To be created in Phase 2.

This file will contain the high-level system architecture diagram for the Babysitter Booking Platform.

## Planned Architecture Layers

```
[ Client / API Consumer ]
          |
          v
[ Spring Security Filter (JWT) ]
          |
          v
[ REST Controllers ]
   |               |
   v               v
[ Service Layer ]  [ Exception Handler ]
   |
   v
[ Repository Layer (Spring Data JPA) ]
   |
   v
[ Database (MySQL / PostgreSQL) ]
```

## Key Architectural Notes

- **Concurrency layer**: Lives in the Service layer where `@Transactional` and JPA lock modes are applied.
- **Audit logging**: Uses `REQUIRES_NEW` propagation so it survives transaction rollbacks.
- **JWT filter**: Stateless authentication; role extracted from token claims.
