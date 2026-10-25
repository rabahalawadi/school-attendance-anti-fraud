# VeriAttend

> Production-grade school attendance management and multi-factor anti-fraud platform built with **Spring Boot 3 (Java 21 LTS)**, **React 18 + TypeScript + Vite**, and **PostgreSQL**.

[![Repository](https://img.shields.io/badge/GitHub-rabahalawadi%2Fschool--attendance--anti--fraud-blue?logo=github)](https://github.com/rabahalawadi/school-attendance-anti-fraud)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18%20%7C%20TypeScript-blue?logo=react)](https://react.dev/)

---

## 📌 Project Overview

**VeriAttend** is an intelligent attendance tracking and anti-fraud platform designed to prevent students from recording attendance for absent peers. It combines cryptographically signed dynamic QR code rotation, synchronized 6-digit challenge codes, classroom device authorization, GPS geofencing, and a 14-day grace window for manual teacher confirmation.

Administrators have granular control over anti-fraud policies, including independent activation/deactivation, parameter adjustments, and class-level overrides.

### Core Features

1. **Academic Hierarchy & Student Management**:
   - `School → Academic Year → Grade → Class / Section → Subject → Student Enrollment`.
   - Teacher assignments to subjects and class sections.
   - Excel (`.xlsx`) and CSV bulk import with schema validation, conflict detection, formula injection defense, and preview staging.
   - One-click sample dataset generation for instant testing without opening spreadsheets.
   - Downloadable Excel (`.xlsx`) and CSV (`.csv`) import templates.
   - Manual grade-promotion workflow preserving historical records.

2. **Configurable Multi-Factor Anti-Fraud Engine**:
   - **Method A: Registered Classroom Device Verification** (School PC / kiosk credential validation).
   - **Method B: Rotating Dynamic QR Code** (Cryptographically signed, default 15s server-authoritative rotation).
   - **Method C: Ephemeral 6-Digit Challenge Code** (Displayed alongside QR, server-expired, strictly rate-limited).
   - **Method D: GPS Geofencing (Optional/Advisory)** (School perimeter bounding with accuracy limits).
   - **Initial 14-Day Manual Verification Window** (Configurable grace period for teacher review and validation).

3. **Offline Attendance & PWA Synchronization**:
   - Progressive Web App (PWA) with client-side caching (IndexedDB).
   - Teachers can conduct sessions offline and synchronize once connected.
   - Distinct statuses: `Recorded Offline`, `Sync Pending`, `Synchronized`, `Server Verified`.
   - Cryptographic idempotency keys prevent duplicates or replay attacks.

4. **Zero-Friction Testing & Demo Suite**:
   - Persistent top **Demo Bar** with 1-click persona switching (Admin, Teacher Ahmad, Student Tariq, Student Layla).
   - Pre-seeded active demo session with live challenges (`c0000000-0000-0000-0000-000000000001`).
   - "⚡ 1-Click Fill Demo Code" on student portal for instant PC testing without camera.
   - "Simulate Check-In" on projector HUD for instant roster populating.

5. **Internationalization (i18n)**:
   - Full bilingual support: **English (LTR)** and **العربية (RTL)** with fluid layout flipping.

---

## 🔑 Demo & Test Credentials

All accounts are pre-seeded with the uniform password: `Password@123456`

| Persona | Username | Role | Scope |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin` | `ADMIN` | Full school administration & policy control |
| **Teacher Ahmad** | `teacher.ahmad` | `TEACHER` | Grade 10-A Computer Science (CS101) |
| **Teacher Sarah** | `teacher.sarah` | `TEACHER` | Advanced Mathematics & Applied Physics |
| **Student Tariq** | `student.tariq` | `STUDENT` | Grade 10-A (Student ID: `STU-1001`) |
| **Student Layla** | `student.layla` | `STUDENT` | Grade 10-A (Student ID: `STU-1002`) |

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

## 🗄️ Database Migrations (Flyway)

1. `V1__init_schema.sql` (21 tables covering users, academic hierarchy, sessions, challenges, records, policies, devices, sync batches, audit logs).
2. `V2__seed_initial_data.sql` (Academic year 2026/2027, Grades 10-12, Sections A-B, Subjects, Teachers, Students, Enrollments, Global Anti-Fraud Policy).
3. `V3__add_demo_session.sql` (Active open session `c0000000-0000-0000-0000-000000000001` with rotating challenge codes for Grade 10-A CS101).

---

## 🚀 Running Locally

### Backend (Spring Boot 3 + Java 21)
```bash
cd backend
mvn spring-boot:run
```
- API Base URL: `http://localhost:8080`
- Swagger / OpenAPI Docs: `http://localhost:8080/swagger-ui.html`
- Health Endpoint: `http://localhost:8080/actuator/health`

### Frontend (React 18 + TypeScript + Vite)
```bash
cd frontend
npm install
npm run dev
```
- Web Application: `http://localhost:5173`
