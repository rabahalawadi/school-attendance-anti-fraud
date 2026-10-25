-- ==============================================================================
-- V3: Add Active Demo Session and Live Challenge Codes for Zero-Friction Testing
-- ==============================================================================

INSERT INTO attendance_sessions (
    id, academic_year_id, section_id, subject_id, teacher_id,
    scheduled_start_at, scheduled_end_at, opened_at, status,
    policy_snapshot, session_secret, created_at
) VALUES (
    'c0000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000001',
    '50000000-0000-0000-0000-000000000001',
    '60000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'OPEN',
    '{"deviceCheck":{"enabled":true,"required":false},"qrCheck":{"enabled":true,"required":true,"rotationSeconds":15},"sixDigitCheck":{"enabled":true,"required":false,"expirationSeconds":30},"gpsCheck":{"enabled":true,"required":false,"radiusMeters":150,"maxAccuracyMeters":80,"schoolLatitude":32.0575,"schoolLongitude":35.8890},"manualReview":{"enabled":true,"initialPeriodDays":14,"requireReviewForFlagged":true}}',
    'demo_session_secret_2026',
    CURRENT_TIMESTAMP
);

INSERT INTO attendance_challenges (
    id, attendance_session_id, challenge_type, token_hash, issued_at, expires_at, status
) VALUES 
    ('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'QR_TOKEN', 'veriattend_demo_qr_active_token_9999', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE'),
    ('d0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'SIX_DIGIT_CODE', '482915', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE');
