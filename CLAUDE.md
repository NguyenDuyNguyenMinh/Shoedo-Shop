# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ShoeDo Shop is a full-stack e-commerce platform for a shoe store. It uses a **Vue 3 SPA frontend** with a **Spring Boot REST API backend** and **SQL Server** database.

```
Shoedo-Shop/
├── FeShoeDo/FeShoeDo/     # Vue 3 frontend (src/ has components/, services/, stores/, router/)
└── ShoeDoShop/             # Spring Boot backend (src/main/java/poly/edu/)
    └── src/main/resources/static/anh/ + static/images/  # uploaded product images
```

## Commands

### Frontend (Vue 3 + Vite)
```bash
cd FeShoeDo/FeShoeDo
npm install
npm run dev      # Dev server at http://localhost:5173 (proxies /api, /images, /anh → :8080)
npm run build    # Production build
npm run preview
```

### Backend (Spring Boot)
```bash
cd ShoeDoShop
./mvnw spring-boot:run    # Runs on port 8080
./mvnw clean package      # Build JAR
./mvnw test               # Run unit tests
```

## Architecture

### Frontend
- **Vue 3** with Composition API (`<script setup>`)
- **Pinia** for auth and app state (`stores/auth.js`)
- **Vue Router** — role-based routing: `/customer/*` (CUSTOMER role) vs `/employee/*` (ADMIN/EMPLOYEE)
- **Axios** for API calls via Vite proxy to backend
- **Bootstrap 5** + icons for UI
- Components organized by role: `Customer/` (KH_*), `Employee/` (NV_*), `Shared/`

### Backend
- **Spring Boot 4** (Java 21), layered: `controller/` → `service/` → `dao/` (JPA repositories)
- **Security:** Spring Security with OAuth2 (Google login) + JWT cookie-based auth
- **DB:** SQL Server (`localhost:14314`, DB `ShoedoShop`) via `mssql-jdbc`
- **Key integrations:** VNPay (payments), Gmail SMTP (email), Coze/Groq API (chatbot)
- **Async:** `@EnableAsync` thread pool (core 5, max 20) for email sending
- **File uploads:** stored in `src/main/resources/static/images/`

### Key Entity Relationships
- `SanPham` (product) → `SanPhamChiTiet` (SKU/variant by size/color)
- `HoaDon` (order) → `HoaDonCT` (line items via `HoaDonCTId` composite key)
- `KhachHang` / `QuanTri` extend `Users`
- `GioHang` per user per SKU

### Database Schema
Managed via `ShoedoShop Final.sql` — run this to seed/reset the DB.

## Hard Constraints (Must Always Follow)

- **KHÔNG ĐƯỢC chỉnh sửa entity classes hoặc database schema.** Không thêm, sửa, xóa trường/property/relationship trong entity, không sửa đổi cấu trúc bảng, không tạo bảng mới. Luôn tuân theo ràng buộc này trong mọi thao tác.
- Nếu cần lưu dữ liệu mới, hãy sử dụng bảng/trường đã có sẵn hoặc tạo bảng mới hoàn toàn tách biệt mà không sửa entity hiện tại.

## Important Conventions

- **Roles:** `CUSTOMER`, `ADMIN`, `EMPLOYEE` — routing and API endpoints enforce these
- **PDF invoices:** generated server-side with `itext7-core` via `PdfService`
- **VNPay:** configured in sandbox mode — payment URLs use `vnpaySandboxurl` in `application.properties`
- **Image paths:** served as static resources; frontend references via `/anh/` and `/images/` proxied paths
- **OAuth2:** Google sign-in configured; `OAuth2Controller` handles the callback
- **Auth interceptor:** `AuthInterceptor` (configured in `WebConfig`) handles JWT cookie validation for protected endpoints
