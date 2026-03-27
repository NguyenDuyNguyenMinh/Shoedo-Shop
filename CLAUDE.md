# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

This repository contains **two applications** that are developed and run together:

- `FeShoeDo/FeShoeDo`: Vue 3 + Vite frontend (SPA)
- `ShoeDoShop`: Spring Boot backend (REST API + business logic + DB access)

The frontend talks to the backend through `/api` (and static image paths) using Vite proxy settings.

---

## Common Commands

### Frontend (`FeShoeDo/FeShoeDo`)

```bash
npm install
npm run dev
npm run build
npm run preview
```

Notes:
- Vite dev server runs on port `5173`.
- Proxy routes in `vite.config.js` forward `/api`, `/images`, `/anh` to `http://localhost:8080`.
- There is currently no dedicated frontend test or lint script in `package.json`.

### Backend (`ShoeDoShop`)

Use Maven wrapper from the `ShoeDoShop` directory.

**Windows:**
```bash
mvnw.cmd spring-boot:run
mvnw.cmd test
mvnw.cmd clean package
```

**macOS/Linux:**
```bash
./mvnw spring-boot:run
./mvnw test
./mvnw clean package
```

Run a single backend test class:

**Windows:**
```bash
mvnw.cmd -Dtest=ShoeDoShopApplicationTests test
```

**macOS/Linux:**
```bash
./mvnw -Dtest=ShoeDoShopApplicationTests test
```

---

## High-Level Architecture

## 1) Frontend architecture (Vue)

- Entry: `src/main.js` mounts Vue app, registers Pinia + Vue Router, loads Bootstrap/FontAwesome.
- Root app: `src/App.vue` renders only `<router-view />`.
- Routing: `src/router/index.js`
  - Route groups are split by role and domain:
    - customer routes (`/customer/...`)
    - employee/admin routes (`/employee/...`)
    - auth route (`/auth/login`)
  - Global navigation guard enforces `requiresAuth`, `requiresGuest`, and role constraints.
  - Guard initializes auth store on first navigation.
- State/auth: `src/stores/auth.js`
  - Central auth/session state (user, token, cart count, init state).
  - Startup flow tries token -> session check -> cookie auto-login.
  - Uses backend endpoints under `/api/auth/*`.
- API layer: `src/services/api.js`
  - Centralized Axios client with `baseURL: /api` and `withCredentials: true`.
  - Request interceptor attaches bearer token from localStorage.
  - Response interceptor handles `401` by clearing local auth and redirecting to login.
  - Exposes grouped API methods for auth, customer cart/checkout/profile/orders, employee product/order/user management, and statistics.

## 2) Backend architecture (Spring Boot)

- Entry: `poly.edu.ShoeDoShopApplication`
  - Enabled with `@EnableScheduling` and `@EnableAsync`.
- Typical layering pattern:
  - `controller/*`: REST endpoints
  - `service/*`: business logic and orchestration
  - `dao/*`: Spring Data JPA repositories
  - `entity/*`: JPA entities
  - `dto/*`: request/response transfer objects
  - `config/*`: app configuration (CORS, VNPay, etc.)

Important backend domains visible in controllers/services:

- Authentication/session (`AuthController`, `AuthService`): login/logout, current user, password/reset flows, session and cookie-based auto-login.
- Customer cart + checkout (`GioHangController`, `GioHangService`): cart CRUD, stock validation, checkout creation, order line generation.
- Customer profile/address (`ProfileController`, `ProfileService`): profile updates, address management, password changes.
- Employee statistics (`ThongKeController`, `ThongKeService`): dashboard/timeseries/category/top-customer/product analytics endpoints.
- Payment integration (`VNPayController`, `VNPayConfig`): VNPay URL/signature configuration and payment flow.

## 3) Frontend ↔ Backend integration expectations

- Frontend expects backend at `localhost:8080` during development (via Vite proxy).
- Backend CORS is configured to allow `http://localhost:5173` (also 5174/8080/4200 in `WebConfig`).
- Image resources are served via Spring static/resource handler mapping (`/images/**`).

---

## Configuration Notes

- Backend runtime configuration is in `ShoeDoShop/src/main/resources/application.properties`.
- The app depends on SQL Server (`spring.datasource.*`) and external services (mail, Google OAuth2, VNPay, chatbot token settings).
- Before sharing or deploying, replace local/development secrets and credentials with environment-specific secure configuration.
