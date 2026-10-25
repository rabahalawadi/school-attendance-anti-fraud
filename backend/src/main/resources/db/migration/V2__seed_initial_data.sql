-- ==============================================================================
-- V2: Seed Initial School Data and Verification Policies
-- ==============================================================================

-- 1. Default Users (BCrypt hash for "Admin@123456", "Teacher@123456", "Student@123456")
-- Password for all seed accounts: "Password@123456"
-- BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO users (id, username, password_hash, role, account_status, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000002', 'teacher.ahmad', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'TEACHER', 'ACTIVE', CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000003', 'teacher.sarah', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'TEACHER', 'ACTIVE', CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000004', 'student.tariq', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000005', 'student.layla', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP);

-- 2. Teachers
INSERT INTO teachers (id, user_id, employee_number, full_name, contact_email, status)
VALUES 
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'EMP-2026-001', 'Dr. Ahmad Yousef', 'ahmad.yousef@school.edu', 'ACTIVE'),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 'EMP-2026-002', 'Ms. Sarah Jenkins', 'sarah.jenkins@school.edu', 'ACTIVE');

-- 3. Students
INSERT INTO students (id, user_id, student_number, full_name, contact_email, contact_phone, status)
VALUES 
    ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000004', 'STU-1001', 'Tariq Al-Mansoor', 'tariq@student.school.edu', '+962791112233', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000005', 'STU-1002', 'Layla Al-Khatib', 'layla@student.school.edu', '+962792223344', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000003', NULL, 'STU-1003', 'Omar Salem', 'omar@student.school.edu', '+962793334455', 'ACTIVE'),
    ('20000000-0000-0000-0000-000000000004', NULL, 'STU-1004', 'Nour Haddad', 'nour@student.school.edu', '+962794445566', 'ACTIVE');

-- 4. Academic Year
INSERT INTO academic_years (id, name, start_date, end_date, status)
VALUES 
    ('30000000-0000-0000-0000-000000000001', 'Academic Year 2026/2027', '2026-09-01', '2027-06-30', 'ACTIVE');

-- 5. Grades
INSERT INTO grades (id, academic_year_id, name, level, status)
VALUES 
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Grade 10', 10, 'ACTIVE'),
    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'Grade 11', 11, 'ACTIVE'),
    ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'Grade 12', 12, 'ACTIVE');

-- 6. Sections
INSERT INTO sections (id, grade_id, name, capacity, status)
VALUES 
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'Section A', 30, 'ACTIVE'),
    ('50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'Section B', 30, 'ACTIVE');

-- 7. Subjects
INSERT INTO subjects (id, code, name, status)
VALUES 
    ('60000000-0000-0000-0000-000000000001', 'CS101', 'Computer Science & Software', 'ACTIVE'),
    ('60000000-0000-0000-0000-000000000002', 'MATH101', 'Advanced Mathematics', 'ACTIVE'),
    ('60000000-0000-0000-0000-000000000003', 'PHYS101', 'Applied Physics', 'ACTIVE');

-- 8. Student Enrollments (Grade 10 Section A)
INSERT INTO student_enrollments (id, student_id, academic_year_id, grade_id, section_id, start_date, status)
VALUES 
    ('70000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '2026-09-01', 'ENROLLED'),
    ('70000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '2026-09-01', 'ENROLLED'),
    ('70000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '2026-09-01', 'ENROLLED'),
    ('70000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '2026-09-01', 'ENROLLED');

-- 9. Student Subject Enrollments
INSERT INTO student_subject_enrollments (id, student_enrollment_id, subject_id, enrollment_status)
VALUES 
    ('80000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'ENROLLED'),
    ('80000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', 'ENROLLED'),
    ('80000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', 'ENROLLED'),
    ('80000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000004', '60000000-0000-0000-0000-000000000001', 'ENROLLED');

-- 10. Teacher Assignments
INSERT INTO teacher_assignments (id, teacher_id, section_id, subject_id, academic_year_id, status)
VALUES 
    ('90000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'ACTIVE');

-- 11. Registered Classroom Device (Method A Seed)
INSERT INTO registered_devices (id, device_name, device_type, classroom, credential_reference, status, registered_at)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'Lab-1-Station-01', 'CLASSROOM_PC', 'Computer Lab 1', 'KIOSK-LAB1-PC01-CERT-2026', 'AUTHORIZED', CURRENT_TIMESTAMP);

-- 12. Verification Policy (Global Default Anti-Fraud Configuration)
INSERT INTO verification_policies (id, name, scope_type, scope_id, enabled, policy_configuration, version, effective_from)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'Standard Global Anti-Fraud Policy',
    'GLOBAL',
    NULL,
    TRUE,
    '{"deviceCheck":{"enabled":true,"required":false},"qrCheck":{"enabled":true,"required":true,"rotationSeconds":15},"sixDigitCheck":{"enabled":true,"required":false,"expirationSeconds":30},"gpsCheck":{"enabled":true,"required":false,"radiusMeters":150,"maxAccuracyMeters":80,"schoolLatitude":32.0575,"schoolLongitude":35.8890},"manualReview":{"enabled":true,"initialPeriodDays":14,"requireReviewForFlagged":true}}',
    1,
    CURRENT_TIMESTAMP
);
