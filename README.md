# SupportAI – AI-Powered Smart Ticket Management System

**A secure, AI-powered customer support and ticket management backend built with Java 21, Spring Boot, Spring Security, JWT, Hibernate, MySQL, Docker, and Ollama.**

SupportAI is a backend application that automates customer support workflows through ticket management, AI-based ticket classification, intelligent agent assignment, SLA monitoring, escalation, analytics, and an AI-powered conversational assistant.

The system combines traditional backend business logic with locally hosted Large Language Models (LLMs). AI interprets customer requests and provides recommendations, while Spring Boot handles authentication, authorization, validation, business rules, and database operations.

---

# 1. Project Overview

Traditional customer support systems often require manual ticket classification, agent assignment, priority selection, and SLA monitoring.

SupportAI addresses these challenges by providing a centralized backend application where:

- Customers can register, log in, create tickets, and track their issues.
- Agents can manage assigned tickets and communicate with customers.
- Managers can receive SLA escalation notifications.
- Administrators can manage users, assignments, and support operations.
- AI can classify tickets and understand customer requests.
- Automated services can monitor SLA deadlines and escalate overdue tickets.
- Dashboard APIs provide operational and historical analytics.

## Main objectives

1. Develop a secure RESTful backend using Spring Boot.
2. Implement authentication and role-based authorization.
3. Automate ticket classification using Ollama.
4. Recommend agents based on workload and expertise.
5. Monitor SLA deadlines and detect breaches automatically.
6. Maintain a complete ticket activity history.
7. Implement an AI chatbot with conversation context and RAG.
8. Containerize the backend and database using Docker Compose.

---

# 2. Technology Stack

| Category | Technology | Purpose |
|---|---|---|
| Programming Language | Java 21 | Backend development |
| Backend Framework | Spring Boot 4.x | REST API development |
| Security | Spring Security | Authentication and authorization |
| Authentication | JWT | Stateless authentication |
| Password Security | BCrypt | Password hashing |
| ORM | Hibernate | Object-relational mapping |
| Persistence | Spring Data JPA | Database operations |
| Database | MySQL 8.4 | Persistent data storage |
| AI Runtime | Ollama | Local LLM execution |
| AI Model | Llama 3.2 | Classification and chatbot intelligence |
| API Testing | Postman | API testing |
| Unit Testing | JUnit 5, Mockito | Backend testing |
| Containerization | Docker | Application containerization |
| Orchestration | Docker Compose | Multi-container application management |
| Build Tool | Maven | Dependency and build management |
| API Style | REST | Client-server communication |

---

# 3. High-Level System Architecture

```text
                 CUSTOMER / AGENT / MANAGER / ADMIN
                                |
                                v
                         CLIENT APPLICATION
                                |
                                v
                         REST API REQUEST
                                |
                                v
                     SPRING BOOT CONTROLLERS
                                |
                                v
                       SPRING SECURITY
                                |
                       JWT AUTHENTICATION
                                |
                       ROLE AUTHORIZATION
                                |
                                v
                          SERVICE LAYER
                                |
        +-----------------------+-------------------------+
        |                       |                         |
        v                       v                         v
  USER / AUTH MODULE      TICKET MODULE              AI MODULE
        |                       |                         |
        |                 Ticket Services          Ollama Client
        |                 Assignment               Llama 3.2
        |                 Comments                 Classification
        |                 History                  Chatbot
        |                                           RAG
        |                       |                         |
        +-----------------------+-------------------------+
                                |
                +---------------+----------------+
                |                                |
                v                                v
           SLA MODULE                      ADMIN MODULE
                |                                |
          SLA Calculator                    Dashboard
          SLA Scheduler                     Analytics
          Breach Detection                  Workload
          Escalation                        SLA Reports
                |                                |
                +---------------+----------------+
                                |
                                v
                      SPRING DATA JPA
                                |
                                v
                         HIBERNATE ORM
                                |
                                v
                           MYSQL 8.4
```

## Request processing flow

1. A client sends an HTTP request to a REST controller.
2. Spring Security processes the request.
3. JWT authentication validates the token.
4. The authenticated user's identity and authorities are established.
5. Role-based authorization checks whether the user can access the operation.
6. The controller delegates the operation to the appropriate service.
7. The service executes business logic and applies validation.
8. Spring Data JPA communicates with MySQL through Hibernate.
9. The application maps the result into a response DTO.
10. The controller returns the HTTP response.

For AI-powered operations, the relevant service also communicates with the local Ollama runtime.

---

# 4. Application Package and Class Architecture

The application follows a layered architecture.

The following describes the major class responsibilities and the project modules. Exact package paths should be checked against the source repository.

## 4.1 Controller Layer

Controllers expose REST endpoints and receive HTTP requests.

Responsibilities:

- Handle HTTP methods such as GET, POST, PATCH, and DELETE.
- Receive request DTOs.
- Extract authenticated user information.
- Apply endpoint-level role restrictions.
- Delegate business operations to services.
- Return response DTOs.

