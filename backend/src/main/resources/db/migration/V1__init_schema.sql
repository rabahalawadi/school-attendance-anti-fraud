-- ==============================================================================
-- V1: Core Schema for School Attendance Management & Anti-Fraud System
-- ==============================================================================

-- 1. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    account_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- 2. Students Table
CREATE TABLE students (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    student_number VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_student_number ON students(student_number);
CREATE INDEX idx_students_user_id ON students(user_id);

-- 3. Teachers Table
CREATE TABLE teachers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    employee_number VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teachers_employee_number ON teachers(employee_number);
CREATE INDEX idx_teachers_user_id ON teachers(user_id);

-- 4. Academic Years Table
CREATE TABLE academic_years (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Grades Table
CREATE TABLE grades (
    id UUID PRIMARY KEY,
    academic_year_id UUID NOT NULL REFERENCES academic_years(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    level INT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_grade_year_name UNIQUE (academic_year_id, name)
);

-- 6. Sections Table
CREATE TABLE sections (
    id UUID PRIMARY KEY,
    grade_id UUID NOT NULL REFERENCES grades(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    capacity INT DEFAULT 30,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_section_grade_name UNIQUE (grade_id, name)
);

-- 7. Subjects Table
CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Student Enrollments Table
CREATE TABLE student_enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    academic_year_id UUID NOT NULL REFERENCES academic_years(id) ON DELETE RESTRICT,
    grade_id UUID NOT NULL REFERENCES grades(id) ON DELETE RESTRICT,
    section_id UUID NOT NULL REFERENCES sections(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'ENROLLED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_year_enrollment UNIQUE (student_id, academic_year_id)
);

CREATE INDEX idx_enrollment_section ON student_enrollments(section_id);

-- 9. Student Subject Enrollments Table
CREATE TABLE student_subject_enrollments (
    id UUID PRIMARY KEY,
    student_enrollment_id UUID NOT NULL REFERENCES student_enrollments(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT,
    enrollment_status VARCHAR(32) NOT NULL DEFAULT 'ENROLLED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_subject UNIQUE (student_enrollment_id, subject_id)
);

-- 10. Teacher Assignments Table
CREATE TABLE teacher_assignments (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES teachers(id) ON DELETE RESTRICT,
    section_id UUID NOT NULL REFERENCES sections(id) ON DELETE RESTRICT,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT,
    academic_year_id UUID NOT NULL REFERENCES academic_years(id) ON DELETE RESTRICT,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_teacher_section_subject UNIQUE (teacher_id, section_id, subject_id, academic_year_id)
);

-- 11. Registered Devices Table (Method A)
CREATE TABLE registered_devices (
    id UUID PRIMARY KEY,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(64) NOT NULL,
    classroom VARCHAR(100) NOT NULL,
    credential_reference VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'AUTHORIZED',
    registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_registered_devices_status ON registered_devices(status);

-- 12. Device Authorizations Table
CREATE TABLE device_authorizations (
    id UUID PRIMARY KEY,
    device_id UUID NOT NULL REFERENCES registered_devices(id) ON DELETE CASCADE,
    authorized_scope VARCHAR(64) NOT NULL,
    scope_id UUID,
    authorized_by UUID REFERENCES users(id),
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 13. Verification Policies Table
CREATE TABLE verification_policies (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    scope_type VARCHAR(64) NOT NULL,
    scope_id UUID,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    policy_configuration TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL,
    effective_until TIMESTAMP WITH TIME ZONE,
    updated_by UUID REFERENCES users(id),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_policies_scope ON verification_policies(scope_type, scope_id, enabled);

-- 14. Attendance Sessions Table
CREATE TABLE attendance_sessions (
    id UUID PRIMARY KEY,
    academic_year_id UUID NOT NULL REFERENCES academic_years(id) ON DELETE RESTRICT,
    section_id UUID NOT NULL REFERENCES sections(id) ON DELETE RESTRICT,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT,
    teacher_id UUID NOT NULL REFERENCES teachers(id) ON DELETE RESTRICT,
    scheduled_start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
    policy_snapshot TEXT NOT NULL,
    session_secret VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_teacher ON attendance_sessions(teacher_id);
CREATE INDEX idx_sessions_section ON attendance_sessions(section_id);
CREATE INDEX idx_sessions_status ON attendance_sessions(status);

-- 15. Attendance Challenges Table (Ephemeral Dynamic QR & 6-Digit)
CREATE TABLE attendance_challenges (
    id UUID PRIMARY KEY,
    attendance_session_id UUID NOT NULL REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    challenge_type VARCHAR(32) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_challenges_session_active ON attendance_challenges(attendance_session_id, challenge_type, status, expires_at);

-- 16. Attendance Records Table
CREATE TABLE attendance_records (
    id UUID PRIMARY KEY,
    attendance_session_id UUID NOT NULL REFERENCES attendance_sessions(id) ON DELETE RESTRICT,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    status VARCHAR(32) NOT NULL,
    verification_status VARCHAR(32) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    server_received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    confirmed_by UUID REFERENCES users(id),
    confirmed_at TIMESTAMP WITH TIME ZONE,
    correction_reason TEXT,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_session_student UNIQUE (attendance_session_id, student_id)
);

CREATE INDEX idx_attendance_session ON attendance_records(attendance_session_id);
CREATE INDEX idx_attendance_student ON attendance_records(student_id);
CREATE INDEX idx_attendance_status ON attendance_records(status);

-- 17. Verification Attempts Table (Audit Log for Every Check-in Factor)
CREATE TABLE verification_attempts (
    id UUID PRIMARY KEY,
    attendance_session_id UUID NOT NULL REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    method VARCHAR(32) NOT NULL,
    result VARCHAR(32) NOT NULL,
    reason_code VARCHAR(64),
    risk_level VARCHAR(32) NOT NULL DEFAULT 'LOW',
    attempted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    device_reference VARCHAR(255)
);

CREATE INDEX idx_verif_attempts_session ON verification_attempts(attendance_session_id);
CREATE INDEX idx_verif_attempts_student ON verification_attempts(student_id);

-- 18. Manual Reviews Table (Initial 2-week window & flagged items)
CREATE TABLE manual_reviews (
    id UUID PRIMARY KEY,
    attendance_record_id UUID NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
    reviewer_id UUID NOT NULL REFERENCES users(id),
    decision VARCHAR(32) NOT NULL,
    reason TEXT NOT NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 19. Attendance Correction Requests Table
CREATE TABLE attendance_correction_requests (
    id UUID PRIMARY KEY,
    attendance_record_id UUID NOT NULL REFERENCES attendance_records(id) ON DELETE CASCADE,
    requested_by UUID NOT NULL REFERENCES users(id),
    proposed_status VARCHAR(32) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 20. Offline Sync Batches Table
CREATE TABLE offline_sync_batches (
    id UUID PRIMARY KEY,
    source_device_id VARCHAR(100) NOT NULL,
    batch_identifier VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    synchronization_status VARCHAR(32) NOT NULL,
    result_summary TEXT
);

-- 21. Audit Logs Table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    request_id VARCHAR(100),
    sanitized_metadata TEXT
);

CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
