# Complete Setup & Production Deployment Guide

## 1. Prerequisites

- **Java**: OpenJDK 21 LTS (`java -version`)
- **Maven**: 3.9+ or use Apache Maven
- **Node.js**: v18+ / v20+ / v22+ and npm
- **Database**: PostgreSQL 15+ (local or Docker)

---

## 2. Local Environment Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/rabahalawadi/school-attendance-anti-fraud.git
   cd school-attendance-anti-fraud
   ```

2. **Environment Variables**:
   Copy `.env.example` to `.env` and configure:
   ```bash
   cp .env.example .env
   ```

3. **Running Database & Migrations**:
   If using Docker Compose:
   ```bash
   docker compose -f docker/docker-compose.yml up -d postgres
   ```
   *Flyway automatically applies `V1__init_schema.sql` and `V2__seed_initial_data.sql`.*

4. **Running the Spring Boot Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   *Runs at `http://localhost:8080`. Swagger UI is accessible at `http://localhost:8080/swagger-ui.html`.*

5. **Running the React Frontend**:
   ```bash
   cd ../frontend
   npm install
   npm run dev
   ```
   *Runs at `http://localhost:5173` with full Vite HMR and API proxy.*

---

## 3. Seed Accounts for Testing

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Password@123456` | Full school, policy, and import access |
| **Teacher** | `teacher.ahmad` | `Password@123456` | Assigned to Grade 10 Section A (CS101) |
| **Teacher** | `teacher.sarah` | `Password@123456` | Assigned to Grade 11 |
| **Student** | `student.tariq` | `Password@123456` | Enrolled in Grade 10 Section A |
| **Student** | `student.layla` | `Password@123456` | Enrolled in Grade 10 Section A |

---

## 4. Production Docker Deployment

To build and run all services (PostgreSQL, Spring Boot Backend, Nginx + React Frontend):
```bash
docker compose -f docker/docker-compose.yml up --build -d
```
All containers run on an isolated network with healthy database wait conditions.