Major controller areas:

| Controller area | Responsibility |
|---|---|
| Auth Controller | Registration and login |
| Ticket Controller | Ticket operations |
| Admin Controller | Administrative operations |
| Agent Controller | Agent-specific ticket operations |
| Comment Controller | Ticket comments |
| Chat Controller | AI chatbot conversations |
| Notification Controller | Notification operations |
| SLA Controller / Ticket Controller | SLA status and related operations |

The exact controller class names and endpoint prefixes should be verified in the source code.

## 4.2 Service Layer

The service layer contains the application's business logic.

Important implemented service responsibilities include:

| Service / implementation | Responsibility |
|---|---|
| CustomUserDetailsService | Load user details for Spring Security |
| TicketService | Ticket business operations |
| AiClassificationService | AI-based ticket classification |
| SlaService | SLA deadline and status calculations |
| SlaBreachService | Detect overdue tickets |
| SlaScheduler | Execute scheduled SLA checks |
| SlaEscalationService | Escalate breached tickets |
| SlaAnalyticsService | SLA analytics and reporting |
| TicketHistory handling | Record ticket activity |
| Agent recommendation service | Recommend agents and perform auto-assignment |
| Chat services | Process chatbot conversations and intents |
| Knowledge-base services | Manage and retrieve knowledge information |
| Notification service | Create user notifications |

The application uses constructor-based dependency injection with Spring-managed beans.

For example:

```java
@Service
@RequiredArgsConstructor
public class SlaServiceImpl implements SlaService {

    private final TicketRepository ticketRepository;
}
```

Spring injects the required repository when creating the service.

## 4.3 Repository Layer

Repositories use Spring Data JPA to perform database operations.

Examples of repository responsibilities:

- Find users by email.
- Find users by role.
- Save and retrieve tickets.
- Search tickets using specifications.
- Count tickets by status, priority, and category.
- Retrieve ticket history.
- Retrieve chatbot conversations and messages.
- Retrieve knowledge-base information.
- Store and retrieve notifications.

A representative repository declaration:

```java
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
```

Spring Data JPA generates the required queries from repository method names.

## 4.4 DTO Layer

DTOs (Data Transfer Objects) separate API request and response structures from database entities.

Important DTO examples from SupportAI include:

| DTO | Purpose |
|---|---|
| SlaStatusResponse | Individual ticket SLA status |
| SlaAnalyticsResponse | Overall SLA dashboard metrics |
| SlaReportResponse | Filtered SLA reporting |
| HistoricalAnalyticsResponse | Historical ticket resolution analytics |
| DateRangeAnalyticsResponse | Date-range ticket analytics |
| AgentRecommendationResponse | Agent recommendation results |
| OverloadedAgentResponse | Overloaded agent information |
| AiIntentResponse | Structured chatbot intent information |

DTOs help avoid exposing entity structures directly to API clients.

## 4.5 Security Layer

Security-related components are responsible for:

- JWT generation and validation.
- Password encoding.
- User authentication.
- Loading user details.
- Assigning role authorities.
- Protecting endpoints.
- Enforcing method-level authorization.

Important implementation details:

```java
@EnableMethodSecurity
```

Enables method-level security annotations.

```java
@PreAuthorize("hasRole('ADMIN')")
```

Restricts an operation to administrators.

The application uses:

```java
.roles(user.getRole().name())
```

Spring Security adds the `ROLE_` prefix to these role authorities.

---

# 5. Database Design and Entity Relationships

SupportAI uses MySQL 8.4 as its relational database.

Hibernate and Spring Data JPA handle object-relational mapping.

The following describes the logical database entities and their relationships based on the implemented application model. Confirm the physical table names using your entity annotations or the MySQL schema before treating the names below as exact.

## 5.1 User Entity

The User entity represents customers, agents, managers, and administrators.

Important fields:

| Field | Purpose |
|---|---|
| id | Primary key |
| name | User name |
| email | User email address |
| password | BCrypt password hash |
| role | User role |

The role is represented using the Role enum.

Supported roles:

- CUSTOMER
- AGENT
- MANAGER
- ADMIN

The email is used for authentication and user lookup.

Passwords are never stored as plain text.

## 5.2 Ticket Entity

The Ticket entity is the central entity of the application.

Important fields:

| Field | Purpose |
|---|---|
| id | Ticket primary key |
| title | Ticket title |
| description | Customer issue description |
| category | Ticket category |
| priority | Ticket priority |
| status | Current ticket status |
| createdBy | Customer who created the ticket |
| assignedAgent | Assigned support agent |
| createdAt | Ticket creation timestamp |
| updatedAt | Last update timestamp |
| aiCategory | AI-suggested category |
| aiPriority | AI-suggested priority |
| aiReason | AI classification explanation |
| slaDeadline | SLA deadline |
| slaBreached | Whether SLA has been breached |
| escalated | Whether ticket has been escalated |
| escalatedAt | Escalation timestamp |

