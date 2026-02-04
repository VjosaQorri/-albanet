# AlbaNet - Project Documentation

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Modules and Their Purpose](#3-modules-and-their-purpose)
4. [Database Schema](#4-database-schema)
5. [Authentication & Authorization](#5-authentication--authorization)
6. [Key Business Logic](#6-key-business-logic)
7. [Real-Time Features (SSE vs WebSocket)](#7-real-time-features-sse-vs-websocket)
8. [Request Lifecycle](#8-request-lifecycle)
9. [Code Cleanup Summary](#9-code-cleanup-summary)
10. [Configuration](#10-configuration)
11. [Known Limitations & Future Improvements](#11-known-limitations--future-improvements)

---

## 1. Project Overview

**AlbaNet** is a telecommunications customer portal built with Spring Boot. It serves as a self-service platform for customers of a fictional Albanian telecom company.

### What It Does:
- **For Customers**: Browse and subscribe to TV, Mobile, and Internet plans; manage subscriptions; create support tickets
- **For Staff**: View and claim tickets assigned to their team; update ticket status; resolve issues
- **For Admins**: Oversee all tickets; reassign/escalate tickets; create new staff accounts; manage the system

### Technology Stack:
| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 4.0.2, Java 21 |
| Database | PostgreSQL |
| Security | Spring Security (form-based login) |
| Frontend | Thymeleaf (server-side rendering) |
| Real-time | Server-Sent Events (SSE) |
| Build Tool | Maven |

---

## 2. Architecture

The project follows a **modular layered architecture** organized by business domain:

```
com.example.albanet/
├── AlbaNetApplication.java          # Entry point
├── auth/                            # Authentication (login/register)
├── catalog/                         # Product catalog enums
├── client/                          # Client-facing controllers
├── config/                          # Security, data initialization
├── exception/                       # Global exception handling
├── staff/                           # Staff management
├── subscription/                    # Subscription plans & user subscriptions
├── ticket/                          # Support ticket system
├── user/                            # Customer accounts
└── util/                            # Utilities (if any)
```

### Layer Structure (per module):
```
module/
├── api/                 # Public contracts (DTOs for external use)
│   └── dto/            # Data Transfer Objects
├── controller/          # HTTP endpoints (MVC controllers)
├── internal/            # Internal implementation
│   ├── Entity.java     # JPA entity
│   ├── Repository.java # Spring Data JPA repository
│   ├── Service.java    # Business logic
│   ├── Mapper.java     # Entity ↔ DTO conversion
│   └── enums/          # Domain enums
└── security/            # Module-specific security (if any)
```

---

## 3. Modules and Their Purpose

### 3.1 User Module (`user/`)
**Purpose**: Manages customer accounts

| Component | File | Description |
|-----------|------|-------------|
| Entity | `UserEntity.java` | Customer data (name, email, address, phone) |
| Repository | `UserRepository.java` | Database operations |
| Service | `UserService.java` | Business logic for user operations |
| Security | `CustomUserDetails.java` | Spring Security adapter |
| Security | `CustomUserDetailsService.java` | Loads user for authentication |

### 3.2 Staff Module (`staff/`)
**Purpose**: Manages employee accounts with role-based access

| Component | File | Description |
|-----------|------|-------------|
| Entity | `StaffEntity.java` | Employee data with role (ADMIN, IT1, IT2, FINANCE, SUPPORT) |
| Repository | `StaffRepository.java` | Database operations |
| Service | `StaffService.java` / `StaffServiceImpl.java` | Staff CRUD operations |
| Security | `StaffUserDetails.java` | Spring Security adapter for staff |
| Controllers | `AdminDashboardController.java` | Admin ticket management |
| Controllers | `StaffDashboardController.java` | Staff ticket handling |

### 3.3 Subscription Module (`subscription/`)
**Purpose**: Handles subscription plans and user subscriptions

| Component | File | Description |
|-----------|------|-------------|
| Entity | `SubscriptionPlanEntity.java` | Plan definitions (TV_BASIC, WIFI_PREMIUM, etc.) |
| Entity | `UserSubscriptionEntity.java` | User's active/pending subscriptions |
| Repository | `SubscriptionPlanRepository.java` | Plan queries |
| Repository | `UserSubscriptionRepository.java` | User subscription queries |
| Service | `SubscriptionPlanService.java` | Subscribe, upgrade, cancel logic |
| Utility | `PlanHierarchy.java` | Defines upgrade/downgrade rules |

### 3.4 Ticket Module (`ticket/`)
**Purpose**: Customer support ticketing system

| Component | File | Description |
|-----------|------|-------------|
| Entity | `TicketEntity.java` | Ticket data (title, status, priority, team) |
| Repository | `TicketRepository.java` | Complex ticket queries |
| Service | `ClientTicketService.java` | Ticket creation by customers |
| Service | `AdminTicketService.java` | Admin ticket management |
| Service | `StaffTicketService.java` | Staff ticket handling |
| SSE | `TicketSseController.java` | Real-time notifications |
| Mapper | `TicketMapper.java` | Entity ↔ DTO conversion |
| Enums | `TicketProblemType.java` | Auto-routing rules |

### 3.5 Auth Module (`auth/`)
**Purpose**: User registration and login

| Component | File | Description |
|-----------|------|-------------|
| Controller | `AuthViewController.java` | Login/register pages |
| Service | `AuthService.java` | Registration logic |
| DTO | `RegisterRequest.java` | Registration form data |
| DTO | `LoginRequest.java` | Login form data |

### 3.6 Catalog Module (`catalog/`)
**Purpose**: Product type definitions (enums only)

| Component | File | Description |
|-----------|------|-------------|
| Enum | `CatalogCode.java` | Plan codes (TV_BASIC, PAKO_S, WIFI_PREMIUM) |
| Enum | `ProductType.java` | Product categories (TV, MOBILE, INTERNET) |
| Controller | `CatalogViewController.java` | Serves the catalog page |

### 3.7 Config Module (`config/`)
**Purpose**: Application configuration

| Component | File | Description |
|-----------|------|-------------|
| Security | `SecurityConfig.java` | Two security chains (user & staff) |
| Security | `PasswordEncoderConfig.java` | BCrypt password encoder |
| Security | `StaffLoginSuccessHandler.java` | Redirects after staff login |
| Init | `DataInitializer.java` | Seeds demo user on startup |
| Init | `InitialAdminConfig.java` | Admin setup configuration |
| Init | `SecureAdminInitializer.java` | Secure admin creation |

---

## 4. Database Schema

### Entity Relationship Diagram

```
┌─────────────────────┐
│       users         │
├─────────────────────┤
│ id (PK)             │
│ first_name          │
│ last_name           │
│ email (UNIQUE)      │◄──────────────────┐
│ password            │                   │
│ phone_number        │                   │
│ street              │                   │
│ city                │                   │
│ postal_code         │                   │
│ country             │                   │
│ active              │                   │
│ created_at          │                   │
└─────────────────────┘                   │
          │                               │
          │ user_id                       │ customer_id
          ▼                               │
┌─────────────────────┐     ┌─────────────────────┐
│  user_subscriptions │     │    ticket_entity    │
├─────────────────────┤     ├─────────────────────┤
│ id (PK)             │     │ id (PK)             │
│ user_id (FK)        │     │ title               │
│ plan_id (FK)        │     │ description         │
│ plan_code           │     │ category            │
│ plan_name           │     │ problem_type        │
│ category            │     │ status              │
│ duration_months     │     │ priority            │
│ total_price         │     │ assigned_team       │
│ start_date          │     │ assigned_to ────────┼──► staff.email
│ end_date            │     │ customer_id (FK) ───┘
│ status              │     │ created_at/by       │
│ created_at          │     │ updated_at/by       │
│ cancelled_at        │     │ closed_at           │
└─────────────────────┘     │ resolution_summary  │
          │                 └─────────────────────┘
          │ plan_id
          ▼
┌─────────────────────┐     ┌─────────────────────┐
│  subscription_plans │     │        staff        │
├─────────────────────┤     ├─────────────────────┤
│ id (PK)             │     │ id (PK)             │
│ code (UNIQUE)       │     │ first_name          │
│ name                │     │ last_name           │
│ description         │     │ email (UNIQUE)      │
│ category            │     │ password            │
│ monthly_price       │     │ phone_number        │
│ duration_days       │     │ employee_number     │
│ features            │     │ role                │
│ active              │     │ hired_at            │
└─────────────────────┘     │ active              │
                            │ last_login_at       │
                            └─────────────────────┘
```

### Key Relationships:
- `user_subscriptions.user_id` → `users.id` (Many-to-One)
- `user_subscriptions.plan_id` → `subscription_plans.id` (Many-to-One)
- `ticket_entity.customer_id` → `users.id` (Many-to-One)
- `ticket_entity.assigned_to` → `staff.email` (String reference, not FK)

---

## 5. Authentication & Authorization

### Dual Security Configuration

The application uses **two separate security filter chains**:

```java
@Order(1) staffSecurityFilterChain  → Handles /staff/**
@Order(2) userSecurityFilterChain   → Handles everything else
```

### User (Customer) Authentication Flow:
```
1. User submits email/password to POST /login
2. Spring Security invokes CustomUserDetailsService
3. CustomUserDetailsService calls UserRepository.findByEmail()
4. CustomUserDetails wraps the UserEntity with ROLE_USER
5. DaoAuthenticationProvider validates password (BCrypt)
6. Success → Redirect to /home
```

### Staff Authentication Flow:
```
1. Staff submits email/password to POST /staff/login
2. Spring Security invokes StaffUserDetailsService
3. StaffUserDetailsService calls StaffService.getActiveStaffByEmail()
4. StaffUserDetails wraps StaffEntity with ROLE_<role>
5. DaoAuthenticationProvider validates password
6. StaffLoginSuccessHandler redirects based on role
```

### Role-Based Access Control:

| Role | Access |
|------|--------|
| `ROLE_USER` | `/home`, `/tv`, `/mobile`, `/internet`, `/profile`, `/my-tickets`, `/my-subscriptions`, `/api/**` |
| `ROLE_ADMIN` | `/staff/dashboard`, `/staff/create-staff`, all staff endpoints |
| `ROLE_IT1` | `/staff/my-dashboard`, `/staff/my-tickets/**`, `/staff/tickets/*/claim` |
| `ROLE_IT2` | Same as IT1 |
| `ROLE_FINANCE` | Same as IT1 |
| `ROLE_SUPPORT` | Same as IT1 (can handle any team's tickets) |

---

## 6. Key Business Logic

### 6.1 Automatic Ticket Routing

When a customer creates a ticket, the system automatically assigns:
- **Team** based on problem type
- **Priority** based on urgency

```java
// TicketProblemType.java
TV_NO_SIGNAL("No signal", TicketCategory.TV, "IT1", TicketPriority.HIGH)
INTERNET_INVOICE_COPY("Copy of Invoice", TicketCategory.INTERNET, "FINANCE", TicketPriority.LOW)
```

**Flow:**
```
Customer selects category (TV/Mobile/Internet)
    ↓
Customer selects problem type
    ↓
System extracts team and priority from enum
    ↓
Ticket created with automatic assignment
    ↓
SSE notification sent to team dashboard
```

### 6.2 Subscription Hierarchy

TV and Internet plans have a hierarchy (Basic < Standard < Premium):

```java
// PlanHierarchy.java
TV_HIERARCHY: BASIC(1) < STANDARD(2) < PREMIUM(3)
INTERNET_HIERARCHY: BASIC(1) < STANDARD(2) < PREMIUM(3)
```

**Subscription Logic:**
1. **Same plan** → Extend duration
2. **Different plan (hierarchical)** → Create PENDING subscription
3. **Mobile plans** → No hierarchy, direct extension
4. **Cancel** → Mark as CANCELLED

### 6.3 Ticket Lifecycle

```
OPEN → IN_PROGRESS → DONE
  ↑         │
  └─────────┘ (can be reopened)
```

**States:**
- `OPEN`: New ticket, unassigned
- `IN_PROGRESS`: Claimed by staff member
- `DONE`: Resolved with resolution summary

---

## 7. Real-Time Features (SSE vs WebSocket)

### Why Server-Sent Events (SSE) Instead of WebSocket?

| Feature | SSE | WebSocket |
|---------|-----|-----------|
| Direction | Server → Client (one-way) | Bidirectional |
| Protocol | HTTP | WebSocket (upgrade) |
| Reconnection | Built-in | Manual |
| Complexity | Simple | More complex |
| Use Case | Notifications, feeds | Chat, gaming |

### Our Use Case:
We only need to **push notifications from server to staff dashboards** when new tickets arrive. This is a **one-way flow**, making SSE the simpler and more appropriate choice.

### Implementation:

```java
// TicketSseController.java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamTicketUpdates(@RequestParam String team) {
    // Creates persistent connection for real-time updates
}

public void notifyNewTicket(String team) {
    // Called when ClientTicketService creates a ticket
    sendToTeam(team, "new-ticket", message);
    sendToTeam("ALL", "new-ticket", message);  // Admin sees all
}
```

### When WebSocket Would Be Needed:
If we implemented **live chat** between customers and support staff, we would need WebSocket for bidirectional real-time communication.

---

## 8. Request Lifecycle

### Example: Customer Creates a Ticket

```
1. Customer visits /help
   └── ClientTicketController.showHelpPage()
       └── Returns "client/help" template

2. Customer selects category "TV"
   └── AJAX GET /api/problem-types?category=TV
       └── ClientTicketController.getProblemTypes()
           └── ClientTicketService.getProblemTypesByCategory()
               └── Returns TV problem types from enum

3. Customer submits ticket form
   └── AJAX POST /api/create-ticket
       └── ClientTicketController.createTicket()
           └── ClientTicketService.createTicket()
               ├── Validates user exists
               ├── Parses category and problem type
               ├── Auto-assigns team and priority
               ├── Saves TicketEntity
               └── TicketSseController.notifyNewTicket()
                   └── Pushes SSE event to staff dashboards

4. Staff dashboard receives real-time notification
   └── JavaScript EventSource listener
       └── Refreshes ticket list
```

---

## 9. Code Cleanup Summary

The following unused/placeholder code was removed to keep the codebase clean:

### Removed Files:

| File/Folder | Reason for Removal |
|-------------|-------------------|
| `chat/` (entire module) | Empty placeholder - never implemented |
| `TicketService.java` | Unused interface |
| `TicketServiceImpl.java` | All methods threw `UnsupportedOperationException` |
| `CreateTicketRequest.java` | Replaced by `CreateClientTicketRequest.java` |
| `SubscriptionService.java` | Replaced by `SubscriptionPlanService.java` |
| `SubscriptionMapper.java` | Only used by removed SubscriptionService |
| `SubscriptionRepository.java` | Only used by removed SubscriptionService |
| `SubscriptionEntity.java` | Replaced by `UserSubscriptionEntity.java` |
| `SubscriptionApi.java` | Empty marker interface |
| `SubscriptionDto.java` | Only used by removed SubscriptionMapper |
| `CatalogService.java` | Never called from any controller |
| `CatalogMapper.java` | Only used by removed CatalogService |
| `CatalogRepository.java` | Only used by removed CatalogService |
| `CatalogEntity.java` | Replaced by `SubscriptionPlanEntity.java` |
| `CatalogDto.java` | Only used by removed CatalogMapper |
| `WebSocketConfig.java` | Empty placeholder (SSE used instead) |
| `remove-fragments.ps1` | Script that did nothing |

### Why Fragments Weren't Used:
Thymeleaf fragments were initially attempted for reusable navigation components but caused multiple errors during development. The navigation was implemented directly in each template instead, which works correctly.

### Lines of Code Removed:
Approximately **400+ lines** of dead code were removed, making the codebase cleaner and easier to maintain.

---

## 10. Configuration

### application.properties

```properties
spring.application.name=AlbaNet
server.port=7777

# PostgreSQL (with environment variable overrides)
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/albanet_db}
spring.datasource.username=${DB_USERNAME:albanet}
spring.datasource.password=${DB_PASSWORD:albanet2024}
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=${JPA_HBM2DDL:update}
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
```

### Environment Variables for Deployment:
| Variable | Purpose | Default |
|----------|---------|---------|
| `DB_URL` | Database connection URL | `jdbc:postgresql://localhost:5432/albanet_db` |
| `DB_USERNAME` | Database username | `albanet` |
| `DB_PASSWORD` | Database password | `albanet2024` |
| `JPA_HBM2DDL` | Schema generation mode | `update` |

---

## 11. Known Limitations & Future Improvements

### Current Limitations:

| Area | Limitation |
|------|------------|
| Payment | No actual payment processing |
| Email | No email notifications |
| Testing | Minimal test coverage |
| Chat | Not implemented (placeholder removed) |
| i18n | No localization support |
| Mobile App | No REST API for mobile clients |

### Suggested Improvements:

1. **Add Unit & Integration Tests**
   - Cover services with JUnit 5
   - Use @WebMvcTest for controllers
   - Use @DataJpaTest for repositories

2. **Implement Email Notifications**
   - Spring Mail for ticket updates
   - Password reset functionality

3. **Add Payment Integration**
   - Stripe or PayPal for subscriptions
   - Invoice generation

4. **Implement Live Chat**
   - WebSocket with STOMP
   - Real-time customer support

5. **REST API for Mobile**
   - JWT authentication
   - Swagger/OpenAPI documentation

6. **Audit Logging**
   - Track all changes with `@PrePersist`/`@PreUpdate`
   - Admin audit trail

7. **Caching**
   - Redis for frequently accessed data
   - Plan information caching

---

## Quick Reference

### Default Credentials (Development):

| User Type | Email | Password |
|-----------|-------|----------|
| Demo Customer | demo@albanet.com | password123 |
| Admin | admin@admin.com | admin123 |

### Important URLs:

| URL | Purpose |
|-----|---------|
| `/` | Root (redirects based on auth) |
| `/login` | Customer login |
| `/register` | Customer registration |
| `/home` | Customer dashboard |
| `/staff/login` | Staff login |
| `/staff/dashboard` | Admin dashboard |
| `/staff/my-dashboard` | Staff team dashboard |

---

*Documentation generated: February 2026*
*Project: AlbaNet - Telecommunications Customer Portal*
