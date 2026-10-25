# School Attendance Management & Anti-Fraud System

> Production-oriented school attendance management and anti-fraud system built with **Spring Boot 3 (Java 21 LTS)**, **React 18 + TypeScript + Vite**, and **PostgreSQL**.

[![Repository](https://img.shields.io/badge/GitHub-rabahalawadi%2Fschool--attendance--anti--fraud-blue?logo=github)](https://github.com/rabahalawadi/school-attendance-anti-fraud)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18%20%7C%20TypeScript-blue?logo=react)](https://react.dev/)

---

## 📌 Project Overview

This system provides complete, secure attendance tracking across classes, grades, and subjects, specifically engineered to **eliminate fraudulent attendance** (e.g. students registering for absent classmates).

Administrators have granular control over anti-fraud policies, including independent activation/deactivation, parameter adjustments, and class-level overrides.

### Core Features

1. **Academic Hierarchy & Student Management**:
   - `School → Academic Year → Grade → Class / Section → Subject → Student Enrollment`.
   - Teacher assignments to subjects and class sections.
   - Excel (`.xlsx`) and CSV bulk import with schema validation, conflict detection, and preview.
   - Manual grade-promotion workflow preserving historical records.

2. **Configurable Multi-Factor Anti-Fraud Engine**:
   - **Method A: Registered Classroom Device Verification** (School PC / kiosk credential validation).
   - **Method B: Rotating Dynamic QR Code** (Cryptographically signed, default 15s server-authoritative rotation).
   - **Method C: Ephemeral 6-Digit Challenge Code** (Displayed alongside QR, server-expired, strictly rate-limited).
   - **Method D: GPS Geofencing (Optional/Advisory)** (School perimeter bounding with accuracy limits).
   - **Initial 2-Week Manual Verification Window** (Configurable grace period for teacher review and validation).

3. **Offline Attendance & PWA Synchronization**:
   - Progressive Web App (PWA) with client-side caching (IndexedDB).
   - Teachers can conduct sessions offline and synchronize once connected.
   - Distinct statuses: `Recorded Offline`, `Sync Pending`, `Synchronized`, `Server Verified`.
   - Cryptographic idempotency keys prevent duplicates or replay attacks.

4. **Role-Based Access Control (RBAC)**:
   - **Administrator**: Comprehensive school setup, anti-fraud rules, imports, device management, audit logs, system-wide analytics.
   - **Teacher**: Lesson session management, real-time QR display, manual check-in override, student attendance review.
   - **Student**: View active eligible sessions, scan rotating QR code / enter challenge code, personal attendance history.

5. **Internationalization (i18n)**:
   - Full bilingual support: **English (LTR)** and **العربية (RTL)** with fluid layout flipping.

---

## 🏛️ System Architecture

```
                                      +-------------------------------+
                                      |         Classroom Screen       |
                                      |  (Dynamic Rotating QR & Code) |
                                      +---------------+---------------+
                                                      ^
                                                      |
+--------------------------+          +---------------+---------------+          +--------------------------+
|      Student Device      |  Scan /  |      Spring Boot API Monolith |  Sync /  |   Administrator & Teacher|
|   (Mobile / Tablet / PC) +--------->+ (Security, Fraud Engine, REST)+<---------+    Management Console    |
|       (React PWA)        |  Submit  +---------------+---------------+  Report  |       (React PWA)        |
+--------------------------+                          |                          +--------------------------+
                                                      v
                                      +---------------+---------------+
                                      |     PostgreSQL Database       |
                                      |  (Flyway Migrated & Audited)  |
                                      +-------------------------------+
```

---

## 🗄️ Database Schema & Entities

The relational database is managed via **Flyway migrations** and strictly normalizes the academic and security domains:

- `users` (UUID, username, password_hash via Argon2/BCrypt, role, status)
- `students` (UUID, user_id, student_number, full_name, status)
- `teachers` (UUID, user_id, employee_number, full_name, status)
- `academic_years`, `grades`, `sections`, `subjects`
- `student_enrollments`, `student_subject_enrollments`
- `teacher_assignments`
- `attendance_sessions` (academic_year, section, subject, teacher, status, policy_snapshot)
- `attendance_challenges` (ephemeral cryptographic QR & 6-digit challenge tokens)
- `attendance_records` (session_id, student_id, status, verification_status, idempotency_key)
- `verification_policies` (global and section-specific rules, parameters, required/optional)
- `verification_attempts` (audit log of every factor verification)
- `registered_devices` & `device_authorizations` (trusted school classroom hardware)
- `manual_reviews` & `attendance_correction_requests`
- `offline_sync_batches` & `audit_logs`

---

## 🚀 Getting Started

### Prerequisites

- **Java**: OpenJDK 21 LTS (`java -version`)
- **Maven**: 3.9+ or `./mvnw`
- **Node.js**: v18+ / v20+ / v22+ and npm
- **Database**: PostgreSQL 15+ (local or containerized)

### Local Development Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/rabahalawadi/school-attendance-anti-fraud.git
   cd school-attendance-anti-fraud
   ```

2. **Configure Environment**:
   ```bash
   cp .env.example .env
   # Update database credentials and JWT secret if needed
   ```

3. **Backend Setup**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   *Runs at `http://localhost:8080`, Flyway automatically applies all database migrations.*

4. **Frontend Setup**:
   ```bash
   cd ../frontend
   npm install
   npm run dev
   ```
   *Runs at `http://localhost:5173` with Vite HMR.*

---

## 📅 Phased Implementation Plan

- [x] **Phase 1: Architecture & Remote Repository Initialization**
  - Git repository created & published: [`rabahalawadi/school-attendance-anti-fraud`](https://github.com/rabahalawadi/school-attendance-anti-fraud)
  - Specification docs, ER models, security architecture, and schema definitions.
- [ ] **Phase 2: Backend Architecture & Database Migrations (Spring Boot 3 + Flyway)**
  - Entity models, Spring Security, JWT authentication, RBAC, Flyway migrations `V1__init_schema.sql`.
- [ ] **Phase 3: Academic Management & Excel Importer**
  - Grades, sections, subjects, student/teacher enrollment, Apache POI `.xlsx` import & validation.
- [ ] **Phase 4: Attendance Lifecycle & Multi-Factor Anti-Fraud Engine**
  - Session manager, dynamic QR rotation (15s), 6-digit code rotation, registered device & GPS verification, policy evaluator.
- [ ] **Phase 5: Offline PWA & Sync Protocol**
  - IndexedDB storage, idempotency hashing, sync batches, clock-drift detection.
- [ ] **Phase 6: Frontend Portals (Admin, Teacher, Student) & Bilingual UI**
  - Responsive dashboards, projector-friendly teacher QR screen, student scanner, English/Arabic RTL.
- [ ] **Phase 7: Analytics, Hardening, Automated Tests & Docker Deployment**
  - Daily/weekly/monthly metrics, CSV/Excel export, security & Playwright integration tests, Docker Compose.

---

## 🛡️ Anti-Fraud Limitations & Threat Model

While this system aggressively mitigates fraudulent check-ins, no electronic system can claim 100% fraud immunity without physical human oversight:
- **QR Sharing**: High-frequency rotation (15s) drastically reduces remote screenshot sharing over chat.
- **GPS Spoofing**: Marked advisory; verified against known classroom device hardware trust anchors.
- **Device Credentialing**: School kiosk mode binds attendance to physical classroom terminals.
- **Initial Verification Window**: Teachers manually confirm student presence during the first 14 days to establish baseline behavioral integrity.
