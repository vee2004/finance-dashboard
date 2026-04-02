# Finance Dashboard — Backend API

An enterprise-ready Spring Boot REST API for a finance dashboard featuring **role-based access control**, **JWT authentication**, **financial record management**, and **analytics dashboards**.

---

## 📋 Project Overview

The Finance Dashboard backend provides a secure, well-structured API to:
- Manage users and their roles (Viewer, Analyst, Admin).
- Track financial records (income/expense) with custom filtering and pagination.
- Deliver dashboard analytics: comprehensive summaries, category breakdowns, and monthly trends.
- Enforce strict role-based access control on every endpoint.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Framework** | Spring Boot 3.2.x |
| **Language** | Java 17 |
| **ORM** | Spring Data JPA + Hibernate |
| **Database** | PostgreSQL (Production) / H2 (Testing) |
| **Security** | Spring Security 6 + JWT (jjwt 0.12) |
| **API Docs** | springdoc-openapi 2.3 (Swagger UI) |
| **Build** | Maven |

---

## 🏗 Why This Design?

* **Spring Boot:** Provides a mature, production-ready ecosystem with auto-configuration, making it ideal for rapidly developing secure REST APIs without boilerplate.
* **Layered Architecture:** Splitting code strictly into Controllers (Routing), Services (Business Logic), and Repositories (Data Access) ensures a clear separation of concerns, making the API independently testable and highly maintainable.
* **JWT Authentication:** A stateless authentication mechanism that allows the API to scale horizontally without the overhead of tracking server-side sessions.
* **PostgreSQL:** A robust, ACID-compliant relational DB that excels at handling structured financial data and complex aggregation queries for dashboards.

---

## ⚙️ Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 1. Database Configuration
Create a PostgreSQL database named `finance_dashboard`:
```sql
CREATE DATABASE finance_dashboard;
```
Configure your specific connection credentials in `src/main/resources/application.yml`.

### 2. Build & Run
```bash
# Build the project
mvn clean install

# Run the app
mvn spring-boot:run
```
The application will launch on **http://localhost:8080**.

### 3. Default Admin Creation
Upon the first startup, a default admin is seeded automatically:
- **Username:** `admin`
- **Password:** `admin123` *(Note: Change immediately via DB in production)*

---

## 🔒 Role-Based Access Control

| Feature | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| Auth Login | ✅ | ✅ | ✅ |
| Auth Register | ❌ | ❌ | ✅ |
| Access all Records | ✅ | ✅ | ✅ |
| Add / Update Record| ❌ | ✅ | ✅ |
| Delete Record (Soft)| ❌ | ❌ | ✅ |
| Dashboard Summaries| ✅ | ✅ | ✅ |
| Dashboard Monthly | ❌ | ✅ | ✅ |

---

## ✔️ Validation & Business Rules

To protect data integrity, strict validation occurs at the API boundary:
* **Amount must be positive:** Financial transaction values cannot be negative. The actual direction of cash flow is purely dictated by the `type` field.
* **Date cannot be in the future:** Prevents the system from tracking speculative unverified data rather than realized activity.
* **Type must be `INCOME` or `EXPENSE`:** Enforced via enums to ensure query predictability and strict mathematical logic on aggregates.
* **Category cannot be empty:** Critical for accurate dashboard grouping and categorical metrics.

---

## 🗄️ Database Design

### Schema Overview
* **`users` table:** Tracks identities, roles, and status mappings.
* **`financial_records` table:** Stores transaction amounts, explicitly mapped to a creator ID, featuring boolean soft deletes.

### Indexing Strategy
To ensure the dashboard queries remain extremely performant as hundreds of thousands of records are inserted, specific compound indexes are utilized:
1. **Index on `(type, category, date)`:** Drastically reduces total row scans when calculating specific monthly analytics and filtering category breakdowns.
2. **Index on Foreign Key `(created_by_id)`:** Accelerates joins for user-scoped queries and historical audit tracking.

---

## 🧪 Edge Cases & Error Handling

* **Unauthorized / Invalid Tokens:** Handled securely via a centralized `JwtAuthFilter`. Tampered or expired tokens gracefully return `401 Unauthorized` without crashing execution flows.
* **Invalid Input:** Standardized `@ControllerAdvice` grabs `MethodArgumentNotValidException` to yield descriptive, field-level `400 Bad Request` messages to the client.
* **Deleting Already Deleted Records:** Soft-deleted queries ensure deleting an already `deleted=true` row safely returns `404 Not Found` rather than causing an unexpected state transition or DB error.
* **Empty Dashboard Queries:** Returns clean `0.00` fallback totals rather than throwing `NullPointerExceptions` during math aggregation.

---

## 🚀 Scalability Considerations

While a monolith repository perfectly serves the current domain requirements, the following architectural steps would be prioritized to scale:
* **Analytics Caching:** Moving dashboard analytical queries (e.g., historical monthly / category summaries) to a Redis cache layer, since historical closed months rarely alter.
* **Efficient Memory Usage:** List endpoints enforce native Spring Data `Pageable` limits strictness to ensure memory safety on the JVM.
* **Microservices Evolution:** If User Management grows to encompass massive RBAC rules (e.g. multi-tenant organizations) and Analytics becomes heavily loaded, the repository seamlessly splits into an Auth Service, a Core Record Service, and a materialized View/Analytics Service.

---

## 📖 Interactive API Docs (Swagger)

Run the application and visit **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** to access the UI.

1. Hit the `POST /api/auth/login` endpoint with your credentials to obtain a token.
2. Click the **Authorize 🔒** button at the top of the UI.
3. Provide the token using the format: `Bearer <your_copied_token>`
4. You can now execute, inspect models, and test all protected endpoints directly from the interface.

---

## 📌 Assumptions

1. **Simplified Visibility:** For this assignment scope, all user roles can view all active records across the system. In a real-world multi-tenant production system, visibility would be tightly scoped (filtered in the query layer) per User ID, Tenant ID, or structural organization.
2. **Immutability:** Financial amounts should practically be immutable after long periods for accounting compliance, though for simple testing, `PUT` allows full ad-hoc updates.
3. User role escalation is not self-service; only Admin accounts can natively manage identities.