### Ticket enums

**TicketStatus**

```java
OPEN
IN_PROGRESS
RESOLVED
CLOSED
```

**TicketPriority**

```java
LOW
MEDIUM
HIGH
CRITICAL
```

**TicketCategory**

```java
TECHNICAL
BILLING
ACCOUNT
PRODUCT
GENERAL
```

### Ticket relationships

The Ticket entity has two important relationships with User:

```java
@ManyToOne(fetch = FetchType.LAZY)
private User createdBy;

@ManyToOne(fetch = FetchType.LAZY)
private User assignedAgent;
```

Conceptually:

- One customer can create multiple tickets.
- One agent can be assigned multiple tickets.
- Each ticket has one creator.
- Each ticket can have one assigned agent or no assigned agent.

These are many-to-one relationships from Ticket to User.

## 5.3 TicketHistory Entity

TicketHistory records important changes and activities associated with a ticket.

Important fields:

| Field | Purpose |
|---|---|
| id | History primary key |
| ticket | Related ticket |
| changedBy | User responsible for the change |
| action | Type of activity |
| oldValue | Previous value |
| newValue | New value |
| createdAt | Activity timestamp |

Relationships:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "ticket_id", nullable = false)
private Ticket ticket;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "changed_by", nullable = false)
private User changedBy;
```

This provides a record of who changed a ticket and when.

### TicketHistoryAction

Implemented action types include:

```java
CREATED
ASSIGNED
CATEGORY_CHANGED
PRIORITY_CHANGED
STATUS_CHANGED
COMMENT_ADDED
SLA_BREACHED
```

Example history:

```text
Ticket #12
    |
    +-- CREATED
    |
    +-- ASSIGNED
    |
    +-- STATUS_CHANGED
    |
    +-- COMMENT_ADDED
    |
    +-- SLA_BREACHED
```

The history module supports auditing and historical analytics.

## 5.4 Notification Entity

The notification module stores notifications generated by application events.

Examples:

- New ticket assigned to an agent.
- SLA breach notification to managers.
- Other ticket-related notifications.

The notification service centralizes notification creation so that business services can trigger notifications without duplicating notification logic.

The exact notification entity fields and foreign-key mapping should be verified against the source.

## 5.5 ChatConversation Entity

ChatConversation represents a chatbot conversation.

It provides the conversation-level context used to associate messages with a particular chat session.

## 5.6 ChatMessage Entity

ChatMessage stores individual messages exchanged during a chatbot conversation.

Conceptual relationship:

```text
ChatConversation
       |
       | One-to-Many
       |
       v
   ChatMessage
```

One conversation can contain multiple messages.

The chatbot uses conversation history to understand follow-up questions and maintain context.

## 5.7 Knowledge Base

The knowledge-base module stores information used by the chatbot to answer support-related questions.

Its responsibilities include:

- Store knowledge entries.
- Provide administrative knowledge management.
- Retrieve relevant information for incoming chatbot questions.
- Supply retrieved context to the AI response process.

The exact knowledge entity and its database fields should be confirmed from the source code.

## 5.8 Logical Database Relationship Diagram

```text
                    USER
                     |
          +----------+-----------+
          |                      |
          v                      v
       TICKET                NOTIFICATION
          |
     +----+---------------------+
     |                          |
     v                          v
TICKET_HISTORY              COMMENTS
     |
     v
   USER
(changedBy)


CHAT_CONVERSATION
        |
        | One-to-Many
        v
   CHAT_MESSAGE


KNOWLEDGE BASE
        |
        v
  RAG RETRIEVAL
```

**Important:** Comments are represented through the implemented ticket comment/history functionality. The diagram does not imply that comments necessarily use a separate physical database table.

---

# 6. Authentication and Role-Based Access Control

SupportAI implements authentication using Spring Security and JWT.

## 6.1 Registration Flow

```text
User Registration Request
          |
          v
    Auth Controller
          |
          v
    Auth Service
          |
          v
   BCrypt Password Encoder
          |
          v
   Hash User Password
          |
          v
     User Repository
          |
          v
       MySQL
```

The password is encoded using BCrypt before it is saved.

## 6.2 Login Flow

```text
User Login
    |
    v
Email + Password
    |
    v
Authentication Manager
    |
    v
CustomUserDetailsService
    |
    v
Load User from MySQL
    |
    v
Verify Password
    |
    v
Generate JWT
    |
    v
Return Token to Client
```

The client includes the JWT in subsequent protected requests:

```http
Authorization: Bearer <JWT_TOKEN>
```

## 6.3 JWT Request Authentication

```text
Incoming HTTP Request
          |
          v
   JWT Authentication Filter
          |
          v
 Extract Bearer Token
          |
          v
 Validate JWT
          |
          v
 Load User Details
          |
          v
 Set Security Context
          |
          v
 Continue Request
