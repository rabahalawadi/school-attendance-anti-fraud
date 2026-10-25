# System Architecture & Technical Design

## 1. Modular Monolith Architecture

The system is structured as a modular monolith in Spring Boot 3.3 (Java 21 LTS), balancing high maintainability with clean separation of domain boundaries.

```
com.school.attendance
├── config/                # Security, WebMvc, Flyway, OpenAPI, CORS configuration
├── security/              # Spring Security, JWT filters, Argon2 password encoder
├── academic/              # Academic year, Grade, Section, Subject, Enrollment
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
├── user/                  # User, Teacher, Student accounts and RBAC
├── attendance/            # Sessions, Challenges, Records, Lifecycle management
├── fraud/                 # Anti-fraud engine, Policy evaluator, Verifiers
│   ├── engine/            # Deterministic multi-factor verification pipeline
│   ├── verifier/          # DeviceVerifier, QrVerifier, CodeVerifier, GpsVerifier
│   └── policy/            # Policy evaluation models & snapshots
├── device/                # Registered classroom devices & authorizations
├── offline/               # Offline sync batches, conflict resolution, idempotency
├── report/                # Analytics engine, daily/weekly/monthly aggregations, Excel/CSV exports
└── common/                # Exceptions, Error responses, Audit logging, Utilities
```

---

## 2. Anti-Fraud Verification Pipeline

When a student submits an attendance check-in, the request enters the `AntiFraudEngine`:

```
Student Registration Request
          │
          ▼
┌─────────────────────────┐
│ Fetch Session Snapshot  │ (Verification policy frozen at session start)
└─────────┬───────────────┘
          │
          ├──> 1. Registered Device Check (Is device required? Is kiosk token valid?)
          │
          ├──> 2. Rotating QR Code Check  (Is token valid, active, matching session, unexpired?)
          │
          ├──> 3. 6-Digit Challenge Check (Is code valid for current window?)
          │
          └──> 4. GPS Geofence Check     (Is coordinate within school radius? Accuracy threshold met?)
          │
          ▼
┌─────────────────────────┐
│ Deterministic Evaluator │
└─────────┬───────────────┘
          │
   ┌──────┴───────────────────────────┐
   │ Check Required vs Optional Rules │
   └──────┬───────────────────────────┘
          │
   ┌──────┴───────────────┬─────────────────────────┬──────────────────────┐
   ▼                      ▼                         ▼                      ▼
All Required Passed    Initial 2-Week Window    Required Failed        Fraud Detected
   │                      │                         │                      │
   ▼                      ▼                         ▼                      ▼
[VERIFIED]            [PENDING_CONFIRMATION]      [REJECTED]             [FLAGGED_SUSPICIOUS]
```

### Deterministic Policy Rules:
1. **Disabled Methods**: Completely bypassed; cannot cause failure or blocking.
2. **Optional Methods**: Contributes to audit risk score; failures do not block check-in.
3. **Required Methods**: Must strictly succeed. Any failure results in `REJECTED` or `FLAGGED_FOR_REVIEW` based on configured policy.
4. **Manual Review Exception**: When enabled (such as during the first 14 days), records transition to `PENDING_CONFIRMATION` for teacher review rather than immediate rejection.

---

## 3. Ephemeral Rotating Tokens (QR & 6-Digit)

- **QR Token**: Generated using cryptographically secure random tokens (`SecureRandom`). Stored server-side in `attendance_challenges` with `expires_at = now() + 15s`.
- **6-Digit Code**: 6-digit numeric string generated with `SecureRandom` (`100000` to `999999`), valid for 30s. Rate limited at 5 failed guesses per IP/account/session with exponential backoff.
- **Clock Authority**: The backend database clock is the sole source of truth. Client device time is ignored.

---

## 4. Offline Attendance & Synchronization Protocol

1. **Roster Download**: Teacher loads class roster on PWA when online. Roster is cached in IndexedDB.
2. **Offline Session**: When internet drops, the teacher initiates an offline session.
3. **Local Recording**: Attendances are marked locally with a client-generated UUID `idempotency_key`, `device_recorded_at`, and `sync_status = RECORDED_OFFLINE`.
4. **Batch Synchronization**: When connectivity resumes, the PWA submits `POST /api/v1/offline-sync/batches`.
5. **Conflict Resolution**:
   - The server verifies that no duplicate record exists for `(session_id, student_id)`.
   - If an online record already exists, newer verified records take precedence, and discrepancies are logged as manual review items.
   - Idempotency keys prevent duplicate insertions if network retries occur.
