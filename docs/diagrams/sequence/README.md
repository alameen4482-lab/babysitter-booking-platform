# Sequence Diagrams

> **Status:** Pending — To be created in Phase 2.

This directory will contain sequence diagrams for the key flows of the Babysitter Booking Platform.

## Planned Sequence Diagrams

### 1. Concurrent Booking Request Flow
Shows what happens when two parents simultaneously attempt to book the same slot:
- Parent A and Parent B send requests at the same time
- JPA optimistic lock check
- One succeeds with HTTP 200, the other gets HTTP 409 Conflict

### 2. Normal Booking Creation Flow
Shows the happy-path booking:
- Parent searches available slots
- Selects a slot and submits a booking request
- Slot is marked as booked atomically
- Audit log entry is created

### 3. Babysitter Accepts/Declines Flow
Shows how a babysitter responds to a PENDING booking:
- Babysitter views pending requests
- Accepts or declines
- Booking status transitions with audit log