```

The application uses stateless authentication, so it does not rely on an HTTP session to maintain the authenticated user.

## 6.4 Role-Based Permissions

| Operation | CUSTOMER | AGENT | MANAGER | ADMIN |
|---|---|---|---|---|
| Register / Login | Yes | Yes | Yes | Yes |
| Create own ticket | Yes | As permitted | As permitted | As permitted |
| View own tickets | Yes | No general customer ownership | As permitted | As permitted |
| Manage assigned tickets | No | Yes | As permitted | Yes |
| Assign agents | No | No | According to endpoint rules | Yes |
| Manage user roles | No | No | No | Yes |
| Access admin dashboard | No | No | No | Yes |
| Receive SLA escalation notifications | No | No | Yes | According to configuration |

The actual endpoint restrictions are enforced by Spring Security and method-level authorization. The table describes the intended role responsibilities; exact permissions should be checked against individual controller and service methods.

## 6.5 Admin Role Management

The application includes an administrator operation for updating user roles:

```http
PATCH /api/admin/users/{id}/role
```

The operation is protected using:

```java
@PreAuthorize("hasRole('ADMIN')")
```

This prevents ordinary customers and agents from changing user roles.

---

# 7. Ticket Management Module

Ticket management is the central business module.

## 7.1 Ticket Creation Flow

```text
Customer
   |
   v
POST /api/tickets
   |
   v
Ticket Controller
   |
   v
Ticket Service
   |
   +----------------------+
   |                      |
   v                      v
AI Classification     SLA Calculation
   |                      |
   v                      v
AI Category           SLA Deadline
AI Priority
AI Reason
   |                      |
   +----------+-----------+
              |
              v
         Create Ticket
              |
              v
        Save in MySQL
              |
              v
       Save History Entry
              |
              v
       Return Ticket DTO
```

The ticket creation process integrates AI classification and SLA deadline calculation.

## 7.2 Ticket Update

Customers can update their own tickets according to business rules.

Agents and administrators have additional capabilities for ticket management.

The application validates the user's identity and permissions before applying changes.

Changes to important fields such as status, priority, and category can be recorded in TicketHistory.

## 7.3 Ticket Search and Filtering

The application uses Spring Data JPA Specifications to support dynamic filtering.

Implemented search dimensions include:

- Assigned agent.
- Ticket creator.
- Status.
- Priority.
- Category.
- Text search.
- Assigned-agent search.
- Customer search.

This allows multiple optional filters to be combined into a single search request.

## 7.4 Ticket Comments

The comment module supports:

```http
POST /api/tickets/{ticketId}/comments
GET  /api/tickets/{ticketId}/comments
```

Access is restricted to authorized users, including the ticket owner, assigned agent, and administrator according to the implemented rules.

Adding a comment also creates a COMMENT_ADDED history event.

---

# 8. AI Ticket Classification Using Ollama

SupportAI integrates Ollama to run an LLM locally.

### AI technology

- Runtime: Ollama
- Model: Llama 3.2
- Backend integration: Spring Boot AI classification service
- Result storage: MySQL

## 8.1 What is Ollama?

Ollama is a runtime that allows language models to run locally.

In SupportAI, Ollama provides the AI inference capability without requiring a hosted LLM API for the configured local workflow.

The model used is:

```text
llama3.2
```

## 8.2 Classification Workflow

```text
Customer Creates Ticket
          |
          v
Ticket Service
          |
          v
AiClassificationService
          |
          v
Build AI Prompt
          |
          v
Send Request to Ollama
          |
          v
Llama 3.2 Model
          |
          v
Generate Classification
          |
          v
Parse AI Response
          |
          v
AI Category / Priority / Reason
          |
          v
Store Results in Ticket
          |
          v
MySQL
```

## 8.3 AI Classification Results

The Ticket entity stores:

| Field | Meaning |
|---|---|
| aiCategory | AI-suggested ticket category |
| aiPriority | AI-suggested ticket priority |
| aiReason | Explanation for the classification |

Example:

```json
{
  "title": "Unable to access my account",
  "description": "I cannot log in after resetting my password.",
  "category": "ACCOUNT",
  "priority": "HIGH",
  "aiCategory": "ACCOUNT",
  "aiPriority": "HIGH",
  "aiReason": "The issue relates to account authentication."
}
```

This is an illustrative example of the result structure, not a guaranteed model response.

AI-generated values are suggestions produced through the model. Backend business rules remain responsible for validating and persisting application data.

---

# 9. AI Chatbot

SupportAI includes an AI-powered chatbot for ticket-related conversations.

The chatbot can understand customer messages, identify their intent, use conversation history, retrieve relevant knowledge, and invoke backend operations.

## 9.1 Chatbot Architecture

```text
          CUSTOMER MESSAGE
                  |
                  v
             Chat API
                  |
                  v
          Chat Controller
                  |
                  v
          Chat Service Layer
                  |
          +-------+--------+
          |                |
          v                v
   Conversation       Knowledge Base
      History             Retrieval
          |                |
          +-------+--------+
                  |
                  v
              Ollama
             Llama 3.2
                  |
                  v
           Intent Processing
                  |
                  v
           AiIntentResponse
                  |
                  v
           Intent Validation
                  |
                  v
       Spring Boot Business Services
                  |
                  v
               MySQL
                  |
                  v
          Chatbot Response
