export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';

export interface UserProfile {
  id: string;
  username: string;
  role: Role;
  status: string;
  profileId?: string;
  identifier: string;
  fullName: string;
  email?: string;
  lastLoginAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: string;
  username: string;
  role: Role;
  profileId?: string;
  fullName: string;
}

export interface AttendanceSession {
  id: string;
  academicYear: { id: string; name: string };
  section: { id: string; name: string; grade: { id: string; name: string } };
  subject: { id: string; name: string; code: string };
  teacher: { id: string; fullName: string };
  scheduledStartAt: string;
  scheduledEndAt: string;
  openedAt?: string;
  closedAt?: string;
  status: 'SCHEDULED' | 'OPEN' | 'CLOSING' | 'CLOSED' | 'CANCELLED';
  policySnapshot: string;
}

export interface ChallengeData {
  sessionId: string;
  qrToken: string;
  sixDigitCode: string;
  expiresAt: string;
  secondsRemaining: number;
  status: string;
}

export interface AttendanceRecord {
  id: string;
  sessionId: string;
  subjectName: string;
  className: string;
  teacherName: string;
  studentId: string;
  studentNumber: string;
  studentName: string;
  status: 'PRESENT' | 'LATE' | 'ABSENT' | 'EXCUSED' | 'PENDING_CONFIRMATION' | 'REJECTED';
  verificationStatus: 'SERVER_VERIFIED' | 'OFFLINE_RECORDED' | 'PENDING_MANUAL' | 'MANUALLY_CONFIRMED' | 'FLAGGED';
  recordedAt: string;
  serverReceivedAt: string;
  confirmedByName?: string;
  confirmedAt?: string;
  correctionReason?: string;
}

export interface AntiFraudPolicyConfig {
  deviceCheck: { enabled: boolean; required: boolean };
  qrCheck: { enabled: boolean; required: boolean; rotationSeconds: number };
  sixDigitCheck: { enabled: boolean; required: boolean; expirationSeconds: number };
  gpsCheck: {
    enabled: boolean;
    required: boolean;
    radiusMeters: number;
    maxAccuracyMeters: number;
    schoolLatitude: number;
    schoolLongitude: number;
  };
  manualReview: {
    enabled: boolean;
    initialPeriodDays: number;
    requireReviewForFlagged: boolean;
  };
}

export interface VerificationPolicyEntity {
  id: string;
  name: string;
  scopeType: string;
  scopeId?: string;
  enabled: boolean;
  policyConfiguration: string;
  version: number;
  effectiveFrom: string;
  updatedAt: string;
}
