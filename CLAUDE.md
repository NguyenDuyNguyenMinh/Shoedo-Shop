# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ShoeDoShop** is a Vietnamese multi-role e-commerce platform for selling shoes. It has two sibling applications:
- `ShoeDoShop/` — Spring Boot REST API backend (Java 21, Spring Boot 4, SQL Server)
- `FeShoeDo/FeShoeDo/` — Vue 3 SPA frontend (Vite, Pinia, Vue Router, Bootstrap 5)

> Note: `FeShoeDo/` (outer) is a Maven archetype artifact; the actual Vue project root is `FeShoeDo/FeShoeDo/` where `package.json` and `src/` live.

---

## Development Commands

### Backend (`ShoeDoShop/`)
```bash
cd ShoeDoShop
./mvnw spring-boot:run       # Run the app
./mvnw package               # Build JAR
./mvnw test                  # Run all tests
./mvnw test -Dtest=ClassName              # Run one test class
./mvnw test -Dtest=ClassName#methodName  # Run one test method
./mvnw clean                 # Clean build artifacts
```

### Frontend (`FeShoeDo/FeShoeDo/`)
```bash
cd FeShoeDo/FeShoeDo
npm install                  # Install dependencies
npm run dev                  # Dev server at http://localhost:5173 (proxies /api → :8080)
npm run build                # Production build
npm run preview              # Preview production build
```

**No lint or test framework** is configured in the frontend. The backend has only a placeholder test.

---

## Running the Full Stack

Both servers must run simultaneously:
1. Start Spring Boot: `cd ShoeDoShop && ./mvnw spring-boot:run` (port `:8080`)
2. Start Vite: `cd FeShoeDo/FeShoeDo && npm run dev` (port `:5173`)

---

## Architecture

### Backend — Layered Architecture (`ShoeDoShop/src/main/java/poly/edu/`)

| Layer | Purpose |
|---|---|
| `config/` | Spring Security, CORS, VNPay, OAuth2, static resource serving |
| `controller/` | Thin REST controllers — delegate all logic to services |
| `service/` | Business logic, transaction management |
| `dao/` | Spring Data JPA repositories (extend `JpaRepository`) |
| `entity/` | JPA entities (Hibernate mappings to SQL Server tables) |
| `dto/` | Flat API response DTOs |

**Standard response format**: All endpoints return `ApiResponse<T>` → `{ success, message, data }`.

**Auth**: Session-based (`HttpSession`) with optional "Remember Me" cookie (Base64 `userId:email`). CSRF is disabled. Role detection: `Users` → 1:1 → `KhachHang` (CUSTOMER) or `QuanTri` (ADMIN/role=true, EMPLOYEE/role=false).

**Data flow**: Request → `SecurityConfig` → Controller → Service → DAO → SQL Server

### Frontend — Vue 3 SPA (`FeShoeDo/FeShoeDo/src/`)

- `services/api.js` — Axios client for all API calls
- `stores/auth.js` — Pinia store for auth/session state
- `router/index.js` — Route definitions with role-based guards
- `components/Customer/` — Customer-facing views (browse, cart, checkout, orders, profile)
- `components/Employee/` — Admin/Employee views (dashboard, product/order/user management, stock, promotions)
- `components/Shared/` — Navbar, sidebar, chatbot, toast

Public routes: `/`, `/customer/index`, `/customer/detail-product/:id?`, `/customer/chinhsach`, `/customer/sanpham`, `/auth/login`.

---

## Key Patterns

- **OTP flow**: In-memory `Map` with 10-min expiry, cleaned by `@Scheduled` every 60s.
- **VNPay**: Sandbox payment gateway. IPN/return URLs point to `localhost:8080`. Toggle via `vnpay.enabled=true`.
- **OAuth2**: Google OAuth2. Post-auth redirect goes to `http://localhost:5173` with user data in query params.
- **Image uploads**: Stored in `src/main/resources/static/images/`. Served via Spring `ResourceHandler` at `/images/**`.
- **PDF export**: Using iText 7 via `PdfService`.
- **Email**: Async via `@Async` thread pool (Gmail SMTP).
- **AI Chatbot**: Integrated via Coze API and Groq API.

---

## Prerequisites

- **SQL Server** at `localhost:1433` with the `ShoedoShop` database initialized from `ShoedoShop Final.sql`
- **Java 21**
- **Credentials** (DB, VNPay, email, OAuth client ID, Coze/Groq tokens) are stored in `application.properties` — do not commit sensitive values.
