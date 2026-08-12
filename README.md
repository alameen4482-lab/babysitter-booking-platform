# Babysitter Booking Platform

> **Capstone Project** | Java Track | CSE/IT Specialization: Concurrency Handling & Optimized DB Transactions

---

## Project Overview

The **Babysitter Booking Platform** is a backend-focused web application that connects **parents** seeking childcare with **babysitters** offering their services. The platform is built on **Java Spring Boot** and specifically addresses the challenge of safe concurrent booking — ensuring no two parents can double-book the same babysitter slot, even under simultaneous requests.

This is a Capstone project demonstrating mastery of:
- Transactional integrity using Spring `@Transactional`
- Concurrency control using JPA Optimistic & Pessimistic Locking
- Secure role-based REST API design with Spring Security

---

## Tech Stack

| Layer            | Technology                          |
|------------------|-------------------------------------|
| Language         | Java 17+                            |
| Framework        | Spring Boot 3.x                     |
| ORM              | Spring Data JPA / Hibernate         |
| Database         | MySQL (or PostgreSQL)               |
| Security         | Spring Security + JWT               |
| Build Tool       | Maven                               |
| API Style        | RESTful JSON                        |
| Locking          | JPA Optimistic + Pessimistic Locks  |
| Testing          | JUnit 5 + Spring Boot Test          |

---

## CSE/IT Specialization

> **"Implement booking system with concurrency handling and optimized DB transactions."**

This specialization is the core engineering challenge of this project. It requires:

- **Optimistic Locking** via JPA `@Version` on `AvailabilitySlot` to detect concurrent update conflicts
- **Pessimistic Locking** (`PESSIMISTIC_WRITE`) for critical slot-reservation paths
- **Spring `@Transactional`** with proper isolation levels and propagation rules
- **Atomic multi-step operations**: booking creation + slot reservation + audit log in a single transaction
- Graceful handling of `OptimisticLockException` returning HTTP 409 Conflict

---

## User Roles

| Role        | Description                                              |
|-------------|----------------------------------------------------------|
| PARENT      | Searches and books babysitters                           |
| BABYSITTER  | Manages availability and responds to booking requests    |
| ADMIN       | Manages platform users, views all bookings and audit logs|

---

## Core Entities

| Entity               | Purpose                                             |
|----------------------|-----------------------------------------------------|
| `User`               | All platform users with role-based differentiation  |
| `BabysitterProfile`  | Extended data for babysitters (rate, bio, rating)   |
| `AvailabilitySlot`   | Time windows offered by babysitters                 |
| `Booking`            | Core transactional booking record                   |
| `Review`             | Post-booking parent feedback                        |
| `BookingAuditLog`    | Immutable record of all booking status changes      |

---

## Project Structure (Planned)

```
babysitter-booking-platform/
|
|-- Problem_Statement.md          # Project scope and requirements
|-- README.md                     # This file
|
|-- docs/
|   |-- diagrams/                 # ER diagrams, architecture, sequence diagrams
|
|-- backend/                      # Spring Boot application (to be created)
|   |-- src/
|       |-- main/
|           |-- java/
|           |-- resources/
|
|-- frontend/                     # (If applicable, minimal UI)
```

---

## Development Phases

| Phase | Description                                         | Status     |
|-------|-----------------------------------------------------|------------|
| 1     | Project documentation and folder structure          | In Progress|
| 2     | Database schema design and ER diagram               | Pending    |
| 3     | Spring Boot project setup and entity classes        | Pending    |
| 4     | Repository, Service, and Controller layers          | Pending    |
| 5     | Concurrency handling implementation                 | Pending    |
| 6     | Security (JWT) and role-based access                | Pending    |
| 7     | Testing (unit + concurrency simulation)             | Pending    |
| 8     | Final documentation and demo                        | Pending    |

---

## Key Design Decisions

- **No payment gateway**: Financial transactions are out of scope.
- **No real-time notifications**: Polling-based status check only.
- **JWT authentication**: Stateless, role-encoded tokens.
- **Audit log with REQUIRES_NEW propagation**: Audit entries persist even when the parent transaction rolls back.

---

## Documentation

| Document                                  | Location                          |
|-------------------------------------------|-----------------------------------|
| Problem Statement                         | `Problem_Statement.md`            |
| ER Diagram                                | `docs/diagrams/er_diagram.md`     |
| Architecture Diagram                      | `docs/diagrams/architecture.md`   |
| Sequence Diagrams                         | `docs/diagrams/sequence/`         |

---

## Authors

> Capstone Team — August 2026
