# Entity-Relationship Diagram

> **Status:** Pending — To be created in Phase 2.

This file will contain the ER diagram for the Babysitter Booking Platform, covering all 6 core entities:

- `User`
- `BabysitterProfile`
- `AvailabilitySlot`
- `Booking`
- `Review`
- `BookingAuditLog`

The diagram will illustrate:
- Primary and foreign key relationships
- Cardinality (one-to-one, one-to-many)
- The `@Version` field on `AvailabilitySlot` for optimistic locking