```

## 9.2 Implemented Chat Intents

The chatbot supports the following intent types:

| Intent | Function |
|---|---|
| CREATE_TICKET | Create a support ticket |
| CHECK_TICKET_STATUS | Check ticket status |
| LIST_MY_TICKETS | Retrieve the customer's tickets |
| ADD_COMMENT | Add a comment to a ticket |
| UPDATE_TICKET | Process a ticket update request |
| UNKNOWN | Handle unrecognized requests |

## 9.3 Conversation Management

The chatbot uses ChatConversation and ChatMessage to maintain conversation data.

A conversation can contain multiple messages.

The stored conversation history provides context for subsequent interactions.

Example:

```text
Customer: I cannot log in.

Assistant: I can help you with that.

Customer: Create a support ticket.

Assistant: Your issue can be submitted as a support ticket.
```

The chatbot uses conversation context to understand follow-up requests.

## 9.4 AI Does Not Directly Modify the Database

A critical architectural decision is that the LLM does not receive unrestricted access to the database.

Instead:

1. The AI interprets the customer request.
2. The application extracts the intent and relevant parameters.
3. Spring Boot validates the requested operation.
4. Authentication and authorization are checked.
5. The corresponding backend service executes the operation.
6. The result is returned to the chatbot.

This keeps business operations under application control.

---

# 10. RAG – Retrieval-Augmented Generation

SupportAI includes a knowledge-base retrieval workflow for the chatbot.

## 10.1 What is RAG?

Retrieval-Augmented Generation combines information retrieval with language-model generation.

Instead of relying only on information learned during model training, the application retrieves relevant information from its own knowledge base and supplies that information as context to the LLM.

## 10.2 RAG Workflow

```text
Customer Question
       |
       v
Chat Service
       |
       v
Extract Search Query
       |
       v
Knowledge Base Repository
       |
       v
Retrieve Relevant Entries
       |
       v
Build Context
       |
       v
Send Context + Question to Ollama
       |
       v
Llama 3.2 Generates Response
       |
       v
Return Answer
```

## 10.3 Knowledge Base Management

The knowledge-base module supports:

- Storing support-related knowledge entries.
- Administrative knowledge management.
- Retrieving relevant information.
- Providing retrieved context to chatbot responses.

The current implementation uses keyword-based retrieval.

This means the application searches available knowledge entries using relevant text rather than relying on a demonstrated vector database or embedding-based semantic search.

## 10.4 Why RAG is Useful

Example:

A customer asks:

```text
How can I reset my password?
```

The application retrieves relevant password-reset instructions from its knowledge base.

The retrieved information is supplied to Llama 3.2 along with the customer's question.

The model then generates a response using the available context.

**Important:** The implemented knowledge retrieval is keyword-based. Vector embeddings, vector databases, and semantic similarity search are not claimed as implemented features.

---

# 11. Intelligent Agent Assignment

SupportAI includes an agent recommendation mechanism that considers agent workload and expertise.

## 11.1 Agent Recommendation Logic

The recommendation mechanism considers:

1. Number of active tickets assigned to an agent.
2. Priority of the ticket being assigned.
3. Number of resolved tickets in the same category.

### Active workload

Active workload includes:

```text
OPEN + IN_PROGRESS
```

### Priority multipliers

| Priority | Multiplier |
|---|---:|
| CRITICAL | 2.0 |
| HIGH | 1.5 |
| MEDIUM | 1.0 |
| LOW | 0.5 |

### Scoring logic

```text
workloadScore =
    activeTickets * priorityMultiplier

expertiseScore =
    categoryResolvedTickets * 0.5

totalScore =
    workloadScore - expertiseScore
```

A lower total score is considered better.

The system returns the top three recommended agents.

## 11.2 Auto-Assignment Flow

```text
Admin Requests Auto Assignment
             |
             v
       Ticket Service
             |
             v
      Load Available Agents
             |
             v
     Calculate Agent Scores
             |
             v
       Rank Recommendations
             |
             v
       Select Best Agent
             |
             v
         Assign Ticket
             |
       +-----+------+
       |            |
       v            v
  Save History   Create Notification
       |            |
       +-----+------+
             |
             v
       Return Ticket
