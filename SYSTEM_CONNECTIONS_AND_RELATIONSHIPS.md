# AlbaNet - System Connections and Relationships Analysis

## 📋 Table of Contents
1. [System Overview](#system-overview)
2. [Database Entity Relationships](#database-entity-relationships)
3. [Authentication and Security Architecture](#authentication-and-security-architecture)
4. [Module Dependencies and Interactions](#module-dependencies-and-interactions)
5. [Controller-Service-Repository Flow](#controller-service-repository-flow)
6. [Real-Time Communication Flow](#real-time-communication-flow)
7. [Business Logic Connections](#business-logic-connections)
8. [Frontend-Backend Connections](#frontend-backend-connections)
9. [Complete Request Flow Examples](#complete-request-flow-examples)

---

## 1. System Overview

**AlbaNet** is a Spring Boot telecommunications customer portal with separate interfaces for:
- **Customers (Users)**: Subscribe to plans, create tickets, chat support
- **Staff**: Handle tickets, live chat with customers
- **Admins**: Manage staff, oversee all tickets

### Technology Stack
```
┌─────────────────────────────────────────────────────────┐
│                    AlbaNet System                        │
├─────────────────────────────────────────────────────────┤
│ Backend:      Spring Boot 4.0.2 (Java 21)              │
│ Database:     PostgreSQL                                 │
│ Security:     Spring Security (2 filter chains)         │
│ Frontend:     Thymeleaf + Vanilla JavaScript           │
│ Real-time:    SSE (Server-Sent Events) + Polling       │
│ Build:        Maven                                      │
│ Port:         7777                                       │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Database Entity Relationships

### 2.1 Entity Relationship Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          DATABASE SCHEMA                                      │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────┐
│     users           │ (Customer accounts)
├─────────────────────┤
│ PK: id              │
│ UK: email           │
│ first_name          │◄──────────────┐
│ last_name           │               │
│ password (BCrypt)   │               │
│ phone_number        │               │
│ address fields      │               │ FK: user_id
│ active (boolean)    │               │
│ created_at          │               │
└─────────────────────┘               │
        │                             │
        │ FK: user_id                 │
        ▼                             │
┌─────────────────────┐     ┌─────────────────────┐
│ user_subscriptions  │     │ chat_sessions       │
├─────────────────────┤     ├─────────────────────┤
│ PK: id              │     │ PK: id              │
│ FK: user_id         │     │ FK: customer_id ────┘
│ FK: plan_id ────────┼───┐ │ FK: staff_id (nullable)
│ plan_code (enum)    │   │ │ customer_name       │
│ plan_name           │   │ │ customer_email      │
│ category (TV/etc)   │   │ │ staff_name          │
│ duration_months     │   │ │ status (WAITING/ACTIVE/CLOSED)
│ total_price         │   │ │ created_at          │
│ start_date          │   │ │ closed_at           │
│ end_date            │   │ │ last_message        │
│ status (ACTIVE/etc) │   │ │ unread_count        │
│ created_at          │   │ └─────────────────────┘
│ cancelled_at        │   │         │
└─────────────────────┘   │         │ FK: session_id
                          │         ▼
                          │ ┌─────────────────────┐
                          │ │ chat_messages       │
                          │ ├─────────────────────┤
                          │ │ PK: id              │
                          │ │ FK: session_id      │
                          │ │ sender_id           │
                          │ │ sender_name         │
                          │ │ sender_type         │
                          │ │   (CUSTOMER/STAFF)  │
                          │ │ content (TEXT)      │
                          │ │ timestamp           │
                          │ │ read (boolean)      │
                          │ └─────────────────────┘
                          │
                          ▼
┌─────────────────────┐
│ subscription_plans  │ (Catalog)
├─────────────────────┤
│ PK: id              │
│ UK: code (enum)     │
│ name                │
│ description         │
│ category            │
│ monthly_price       │
│ duration_days       │
│ features            │
│ active (boolean)    │
└─────────────────────┘

┌─────────────────────┐
│     staff           │ (Employee accounts)
├─────────────────────┤
│ PK: id              │◄────────────────┐
│ UK: email           │                 │
│ UK: employee_number │                 │
│ first_name          │                 │ FK: staff_id
│ last_name           │                 │
│ password (BCrypt)   │                 │
│ phone_number        │                 │
│ role (ADMIN/IT1/etc)│                 │
│ hired_at            │                 │
│ active (boolean)    │                 │
│ last_login_at       │                 │
└─────────────────────┘                 │
        │                               │
        │ String reference:             │
        │ assigned_to = staff.email     │
        ▼                               │
┌─────────────────────┐                 │
│   ticket_entity     │                 │
├─────────────────────┤                 │
│ PK: id              │                 │
│ title               │                 │
│ description (TEXT)  │                 │
│ category (TV/MOBILE/INTERNET)        │
│ problem_type (enum) │                 │
│ status (OPEN/IN_PROGRESS/DONE)       │
│ priority (LOW/MEDIUM/HIGH)           │
│ assigned_team       │                 │
│ assigned_to (email) │─────────────────┘
│ FK: customer_id     │─────► users.id
│ created_at/by       │
│ updated_at/by       │
│ closed_at           │
│ resolution_summary  │
└─────────────────────┘
```

### 2.2 Key Relationships Explained

| Relationship | Type | Description |
|-------------|------|-------------|
| **User → UserSubscription** | One-to-Many | A customer can have multiple subscriptions (TV + Internet + Mobile) |
| **SubscriptionPlan → UserSubscription** | One-to-Many | Each plan can be subscribed by many users |
| **User → Ticket** | One-to-Many | A customer can create multiple support tickets |
| **Staff → Ticket** | One-to-Many (soft) | Staff assigned to tickets via email reference (not FK) |
| **User → ChatSession** | One-to-Many | A customer can have multiple chat sessions over time |
| **Staff → ChatSession** | One-to-Many | A staff member can join multiple chat sessions |
| **ChatSession → ChatMessage** | One-to-Many | Each chat session contains multiple messages |

### 2.3 Enum Relationships

```java
// Ticket Category → Problem Type → Team Assignment
┌─────────────────────────────────────────────────────┐
│   TicketCategory  →  TicketProblemType  →  Team    │
├─────────────────────────────────────────────────────┤
│   TV              →  TV_NO_SIGNAL       →  IT1     │
│   TV              →  TV_INVOICE_COPY    →  FINANCE │
│   TV              →  TV_EQUIPMENT_ISSUE →  IT2     │
│   MOBILE          →  MOBILE_NO_SERVICE  →  IT1     │
│   MOBILE          →  MOBILE_SIM_ISSUE   →  IT2     │
│   INTERNET        →  INTERNET_SLOW_SPEED→  IT1     │
└─────────────────────────────────────────────────────┘

// Subscription Plans (CatalogCode)
┌─────────────────────────────────────────────────────┐
│   Category   →   CatalogCode    →   Hierarchy      │
├─────────────────────────────────────────────────────┤
│   TV         →   TV_BASIC       →   Level 1        │
│   TV         →   TV_STANDARD    →   Level 2        │
│   TV         →   TV_PREMIUM     →   Level 3        │
│   MOBILE     →   PAKO_S         →   No hierarchy   │
│   MOBILE     →   PAKO_M         →   No hierarchy   │
│   MOBILE     →   PAKO_L         →   No hierarchy   │
│   MOBILE     →   PAKO_XL        →   No hierarchy   │
│   INTERNET   →   WIFI_BASIC     →   Level 1        │
│   INTERNET   →   WIFI_STANDARD  →   Level 2        │
│   INTERNET   →   WIFI_PREMIUM   →   Level 3        │
└─────────────────────────────────────────────────────┘
```

---

## 3. Authentication and Security Architecture

### 3.1 Dual Security Chain Design

Spring Security is configured with **TWO separate filter chains** that handle different user types:

```
┌────────────────────────────────────────────────────────────────┐
│                 Spring Security Configuration                  │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  @Order(1) → staffSecurityFilterChain                         │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Matches: /staff/**                                        │ │
│  │ UserDetailsService: StaffUserDetailsService               │ │
│  │ Login Page: /staff/login                                  │ │
│  │ Success Handler: StaffLoginSuccessHandler                 │ │
│  │ Roles: ADMIN, SUPPORT, FINANCE, IT1, IT2                 │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  @Order(2) → userSecurityFilterChain                          │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Matches: /**  (everything else)                           │ │
│  │ UserDetailsService: CustomUserDetailsService              │ │
│  │ Login Page: /login                                        │ │
│  │ Success URL: /home                                        │ │
│  │ Roles: USER                                               │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                │
│  Shared: PasswordEncoderConfig (BCrypt)                       │
└────────────────────────────────────────────────────────────────┘
```

### 3.2 Authentication Flow Comparison

#### Customer (User) Login Flow:
```
1. Browser: POST /login
   └── email=john@example.com, password=password123

2. Spring Security Filter
   └── Delegates to userAuthenticationProvider

3. CustomUserDetailsService.loadUserByUsername(email)
   └── UserRepository.findByEmail(email)
       └── Returns UserEntity

4. CustomUserDetails wraps UserEntity
   └── Adds GrantedAuthority: "ROLE_USER"

5. DaoAuthenticationProvider validates password
   └── BCryptPasswordEncoder.matches(raw, encoded)

6. Authentication Success
   └── SecurityContext stores Authentication object
   └── Redirect to /home
```

#### Staff Login Flow:
```
1. Browser: POST /staff/login
   └── email=admin@albanet.com, password=admin123

2. Spring Security Filter
   └── Delegates to staffAuthenticationProvider

3. StaffUserDetailsService.loadUserByUsername(email)
   └── StaffService.getActiveStaffByEmail(email)
       └── Returns StaffEntity

4. StaffUserDetails wraps StaffEntity
   └── Adds GrantedAuthority: "ROLE_" + staff.getRole()
       (e.g., "ROLE_ADMIN", "ROLE_IT1")

5. DaoAuthenticationProvider validates password
   └── BCryptPasswordEncoder.matches(raw, encoded)

6. Authentication Success
   └── StaffLoginSuccessHandler.onAuthenticationSuccess()
       └── Redirect based on role:
           • ADMIN → /staff/dashboard
           • Others → /staff/my-dashboard
```

### 3.3 Role-Based Access Control Matrix

| Endpoint Pattern | USER | ADMIN | SUPPORT | IT1/IT2 | FINANCE |
|-----------------|------|-------|---------|---------|---------|
| `/login`, `/register` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/home`, `/tv`, `/mobile`, `/internet` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/profile`, `/my-subscriptions` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/my-tickets` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/api/create-ticket` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/staff/login` | ❌ | ✅ | ✅ | ✅ | ✅ |
| `/staff/dashboard` | ❌ | ✅ | ❌ | ❌ | ❌ |
| `/staff/create-staff` | ❌ | ✅ | ❌ | ❌ | ❌ |
| `/staff/my-dashboard` | ❌ | ❌ | ✅ | ✅ | ✅ |
| `/staff/my-tickets/**` | ❌ | ❌ | ✅ | ✅ | ✅ |
| `/staff/chat`, `/staff/chat/**` | ❌ | ✅ | ✅ | ❌ | ❌ |

---

## 4. Module Dependencies and Interactions

### 4.1 Module Dependency Graph

```
┌─────────────────────────────────────────────────────────────────┐
│                       Module Dependencies                        │
└─────────────────────────────────────────────────────────────────┘

                   AlbaNetApplication (Main)
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
       config/          auth/          exception/
     (Security)     (Login/Register)  (Error Handling)
          │                │                │
          │                └────────┬───────┘
          │                         ▼
          │                      user/
          │                    (Customers)
          │                         │
          │         ┌───────────────┼───────────────┐
          │         ▼               ▼               ▼
          │     client/        subscription/     ticket/
          │   (Customer UI)    (Plans/Subs)   (Support)
          │         │               │               │
          │         └───────────────┼───────────────┘
          │                         │
          ▼                         ▼
       staff/                    chat/
   (Employee System)        (Live Support)
          │
          └──────────► catalog/
                    (Product Enums)
```

### 4.2 Inter-Module Communication

| From Module | To Module | Communication Type | Purpose |
|------------|-----------|-------------------|---------|
| **auth** → **user** | Service call | `UserService.createUser()` | Register new customer |
| **client** → **subscription** | Service call | `SubscriptionPlanService.subscribe()` | Purchase subscription |
| **client** → **ticket** | Service call | `ClientTicketService.createTicket()` | Submit support ticket |
| **ticket** → **user** | Repository query | `UserRepository.findById()` | Validate customer exists |
| **staff** → **ticket** | Service call | `StaffTicketService.claimTicket()` | Assign ticket to staff |
| **admin** → **ticket** | Service call | `AdminTicketService.reassignTicket()` | Change ticket assignment |
| **chat** → **user** | Repository query | Via `CustomUserDetails` | Get customer info for chat |
| **chat** → **staff** | Service call | `StaffService.getActiveStaffByEmail()` | Get staff info for chat |
| **subscription** → **catalog** | Enum reference | `CatalogCode` enum | Identify subscription plan |
| **ticket** → **catalog** | Enum reference | `TicketCategory` enum | Categorize ticket |
| **ticket** → **ticket** (SSE) | Event notification | `TicketSseController.notifyNewTicket()` | Real-time updates |

---

## 5. Controller-Service-Repository Flow

### 5.1 Layered Architecture Pattern

```
┌─────────────────────────────────────────────────────────────────┐
│                    Typical Request Flow                          │
└─────────────────────────────────────────────────────────────────┘

Browser Request (HTTP)
    │
    ▼
┌──────────────────────┐
│   Controller Layer   │  ← @Controller / @RestController
│ • Input validation   │  ← Handles HTTP requests
│ • Authentication     │  ← Gets current user from SecurityContext
│ • Response formatting│
└──────────────────────┘
    │
    ▼
┌──────────────────────┐
│   Service Layer      │  ← @Service
│ • Business logic     │  ← Transaction management (@Transactional)
│ • Validation         │  ← Complex workflows
│ • Cross-module calls │
└──────────────────────┘
    │
    ▼
┌──────────────────────┐
│   Repository Layer   │  ← JpaRepository
│ • Database queries   │  ← Spring Data JPA
│ • CRUD operations    │  ← Custom queries (@Query)
└──────────────────────┘
    │
    ▼
PostgreSQL Database
```

### 5.2 Example: Ticket Module Layers

```
┌────────────────────────────────────────────────────────────────┐
│                   Ticket Module Architecture                    │
└────────────────────────────────────────────────────────────────┘

CLIENT CONTROLLERS:
├── ClientTicketController (REST API)
│   ├── GET /api/problem-types?category={category}
│   ├── POST /api/create-ticket
│   └── GET /api/my-tickets
│
├── TicketViewController (HTML Pages)
│   └── GET /my-tickets → returns "client/my-tickets.html"

STAFF CONTROLLERS:
├── StaffDashboardController (Team Dashboard)
│   ├── GET /staff/my-dashboard
│   └── GET /staff/my-tickets/{team}
│
├── AdminDashboardController (Admin Dashboard)
│   ├── GET /staff/dashboard
│   └── GET /staff/tickets/all
│
├── TicketRestController (Staff Actions)
│   ├── POST /staff/tickets/{id}/claim
│   ├── POST /staff/tickets/{id}/close
│   ├── POST /staff/tickets/{id}/reassign
│   └── POST /staff/tickets/{id}/change-priority

SSE CONTROLLER:
└── TicketSseController (Real-time Notifications)
    └── GET /staff/tickets/stream?team={team}

SERVICES:
├── ClientTicketService
│   ├── createTicket() → Auto-assigns team/priority
│   ├── getMyTickets() → Filters by customer
│   └── getProblemTypesByCategory()
│
├── StaffTicketService
│   ├── getMyTeamTickets() → Filters by assigned_team
│   ├── claimTicket() → Sets assigned_to = current staff email
│   └── closeTicket() → Changes status to DONE
│
└── AdminTicketService
    ├── getAllTickets()
    ├── reassignTicket() → Changes team/assigned_to
    └── changePriority()

REPOSITORY:
└── TicketRepository extends JpaRepository<TicketEntity, Long>
    ├── findByCustomerId()
    ├── findByAssignedTeamOrderByCreatedAtDesc()
    ├── findByAssignedToOrderByCreatedAtDesc()
    └── Custom @Query methods

ENTITY:
└── TicketEntity (JPA Entity, maps to "ticket_entity" table)

DTOs:
├── CreateClientTicketRequest (Input)
├── TicketDto (Output)
└── TicketDetailsResponse (Detailed Output)

MAPPER:
└── TicketMapper (Entity ↔ DTO conversion)
```

---

## 6. Real-Time Communication Flow

### 6.1 Ticket Notifications (Server-Sent Events)

```
┌────────────────────────────────────────────────────────────────┐
│           SSE (Server-Sent Events) Architecture                 │
└────────────────────────────────────────────────────────────────┘

STAFF DASHBOARD (Browser)
│
│ JavaScript: new EventSource('/staff/tickets/stream?team=IT1')
│
▼
┌──────────────────────────────────────────────────────────────┐
│ TicketSseController.streamTicketUpdates(team)                │
│ • Creates SseEmitter for long-lived connection               │
│ • Stores in ConcurrentHashMap<String, List<SseEmitter>>      │
│ • Keeps connection open for 1 hour (configurable)            │
└──────────────────────────────────────────────────────────────┘
│
│ Connection established
│
▼
[WAITING FOR EVENTS...]
│
│ Customer creates ticket via /api/create-ticket
│
▼
┌──────────────────────────────────────────────────────────────┐
│ ClientTicketService.createTicket()                           │
│ 1. Saves ticket to database                                  │
│ 2. Calls: ticketSseController.notifyNewTicket(assignedTeam) │
└──────────────────────────────────────────────────────────────┘
│
▼
┌──────────────────────────────────────────────────────────────┐
│ TicketSseController.notifyNewTicket(team)                    │
│ • Retrieves all SseEmitters for this team                    │
│ • Sends event: name="new-ticket", data={ticket details}      │
│ • Also sends to team="ALL" (for admin dashboard)             │
└──────────────────────────────────────────────────────────────┘
│
▼
STAFF DASHBOARD (Browser)
│
│ EventSource.addEventListener('new-ticket', (event) => {
│     // Play sound
│     // Show notification
│     // Refresh ticket list
│ })
│
▼
Dashboard automatically updates!
```

### 6.2 Chat System (Polling + WebSocket-like behavior)

```
┌────────────────────────────────────────────────────────────────┐
│                   Live Chat Architecture                        │
└────────────────────────────────────────────────────────────────┘

CUSTOMER SIDE (chat widget on /home):
┌──────────────────────────────────────────────────────────────┐
│ 1. User clicks "Start Chat"                                  │
│    POST /api/chat/start                                      │
│    └─► ChatService.getOrCreateSession()                      │
│        • Creates ChatSession (status=WAITING)                │
│        • Returns sessionId                                   │
│                                                              │
│ 2. Widget polls for new messages every 2 seconds:           │
│    GET /api/chat/messages/{sessionId}                        │
│    └─► ChatService.getMessages(sessionId)                   │
│                                                              │
│ 3. User sends message:                                       │
│    POST /api/chat/send                                       │
│    └─► ChatService.sendMessage(                             │
│           sessionId, userId, name, "CUSTOMER", content)      │
│        • Saves ChatMessage                                   │
│        • Updates ChatSession.lastMessage                     │
│        • Increments unreadCount for staff                    │
└──────────────────────────────────────────────────────────────┘

STAFF SIDE (/staff/chat page):
┌──────────────────────────────────────────────────────────────┐
│ 1. Page loads and polls for sessions every 5 seconds:       │
│    GET /staff/chat/sessions                                  │
│    └─► ChatService.getActiveSessions()                       │
│        • Returns all WAITING + ACTIVE sessions               │
│                                                              │
│ 2. Staff clicks on a session:                               │
│    a) If status=WAITING:                                     │
│       POST /staff/chat/join/{sessionId}                      │
│       └─► ChatService.joinSession(sessionId, staffId, name)  │
│           • Sets ChatSession.staffId, staffName              │
│           • Changes status to ACTIVE                         │
│                                                              │
│    b) Load messages:                                         │
│       GET /staff/chat/messages/{sessionId}                   │
│                                                              │
│    c) Mark as read:                                          │
│       POST /staff/chat/read/{sessionId}                      │
│       └─► ChatService.markAsRead(sessionId, "STAFF")         │
│           • Sets ChatMessage.read = true for customer msgs   │
│           • Resets ChatSession.unreadCount to 0              │
│                                                              │
│ 3. Staff sends message:                                      │
│    POST /staff/chat/send                                     │
│    └─► ChatService.sendMessage(                             │
│           sessionId, staffId, name, "STAFF", content)        │
│                                                              │
│ 4. Chat widget polls for new customer messages:             │
│    Every 2 seconds: GET /staff/chat/messages/{sessionId}     │
│    • Filters messages where id > lastMessageId               │
│    • Appends only customer messages to UI                    │
│                                                              │
│ 5. Staff closes chat:                                        │
│    POST /staff/chat/close/{sessionId}                        │
│    └─► ChatService.closeSession(sessionId)                  │
│        • Changes status to CLOSED                            │
│        • Sets closedAt timestamp                             │
└──────────────────────────────────────────────────────────────┘

DATABASE FLOW:
chat_sessions (stores conversation metadata)
    │
    └──► chat_messages (stores individual messages)
         • Links to session via session_id
         • Sender type: CUSTOMER or STAFF
```

**Why not WebSocket?**
- Chat is one-way dominant (customer asks, staff responds)
- Polling is simpler and easier to debug
- No need for WebSocket server overhead
- SSE could be added later for staff-side updates

---

## 7. Business Logic Connections

### 7.1 Ticket Auto-Routing Logic

```
┌────────────────────────────────────────────────────────────────┐
│            Automatic Ticket Assignment Flow                     │
└────────────────────────────────────────────────────────────────┘

User selects: Category = "TV", Problem = "No signal/channels not working"
    │
    ▼
Frontend sends: problemTypeCode = "TV_NO_SIGNAL"
    │
    ▼
ClientTicketService.createTicket(request)
    │
    ├─► TicketProblemType.valueOf("TV_NO_SIGNAL")
    │   └─► Returns enum: TV_NO_SIGNAL
    │       • category = TV
    │       • assignedTeam = "IT1"
    │       • priority = HIGH
    │
    ├─► Creates TicketEntity
    │   • title = "No signal/channels not working"
    │   • category = TicketCategory.TV
    │   • problemType = TicketProblemType.TV_NO_SIGNAL
    │   • status = OPEN
    │   • priority = HIGH (from enum)
    │   • assignedTeam = "IT1" (from enum)
    │   • assignedTo = null (not yet claimed)
    │   • customerId = current user ID
    │
    ├─► TicketRepository.save(ticket)
    │
    └─► TicketSseController.notifyNewTicket("IT1")
        └─► All IT1 staff dashboards receive notification
```

**Team Assignment Rules:**
```java
public enum TicketProblemType {
    // Finance handles billing/invoices
    TV_INVOICE_COPY(..., "FINANCE", ...),
    MOBILE_DOUBLE_CHARGE(..., "FINANCE", ...),
    INTERNET_PAYMENT_NO_ACTIVATION(..., "FINANCE", ...),
    
    // IT1 handles connectivity issues
    TV_NO_SIGNAL(..., "IT1", HIGH),
    MOBILE_NO_SERVICE(..., "IT1", HIGH),
    INTERNET_SLOW_SPEED(..., "IT1", MEDIUM),
    
    // IT2 handles equipment/installation
    TV_EQUIPMENT_ISSUE(..., "IT2", HIGH),
    MOBILE_SIM_ISSUE(..., "IT2", HIGH),
    INTERNET_ROUTER_ISSUE(..., "IT2", HIGH)
}
```

### 7.2 Subscription Hierarchy and Upgrade Logic

```
┌────────────────────────────────────────────────────────────────┐
│              Subscription Hierarchy System                      │
└────────────────────────────────────────────────────────────────┘

PlanHierarchy.java defines:
├── TV: BASIC(1) < STANDARD(2) < PREMIUM(3)
├── INTERNET: BASIC(1) < STANDARD(2) < PREMIUM(3)
└── MOBILE: No hierarchy (independent plans)

SCENARIO 1: User has TV_BASIC, subscribes to TV_STANDARD
┌──────────────────────────────────────────────────────────────┐
│ SubscriptionPlanService.subscribe(userId, TV_STANDARD)        │
│ 1. Check existing subscription for category=TV               │
│    └─► Found: UserSubscription (plan=TV_BASIC)               │
│                                                              │
│ 2. Compare hierarchy:                                        │
│    • Current: TV_BASIC (level 1)                             │
│    • New: TV_STANDARD (level 2)                              │
│    • Result: UPGRADE                                         │
│                                                              │
│ 3. Create PENDING subscription:                              │
│    • status = PENDING                                        │
│    • startDate = current subscription endDate                │
│    • endDate = startDate + duration                          │
│                                                              │
│ 4. Current subscription remains ACTIVE until endDate         │
└──────────────────────────────────────────────────────────────┘

SCENARIO 2: User has no TV subscription, subscribes to TV_PREMIUM
┌──────────────────────────────────────────────────────────────┐
│ SubscriptionPlanService.subscribe(userId, TV_PREMIUM)         │
│ 1. No existing subscription found                            │
│ 2. Create ACTIVE subscription:                               │
│    • status = ACTIVE                                         │
│    • startDate = now()                                       │
│    • endDate = startDate + duration                          │
└──────────────────────────────────────────────────────────────┘

SCENARIO 3: User has PAKO_M (mobile), subscribes to PAKO_L
┌──────────────────────────────────────────────────────────────┐
│ SubscriptionPlanService.subscribe(userId, PAKO_L)             │
│ 1. Found existing: PAKO_M                                    │
│ 2. Mobile has NO hierarchy → treat as extension              │
│ 3. Create new ACTIVE subscription immediately               │
│ 4. Old subscription can be cancelled or allowed to expire    │
└──────────────────────────────────────────────────────────────┘
```

### 7.3 Staff Ticket Claiming Workflow

```
┌────────────────────────────────────────────────────────────────┐
│                  Ticket Claim Lifecycle                         │
└────────────────────────────────────────────────────────────────┘

INITIAL STATE:
TicketEntity:
    status = OPEN
    assignedTeam = "IT1"
    assignedTo = null

IT1 Staff (John) views /staff/my-dashboard:
┌──────────────────────────────────────────────────────────────┐
│ StaffDashboardController.getMyTeamTickets()                   │
│ └─► StaffTicketService.getMyTeamTickets("IT1")               │
│     └─► TicketRepository.findByAssignedTeamOrderBy...        │
│         • Returns all tickets where assignedTeam = "IT1"     │
│                                                              │
│ Shows: "Ticket #123 - TV No Signal - [CLAIM BUTTON]"        │
└──────────────────────────────────────────────────────────────┘

John clicks [CLAIM]:
┌──────────────────────────────────────────────────────────────┐
│ POST /staff/tickets/123/claim                                │
│ └─► TicketRestController.claimTicket(123, auth)              │
│     └─► StaffTicketService.claimTicket(123, "john@albanet") │
│         • ticket.setAssignedTo("john@albanet.com")           │
│         • ticket.setStatus(IN_PROGRESS)                      │
│         • ticket.setUpdatedAt(now)                           │
│         • ticket.setUpdatedBy("John Doe")                    │
│         • TicketRepository.save(ticket)                      │
└──────────────────────────────────────────────────────────────┘

UPDATED STATE:
TicketEntity:
    status = IN_PROGRESS
    assignedTeam = "IT1"
    assignedTo = "john@albanet.com"

John works on ticket and closes it:
┌──────────────────────────────────────────────────────────────┐
│ POST /staff/tickets/123/close                                │
│ └─► StaffTicketService.closeTicket(123, "Router reset")     │
│     • ticket.setStatus(DONE)                                 │
│     • ticket.setClosedAt(now)                                │
│     • ticket.setResolutionSummary("Router reset")            │
│     • TicketRepository.save(ticket)                          │
└──────────────────────────────────────────────────────────────┘

FINAL STATE:
TicketEntity:
    status = DONE
    assignedTeam = "IT1"
    assignedTo = "john@albanet.com"
    closedAt = 2026-02-05 14:30:00
    resolutionSummary = "Router reset fixed the issue"
```

---

## 8. Frontend-Backend Connections

### 8.1 Client Pages and Their Endpoints

| Page | Template | Key Endpoints | Purpose |
|------|----------|---------------|---------|
| **Home** | `client/home.html` | `GET /home` | Dashboard after login |
| **TV Plans** | `client/tv-plans.html` | `GET /tv`, `POST /subscribe` | Browse/buy TV subscriptions |
| **Mobile Plans** | `client/mobile-plans.html` | `GET /mobile`, `POST /subscribe` | Browse/buy mobile plans |
| **Internet Plans** | `client/internet-plans.html` | `GET /internet`, `POST /subscribe` | Browse/buy internet plans |
| **My Subscriptions** | `client/my-subscriptions.html` | `GET /my-subscriptions`, `POST /cancel-subscription/{id}` | View active subscriptions |
| **Help/Tickets** | `client/help.html` | `GET /help`, `GET /api/problem-types`, `POST /api/create-ticket` | Create support tickets |
| **My Tickets** | `client/my-tickets.html` | `GET /my-tickets`, `GET /api/my-tickets` | View ticket history |
| **Profile** | `client/profile.html` | `GET /profile`, `POST /profile/update` | Edit account details |

### 8.2 Staff Pages and Their Endpoints

| Page | Template | Key Endpoints | Purpose |
|------|----------|---------------|---------|
| **Staff Login** | `staff/login.html` | `GET /staff/login`, `POST /staff/login` | Staff authentication |
| **Admin Dashboard** | `staff/dashboard.html` | `GET /staff/dashboard`, `GET /staff/tickets/all` | Overview of all tickets |
| **Staff Dashboard** | `staff/my-dashboard.html` | `GET /staff/my-dashboard`, `GET /staff/my-tickets/{team}` | Team-specific tickets |
| **Create Staff** | `staff/create-staff.html` | `GET /staff/create-staff`, `POST /staff/create` | Add new employees (Admin only) |
| **Live Chat** | `staff/chat.html` | `GET /staff/chat`, `GET /staff/chat/sessions`, `POST /staff/chat/send` | Customer support chat |

### 8.3 JavaScript-Backend Interactions

#### Example: Dynamic Problem Type Loading
```javascript
// File: client/help.html (embedded script)

// User selects category dropdown
document.getElementById('category').addEventListener('change', (e) => {
    const category = e.target.value;
    
    // AJAX request to backend
    fetch(`/api/problem-types?category=${category}`)
        .then(response => response.json())
        .then(problemTypes => {
            // Populate problem type dropdown
            const select = document.getElementById('problemType');
            select.innerHTML = problemTypes.map(pt => 
                `<option value="${pt.code}">${pt.description}</option>`
            ).join('');
        });
});

// Backend endpoint:
@GetMapping("/api/problem-types")
public ResponseEntity<List<ProblemTypeDto>> getProblemTypes(
    @RequestParam TicketCategory category
) {
    return ResponseEntity.ok(
        clientTicketService.getProblemTypesByCategory(category)
    );
}
```

#### Example: SSE Event Listener
```javascript
// File: staff/dashboard.html (embedded script)

// Establish SSE connection
const eventSource = new EventSource(`/staff/tickets/stream?team=${teamName}`);

// Listen for new ticket events
eventSource.addEventListener('new-ticket', (event) => {
    const data = JSON.parse(event.data);
    
    // Play notification sound
    playNotificationSound();
    
    // Show browser notification
    new Notification('New Ticket Assigned', {
        body: `Ticket #${data.id}: ${data.title}`
    });
    
    // Refresh ticket list
    loadTickets();
});
```

---

## 9. Complete Request Flow Examples

### 9.1 Customer Creates a Support Ticket

```
┌────────────────────────────────────────────────────────────────┐
│          COMPLETE FLOW: Create Support Ticket                   │
└────────────────────────────────────────────────────────────────┘

1. USER ACTION: Visits /help page
   ├─► Browser: GET /help
   └─► ClientTicketController.showHelpPage()
       ├─► Checks authentication (Spring Security)
       ├─► Gets current user from SecurityContext
       └─► Returns "client/help" template

2. PAGE LOADS: Shows ticket creation form
   ├─► Category dropdown: TV, MOBILE, INTERNET
   └─► Problem type dropdown: Initially empty

3. USER ACTION: Selects category "INTERNET"
   ├─► JavaScript: onChange event
   └─► AJAX GET /api/problem-types?category=INTERNET
       └─► ClientTicketController.getProblemTypes(INTERNET)
           └─► ClientTicketService.getProblemTypesByCategory()
               └─► Returns: [
                       {code: "INTERNET_NO_CONNECTION", description: "No internet connection"},
                       {code: "INTERNET_SLOW_SPEED", description: "Internet is very slow"},
                       ...
                   ]

4. USER ACTION: Selects problem "Internet is very slow"
   ├─► JavaScript: Sets form field problemTypeCode = "INTERNET_SLOW_SPEED"
   └─► User types description: "Internet speed is 1 Mbps instead of 100 Mbps"

5. USER ACTION: Submits form
   ├─► JavaScript: AJAX POST /api/create-ticket
   │   Body: {
   │       "categoryCode": "INTERNET",
   │       "problemTypeCode": "INTERNET_SLOW_SPEED",
   │       "description": "Internet speed is 1 Mbps instead of 100 Mbps"
   │   }
   │
   └─► ClientTicketController.createTicket(request, auth)
       │
       ├─► Gets current user from SecurityContext
       │   └─► CustomUserDetails → UserEntity (id=42, name="John Doe")
       │
       └─► ClientTicketService.createTicket(request, userId=42)
           │
           ├─► Validates user exists:
           │   └─► UserRepository.findById(42) → UserEntity
           │
           ├─► Parses problem type:
           │   └─► TicketProblemType.valueOf("INTERNET_SLOW_SPEED")
           │       • Returns enum with:
           │           - category = INTERNET
           │           - assignedTeam = "IT1"
           │           - priority = MEDIUM
           │           - description = "Internet is very slow"
           │
           ├─► Creates TicketEntity:
           │   • id = (auto-generated)
           │   • title = "Internet is very slow"
           │   • description = "Internet speed is 1 Mbps instead of 100 Mbps"
           │   • category = INTERNET
           │   • problemType = INTERNET_SLOW_SPEED
           │   • status = OPEN
           │   • priority = MEDIUM (from enum)
           │   • assignedTeam = "IT1" (from enum)
           │   • assignedTo = null (unclaimed)
           │   • customerId = 42
           │   • createdAt = now()
           │   • createdBy = "John Doe"
           │   • updatedAt = now()
           │   • updatedBy = "John Doe"
           │
           ├─► Saves to database:
           │   └─► TicketRepository.save(ticket) → Ticket #567
           │
           └─► Sends real-time notification:
               └─► TicketSseController.notifyNewTicket("IT1")
                   ├─► Retrieves all SseEmitters for team "IT1"
                   ├─► Sends SSE event to all IT1 dashboards
                   └─► Sends SSE event to team "ALL" (admin)

6. SERVER RESPONSE: Returns to client
   └─► HTTP 200 OK
       Body: {
           "success": true,
           "ticketId": 567,
           "message": "Ticket created successfully"
       }

7. BROWSER: Shows success message
   └─► JavaScript: alert("Ticket #567 created successfully")

8. STAFF DASHBOARDS (IT1 team): Receive notification
   ├─► EventSource receives event name="new-ticket"
   ├─► Plays notification sound
   ├─► Shows browser notification
   └─► Auto-refreshes ticket list
       └─► AJAX GET /staff/my-tickets/IT1
           └─► StaffTicketService.getMyTeamTickets("IT1")
               └─► Ticket #567 now appears in list

9. DATABASE STATE:
   ticket_entity table:
   ┌────┬──────────────────┬────────┬──────────┬──────────┬────────────┬──────┐
   │ id │ title            │ status │ priority │ team     │ assigned_to│ cust │
   ├────┼──────────────────┼────────┼──────────┼──────────┼────────────┼──────┤
   │567 │Internet is slow  │ OPEN   │ MEDIUM   │ IT1      │ NULL       │ 42   │
   └────┴──────────────────┴────────┴──────────┴──────────┴────────────┴──────┘
```

### 9.2 Customer Subscribes to a Plan

```
┌────────────────────────────────────────────────────────────────┐
│         COMPLETE FLOW: Subscribe to Internet Plan              │
└────────────────────────────────────────────────────────────────┘

1. USER ACTION: Visits /internet page
   └─► SubscriptionViewController.showInternetPlans(model)
       ├─► SubscriptionPlanService.getPlansByCategory("INTERNET")
       │   └─► Returns: [WIFI_BASIC, WIFI_STANDARD, WIFI_PREMIUM]
       ├─► Adds plans to model
       └─► Returns "client/internet-plans" template

2. PAGE DISPLAYS: Three plan cards
   ┌───────────────────────────────────────────────────────────┐
   │ WIFI_BASIC        WIFI_STANDARD      WIFI_PREMIUM         │
   │ $19.99/month      $39.99/month       $59.99/month         │
   │ 50 Mbps           100 Mbps           300 Mbps             │
   │ [Subscribe]       [Subscribe]        [Subscribe]          │
   └───────────────────────────────────────────────────────────┘

3. USER ACTION: Clicks [Subscribe] on WIFI_STANDARD (plan ID = 8)
   └─► Form: POST /subscribe
       Body: planId=8&durationMonths=1

4. SubscriptionViewController.subscribe(8, 1, auth)
   │
   ├─► Gets current user from SecurityContext
   │   └─► CustomUserDetails → userId = 42
   │
   └─► SubscriptionPlanService.subscribe(userId=42, planId=8, duration=1)
       │
       ├─► Loads plan:
       │   └─► SubscriptionPlanRepository.findById(8)
       │       └─► SubscriptionPlanEntity:
       │           • id = 8
       │           • code = WIFI_STANDARD
       │           • name = "WiFi Standard"
       │           • category = "INTERNET"
       │           • monthlyPrice = 39.99
       │           • durationDays = 30
       │
       ├─► Checks for existing subscription:
       │   └─► UserSubscriptionRepository.findByUserIdAndCategory(42, "INTERNET")
       │       └─► Found: UserSubscriptionEntity (plan=WIFI_BASIC, status=ACTIVE)
       │
       ├─► Compares hierarchy:
       │   └─► PlanHierarchy.compareHierarchy(WIFI_BASIC, WIFI_STANDARD)
       │       • WIFI_BASIC = level 1
       │       • WIFI_STANDARD = level 2
       │       • Result: UPGRADE
       │
       ├─► Calculates dates:
       │   • existingSub.endDate = 2026-02-20
       │   • newStartDate = 2026-02-20
       │   • newEndDate = 2026-03-20 (30 days later)
       │
       ├─► Creates NEW subscription:
       │   └─► UserSubscriptionEntity:
       │       • userId = 42
       │       • planId = 8
       │       • planCode = WIFI_STANDARD
       │       • planName = "WiFi Standard"
       │       • category = "INTERNET"
       │       • durationMonths = 1
       │       • totalPrice = 39.99
       │       • startDate = 2026-02-20
       │       • endDate = 2026-03-20
       │       • status = PENDING (will activate when current ends)
       │       • createdAt = now()
       │
       └─► UserSubscriptionRepository.save(newSubscription)

5. SERVER RESPONSE: Redirect
   └─► HTTP 302 Redirect to /my-subscriptions?success=true

6. BROWSER: Shows subscription page with updated list
   └─► GET /my-subscriptions
       └─► SubscriptionViewController.showMySubscriptions()
           └─► Returns subscriptions:
               ┌───────────────────────────────────────────────┐
               │ Current Subscriptions:                        │
               │ • WiFi Basic - ACTIVE until 2026-02-20        │
               │                                               │
               │ Upcoming Subscriptions:                       │
               │ • WiFi Standard - PENDING from 2026-02-20     │
               └───────────────────────────────────────────────┘

7. DATABASE STATE:
   user_subscriptions table:
   ┌────┬────────┬────────┬──────────────┬────────┬────────────┬────────────┐
   │ id │user_id │plan_id │ plan_code    │ status │ start_date │ end_date   │
   ├────┼────────┼────────┼──────────────┼────────┼────────────┼────────────┤
   │ 15 │  42    │   5    │ WIFI_BASIC   │ ACTIVE │ 2026-01-20 │ 2026-02-20 │
   │ 16 │  42    │   8    │WIFI_STANDARD │PENDING │ 2026-02-20 │ 2026-03-20 │
   └────┴────────┴────────┴──────────────┴────────┴────────────┴────────────┘

8. BACKGROUND JOB (would be scheduled task):
   On 2026-02-20:
   └─► Subscription #15 expires (keep as EXPIRED)
   └─► Subscription #16 activates (change status to ACTIVE)
```

---

## 10. Summary Diagram: Complete System Architecture

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                           AlbaNet System Map                                    │
└────────────────────────────────────────────────────────────────────────────────┘

USERS:
┌──────────────┐              ┌──────────────┐              ┌──────────────┐
│  Customer    │              │    Staff     │              │    Admin     │
│  (ROLE_USER) │              │ (ROLE_IT1/2) │              │ (ROLE_ADMIN) │
└──────┬───────┘              └──────┬───────┘              └──────┬───────┘
       │                             │                             │
       │ /login                      │ /staff/login                │ /staff/login
       ▼                             ▼                             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Spring Security Filters                              │
│  ┌─────────────────────┐                   ┌─────────────────────┐          │
│  │ User Filter Chain   │                   │ Staff Filter Chain  │          │
│  │ @Order(2)           │                   │ @Order(1)           │          │
│  │ → CustomUserDetails │                   │ → StaffUserDetails  │          │
│  └─────────────────────┘                   └─────────────────────┘          │
└──────────────────────────────────────────────────────────────────────────────┘
       │                             │                             │
       ▼                             ▼                             ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ ClientController │      │ StaffDashboard   │      │ AdminDashboard   │
│ • /home          │      │ • /my-dashboard  │      │ • /dashboard     │
│ • /tv, /mobile   │      │ • /my-tickets    │      │ • /create-staff  │
│ • /my-tickets    │      │ • /chat          │      │ • /tickets/all   │
│ • /help          │      └──────────────────┘      └──────────────────┘
└────────┬─────────┘              │                           │
         │                        │                           │
         ▼                        ▼                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Services Layer                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │   User      │  │Subscription │  │   Ticket    │  │    Chat     │       │
│  │  Service    │  │   Service   │  │  Services   │  │  Service    │       │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘       │
└─────────────────────────────────────────────────────────────────────────────┘
         │                        │                    │                │
         ▼                        ▼                    ▼                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Repository Layer (JPA)                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │   User      │  │Subscription │  │   Ticket    │  │    Chat     │       │
│  │  Repo       │  │   Repos     │  │    Repo     │  │   Repos     │       │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘       │
└─────────────────────────────────────────────────────────────────────────────┘
         │                        │                    │                │
         └────────────────────────┴────────────────────┴────────────────┘
                                         ▼
                    ┌──────────────────────────────────────────┐
                    │       PostgreSQL Database                │
                    │  • users                                 │
                    │  • staff                                 │
                    │  • subscription_plans                    │
                    │  • user_subscriptions                    │
                    │  • ticket_entity                         │
                    │  • chat_sessions                         │
                    │  • chat_messages                         │
                    └──────────────────────────────────────────┘

REAL-TIME COMMUNICATION:
┌──────────────────────────────────────────────────────────────────────────────┐
│  Staff Dashboard Browser ◄────SSE────┤ TicketSseController                  │
│    │                                   │   /staff/tickets/stream?team=IT1     │
│    │                                   │   • Keeps connection open            │
│    │                                   │   • Notifies on new ticket           │
│    │                                                                           │
│  Chat Widget (Polling) ────2s────► ChatController                             │
│    GET /api/chat/messages              • Returns new messages                 │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Conclusion

This document provides a comprehensive map of all connections, relationships, and data flows in the AlbaNet system. Key takeaways:

1. **Dual Authentication**: Separate security chains for customers and staff
2. **Auto-Routing**: Tickets automatically assigned based on problem type
3. **Real-Time Updates**: SSE for instant notifications, polling for chat
4. **Modular Architecture**: Clean separation of concerns by domain
5. **Database Relationships**: Clear FK relationships with soft references where needed

Use this document as a reference when:
- Adding new features
- Debugging issues
- Onboarding new developers
- Understanding system behavior