```

Auto-assignment reuses the normal ticket assignment operation so that the existing assignment behavior, history creation, and notification logic remain consistent.

---

# 12. SLA Management

SLA stands for Service Level Agreement.

It defines the expected time within which a support issue should be addressed or resolved.

SupportAI implements priority-based SLA deadlines, status tracking, automatic breach detection, escalation, and reporting.

## 12.1 SLA Policy

| Ticket Priority | SLA Target |
|---|---:|
| CRITICAL | 4 hours |
| HIGH | 8 hours |
| MEDIUM | 24 hours |
| LOW | 48 hours |

## 12.2 SLA Deadline Calculation

When a ticket is created, the application calculates the deadline using its creation timestamp and priority.

Conceptually:

```java
slaDeadline = createdAt + priorityBasedDuration;
```

Example:

```text
Created At: 10:00 AM
Priority: HIGH
SLA Target: 8 Hours
SLA Deadline: 06:00 PM
```

The configured policy uses elapsed hours rather than a documented business-hours calendar.

## 12.3 Individual SLA Status API

```http
GET /api/tickets/{ticketId}/sla
```

The response includes:

- Ticket ID.
- SLA deadline.
- Breach status.
- Remaining minutes.
- Current SLA status.

Possible status values:

| Status | Meaning |
|---|---|
| ON_TRACK | More than one hour remains |
| AT_RISK | One hour or less remains, but deadline has not passed |
| BREACHED | SLA deadline has passed or the ticket is marked breached |

## 12.4 Automatic SLA Breach Detection

The application uses Spring Scheduling.

```java
@Scheduled(fixedDelay = 60000)
public void checkSlaBreaches() {
    slaBreachService.detectBreaches();
}
```

The scheduler checks for tickets whose SLA deadlines have passed and which have not yet been marked as breached.

The check runs with a 60-second fixed delay.

## 12.5 SLA Breach Flow

```text
             SLA Scheduler
                   |
                   v
           Detect Overdue Tickets
                   |
                   v
           Check SLA Deadline
                   |
                   v
             Mark Breached
                   |
                   v
          Invoke Escalation Service
                   |
                   v
          Mark Ticket Escalated
                   |
                   v
         Record Ticket History
                   |
                   v
        Notify Managers
```

The escalation mechanism checks whether a ticket has already been escalated to avoid repeated escalation processing.

## 12.6 SLA Analytics

The application provides an administrative SLA analytics endpoint:

```http
GET /api/admin/dashboard/sla-analytics
```

Metrics include:

| Metric | Description |
|---|---|
| totalActiveTickets | OPEN and IN_PROGRESS tickets |
| slaBreachedTickets | Active tickets marked as breached |
| slaAtRiskTickets | Active tickets with one hour or less remaining |
| slaCompliantTickets | Active tickets with more than one hour remaining |
| escalatedTickets | Active escalated tickets |
| criticalBreaches | Active critical-priority tickets marked breached |
| averageResolutionTimeHours | Average historical resolution duration |

## 12.7 SLA Reporting

The SLA reporting service supports filtering by:

- Date range.
- Priority.
- Category.
- SLA status.
- Escalation status.

The response includes total tickets, breach counts, at-risk counts, compliant counts, escalations, critical breaches, and average resolution time.

---

# 13. Ticket History and Historical Analytics

TicketHistory provides an audit trail of important ticket events.

## 13.1 Why Ticket History?

Without history, an application only shows the current ticket state.

Ticket history allows the application to answer:

- Who created a ticket?
- Who assigned the ticket?
- When was its status changed?
- Was the ticket reopened?
- When was a comment added?
- Was the ticket escalated?

## 13.2 Historical Resolution Analytics

The application calculates average resolution time using ticket status history.

The calculation considers reopening cycles.

Example:

```text
Ticket Created
      |
      v
   OPEN
      |
      v
 IN_PROGRESS
      |
      v
  RESOLVED
      |
      v
   REOPENED
      |
      v
 IN_PROGRESS
      |
      v
  RESOLVED
```

A reopened ticket starts a new resolution cycle.

The historical calculation:

1. Reads status-change history.
2. Identifies ticket reopen events.
3. Uses ticket creation time for the first resolution cycle.
4. Uses reopening time for subsequent resolution cycles.
5. Calculates the duration until resolution.
6. Calculates the average across resolution cycles.

This allows historical analytics to account for tickets that were resolved more than once.

---

# 14. Admin Dashboard and Analytics

The administrative dashboard provides operational insights.

## 14.1 Dashboard Statistics

```http
GET /api/admin/dashboard
```

Metrics include:

- Total tickets.
- Open tickets.
- In-progress tickets.
- Resolved tickets.
- Closed tickets.
- Unassigned tickets.
- Status distribution.
- Priority distribution.
- Category distribution.
- Agent workload.
- Overloaded agents.

## 14.2 Date-Range Analytics

```http
GET /api/admin/dashboard/analytics?from=2026-10-01&to=2026-10-22
```

Provides:

- Number of tickets created in the selected period.
- Status distribution.
- Priority distribution.
- Category distribution.

## 14.3 Historical Resolution Analytics

Historical analytics provides:

- Number of resolved tickets.
- Number of closed tickets.
- Number of reopened tickets.
- Average resolution time in hours.

## 14.4 SLA Analytics

The SLA analytics endpoint combines active ticket metrics with historical resolution performance.

All administrative analytics endpoints are protected according to the administrator role requirements.

---

# 15. Notification Management

SupportAI includes a notification service used by other application modules.

Examples of notification events:

- A ticket is assigned to an agent.
- A ticket breaches its SLA.
- A manager needs to be informed about an escalation.

The assignment service creates notifications for newly assigned agents.

The escalation service creates SLA breach notifications for managers.

Notification types include:

```text
TICKET_ASSIGNED
SLA_BREACHED
```

The notification service separates notification creation from the business operation that triggers it.

---

# 16. Global Exception Handling

SupportAI implements centralized exception handling.

The purpose is to provide consistent API error responses instead of exposing raw application exceptions.

The application handles errors such as:

- Resource not found.
- Invalid request data.
- Invalid enum values.
- Unauthorized access.
- Forbidden operations.
- Business-rule violations.
- Unexpected application errors.

Examples:

| HTTP Status | Meaning |
|---|---|
| 200 | Successful request |
| 201 | Resource created |
| 400 | Invalid request |
| 401 | Authentication required or invalid |
| 403 | Insufficient permissions |
| 404 | Resource not found |
| 500 | Internal server error |

Access-denied exceptions are handled as forbidden responses.

---

# 17. Database and Data Flow

The application uses Spring Data JPA and Hibernate for database persistence.

## 17.1 Database Configuration

The database is:

```text
MySQL 8.4
```

The application uses environment-based database configuration.

Inside Docker Compose, the backend connects to MySQL using the service name:

```text
jdbc:mysql://mysql:3306/supportai_db
```

The hostname `mysql` refers to the MySQL service on the Compose network.

It is not `localhost` from inside the backend container.

## 17.2 Data Persistence

The database uses a named Docker volume:

```yaml
volumes:
  supportai_mysql_data:
```

The volume is mounted to the MySQL data directory:

```text
/var/lib/mysql
```

This allows database data to survive normal container removal and recreation.

The volume is removed only when explicitly deleted, for example by using:

```bash
docker compose down -v
```

## 17.3 Example Ticket Data Flow

```text
Customer
   |
   v
POST /api/tickets
   |
   v
Ticket Controller
   |
   v
Ticket Service
   |
   +----------------------+
   |                      |
   v                      v
AI Classification     SLA Calculation
   |                      |
   +----------+-----------+
              |
              v
        Ticket Entity
              |
              v
       Ticket Repository
              |
              v
          Hibernate
              |
              v
            MySQL
              |
              v
        Ticket Response
```

---

# 18. Docker and Docker Compose Architecture

SupportAI uses Docker Compose to run the Spring Boot backend and MySQL database.

## 18.1 Containers

| Container | Image | Responsibility |
|---|---|---|
| supportai-backend | Project-built image | Spring Boot backend |
| supportai-mysql | mysql:8.4 | MySQL database |

Ollama runs separately on the Windows host in the current configuration.

## 18.2 Docker Network

Docker Compose automatically creates a default network.

The containers communicate using their Compose service names.

```text
          WINDOWS HOST
               |
               |
        localhost:8080
               |
               v
      SUPPORTAI BACKEND
        Port 8080
               |
               |
        Docker Network
               |
               v
         MYSQL SERVICE
        Port 3306
               |
               v
       NAMED DOCKER VOLUME


       OLLAMA ON WINDOWS
        Port 11434
               ^
               |
       host.docker.internal
               |
       SUPPORTAI BACKEND
```

## 18.3 Ollama Connection from Docker

Because Ollama runs on the Windows host, the backend container accesses it using:

```text
http://host.docker.internal:11434
```

This allows the Dockerized Spring Boot application to communicate with the host machine's Ollama service.

## 18.4 Docker Commands

Start the application:

```bash
docker compose up --build -d
```

Check containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f
```

Stop containers:

```bash
docker compose down
```

Stop and remove containers and database volume:

```bash
docker compose down -v
```

**Warning:** The final command deletes the named database volume and its stored data.

---

# 19. REST API Reference

The following is a functional overview of the API modules. It is not intended to replace the actual controller mappings. Confirm the exact paths and HTTP methods against the source code before publishing an exhaustive API contract.

## Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate and receive JWT |

## Tickets

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/tickets` | Create a ticket |
| GET | `/api/tickets/my` | Get current user's tickets |
| GET | `/api/tickets/{id}` | Retrieve ticket |
| PATCH | `/api/tickets/{id}` | Update ticket |
| DELETE | `/api/tickets/{id}` | Delete ticket |
| GET | `/api/tickets/my/search` | Search and filter tickets |

## Ticket Comments and History

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/tickets/{ticketId}/comments` | Add a comment |
| GET | `/api/tickets/{ticketId}/comments` | Retrieve comments |
| GET | `/api/tickets/{ticketId}/history` | Retrieve ticket history, if mapped to this path |

## SLA

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tickets/{ticketId}/sla` | Retrieve SLA status |

## Agent Operations

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/agent/tickets/assigned/search` | Search assigned tickets |

## Admin Operations

| Method | Endpoint | Description |
|---|---|---|
| PATCH | `/api/admin/users/{id}/role` | Update a user's role |
| POST | `/api/admin/tickets/{ticketId}/auto-assign` | Automatically assign a ticket |
| GET | `/api/admin/dashboard` | Retrieve dashboard metrics |
| GET | `/api/admin/dashboard/analytics` | Retrieve date-range analytics |
| GET | `/api/admin/dashboard/sla-analytics` | Retrieve SLA analytics |

## Chatbot

The chatbot exposes a chat API under:

```text
/api/chat
```

The request and response DTOs define the exact payload format.

---

# 20. Testing Strategy

SupportAI was tested using Postman and backend unit tests.

## API testing

The following functional areas were tested:

- User registration.
- User login.
- JWT authentication.
- Role-based authorization.
- Ticket creation.
- Ticket retrieval.
- Ticket updates.
- AI classification.
- Ticket assignment.
- Agent recommendation.
- Ticket history.
- Ticket comments.
- SLA status.
- SLA breach detection.
- SLA escalation.
- SLA analytics.
- Admin dashboard.
- Chatbot operations.
- Docker restart.
- MySQL persistence.

## Unit testing

JUnit and Mockito are used to test backend business logic.

Implemented testing includes the ticket review business rules and related service behavior.

The project should not be described as having complete integration-test coverage unless that has been verified separately.

---

# 21. Running the Project Locally

## Prerequisites

- Java 21.
- Maven.
- Docker Desktop.
- MySQL 8.4, if not using Docker.
- Ollama.
- Llama 3.2 model.

## Step 1: Clone the repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL

cd supportai-backend
```

## Step 2: Download the Ollama model

```bash
ollama pull llama3.2
```

Ensure Ollama is running and listening on port `11434`.

## Step 3: Configure environment variables

Configure:

```env
DB_URL=jdbc:mysql://mysql:3306/supportai_db?createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=your_secure_password

JWT_SECRET=your_secure_base64_encoded_secret
JWT_EXPIRATION=86400000

AI_BASE_URL=http://host.docker.internal:11434
AI_MODEL=llama3.2
```

These are configuration examples. Match the values to the application's actual property names and Docker Compose configuration.

Never commit real passwords, JWT secrets, or private environment files.

## Step 4: Start Docker Compose

```bash
docker compose up --build -d
```

## Step 5: Verify containers

```bash
docker compose ps
```

The MySQL container should report healthy before the backend starts its database-dependent operations.

## Step 6: Access the API

```text
http://localhost:8080
```

Use Postman to register a user, authenticate, obtain a JWT, and test the protected endpoints.

---

# 22. Project Highlights

- Designed and developed a layered RESTful backend using Java 21 and Spring Boot.
- Implemented JWT authentication, BCrypt password hashing, and role-based authorization.
- Built a ticket lifecycle management system with audit history and comments.
- Integrated Ollama and Llama 3.2 for automatic ticket classification.
- Developed an AI chatbot with conversation history, intent processing, and knowledge-base retrieval.
- Implemented keyword-based RAG to provide relevant support information to the chatbot.
- Developed agent recommendations using workload and category expertise scoring.
- Implemented automated SLA monitoring, breach detection, and escalation.
- Created operational, historical, and SLA analytics APIs.
- Containerized the backend and MySQL database using Docker Compose.
- Tested REST APIs using Postman and backend business logic using JUnit and Mockito.

---

# 23. Future Enhancements

Potential future improvements include:

- Frontend dashboard using React.
- Email notifications.
- Vector embeddings and semantic RAG retrieval.
- Redis caching.
- Kafka-based asynchronous event processing.
- Microservices architecture.
- CI/CD automation.
- Production deployment and monitoring.

These are future possibilities and are not presented as completed features.

---

# 24. Author

**Shyam Amol Lade**

Java Backend Developer | Spring Boot | REST APIs | MySQL | Docker | AI Integration

GitHub: https://github.com/Shyampatil07

LinkedIn: https://www.linkedin.com/in/shyam-lade-patil-o7/

---

# 25. Project Summary

SupportAI demonstrates the development of a complete backend-oriented customer support application combining enterprise Java technologies with locally hosted AI.

The project covers authentication, authorization, REST API development, database design, ORM, business logic, ticket lifecycle management, SLA automation, analytics, agent assignment, chatbot integration, knowledge retrieval, testing, and Docker-based deployment.

It demonstrates practical experience with building and connecting multiple backend modules into one integrated application.
