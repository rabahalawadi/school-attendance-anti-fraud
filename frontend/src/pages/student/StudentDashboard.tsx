import React, { useState, useEffect } from 'react';
import { apiRequest } from '../../lib/api';
import { useAuth } from '../../context/AuthContext';
import { AttendanceRecord, AttendanceSession } from '../../types';
import { Html5QrcodeScanner } from 'html5-qrcode';
import {
  ShieldCheck,
  QrCode,
  KeyRound,
  MapPin,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Calendar,
  Layers,
  ChevronRight
} from 'lucide-react';

export const StudentDashboard: React.FC = () => {
  const { user } = useAuth();
  const [analytics, setAnalytics] = useState<any>(null);
  const [history, setHistory] = useState<AttendanceRecord[]>([]);
  const [activeSession, setActiveSession] = useState<AttendanceSession | null>(null);

  // Check-in Form States
  const [sixDigitCode, setSixDigitCode] = useState('');
  const [qrToken, setQrToken] = useState('');
  const [scannerOpen, setScannerOpen] = useState(false);
  const [coords, setCoords] = useState<{ lat: number; lng: number; acc: number } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [checkInResult, setCheckInResult] = useState<any | null>(null);

  useEffect(() => {
    loadStudentData();
  }, []);

  const loadStudentData = async () => {
    try {
      const records = await apiRequest<AttendanceRecord[]>('/attendance/my-records');
      setHistory(records);

      if (user?.profileId) {
        const stats = await apiRequest<any>(`/reports/attendance/students/${user.profileId}`);
        setAnalytics(stats);
      }

      // Find any open active sessions for this student's class
      const sessions = await apiRequest<AttendanceSession[]>('/attendance/sessions').catch(() => []);
      if (Array.isArray(sessions)) {
        const open = sessions.find((s) => s.status === 'OPEN');
        if (open) setActiveSession(open);
      }
    } catch (err) {
      console.error('Failed to load student data', err);
    }
  };

  // QR Scanner activation
  useEffect(() => {
    if (!scannerOpen) return;

    const scanner = new Html5QrcodeScanner(
      'qr-reader',
      { fps: 10, qrbox: { width: 250, height: 250 } },
      false
    );

    scanner.render(
      (decodedText) => {
        try {
          const parsed = JSON.parse(decodedText);
          if (parsed.token) {
            setQrToken(parsed.token);
          } else {
            setQrToken(decodedText);
          }
        } catch {
          setQrToken(decodedText);
        }
        scanner.clear();
        setScannerOpen(false);
      },
      (error) => {
        // scan error ignored
      }
    );

    return () => {
      scanner.clear().catch(() => {});
    };
  }, [scannerOpen]);

  const requestGps = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your device browser');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setCoords({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
          acc: pos.coords.accuracy,
        });
      },
      (err) => {
        alert(`Location permission denied or unavailable: ${err.message}`);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeSession) {
      alert('No active attendance session currently open.');
      return;
    }

    setSubmitting(true);
    setCheckInResult(null);

    const payload = {
      sessionId: activeSession.id,
      qrToken: qrToken || undefined,
      sixDigitCode: sixDigitCode || undefined,
      deviceCredential: localStorage.getItem('deviceId') || undefined,
      latitude: coords?.lat,
      longitude: coords?.lng,
      accuracyMeters: coords?.acc,
      idempotencyKey: crypto.randomUUID(),
    };

    try {
      const res: any = await apiRequest('/attendance/register', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      setCheckInResult(res);
      loadStudentData();
    } catch (err: any) {
      setCheckInResult({
        accepted: false,
        message: err.message || 'Verification failed',
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Student Profile Banner */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-wider font-bold text-brand-600 block">
            Student Attendance Portal
          </span>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight mt-0.5">{user?.fullName}</h1>
          <p className="text-sm text-slate-500 font-mono">Student ID: {user?.identifier}</p>
        </div>

        {/* Attendance Percentage Badge */}
        <div className="flex items-center gap-4 bg-brand-50 p-4 rounded-2xl border border-brand-100">
          <div className="text-right">
            <span className="text-xs font-bold text-brand-700 uppercase tracking-wider block">
              Attendance Standing
            </span>
            <span className="text-2xl font-black text-brand-900">
              {analytics?.attendancePercentage !== undefined ? `${analytics.attendancePercentage}%` : '100%'}
            </span>
          </div>
          <div className="w-12 h-12 rounded-xl bg-brand-600 text-white flex items-center justify-center font-black text-lg shadow-md shadow-brand-500/20">
            ✓
          </div>
        </div>
      </div>

      {/* Active Attendance Session Check-In Card */}
      <div className="bg-gradient-to-br from-slate-900 via-brand-950 to-slate-900 text-white p-8 rounded-3xl shadow-xl border border-slate-800 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-6">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-brand-500/20 text-brand-400 flex items-center justify-center border border-brand-500/30">
              <ShieldCheck className="w-7 h-7" />
            </div>
            <div>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                Session Active & Accepting Registrations
              </span>
              <h2 className="text-xl font-black text-white mt-1">Live Classroom Attendance Registration</h2>
            </div>
          </div>
        </div>

        {checkInResult && (
          <div
            className={`p-5 rounded-2xl border ${
              checkInResult.accepted
                ? 'bg-emerald-950/60 border-emerald-500/40 text-emerald-200'
                : 'bg-rose-950/60 border-rose-500/40 text-rose-200'
            }`}
          >
            <div className="flex items-start gap-3">
              {checkInResult.accepted ? (
                <CheckCircle2 className="w-6 h-6 text-emerald-400 shrink-0" />
              ) : (
                <AlertTriangle className="w-6 h-6 text-rose-400 shrink-0" />
              )}
              <div>
                <h4 className="font-extrabold text-base">
                  {checkInResult.accepted ? 'Attendance Registered!' : 'Registration Verification Rejected'}
                </h4>
                <p className="text-sm mt-1 opacity-90">{checkInResult.message}</p>
                {checkInResult.verificationStatus === 'PENDING_MANUAL' && (
                  <p className="text-xs font-semibold text-amber-300 mt-2">
                    Note: Your attendance is pending teacher confirmation during the initial 14-day grace period.
                  </p>
                )}
              </div>
            </div>
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Factor 1: Rotating QR Scanner */}
            <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/80 space-y-4">
              <div className="flex items-center justify-between">
                <span className="font-bold text-sm text-slate-200 flex items-center gap-2">
                  <QrCode className="w-4 h-4 text-brand-400" />
                  Factor 1: Rotating QR Token
                </span>
                {qrToken && (
                  <span className="text-xs font-bold text-emerald-400 bg-emerald-500/20 px-2 py-0.5 rounded-full">
                    QR Captured
                  </span>
                )}
              </div>

              {scannerOpen ? (
                <div id="qr-reader" className="w-full rounded-xl overflow-hidden bg-black" />
              ) : (
                <button
                  type="button"
                  onClick={() => setScannerOpen(true)}
                  className="w-full py-3 px-4 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-bold text-sm shadow-md transition flex items-center justify-center gap-2"
                >
                  <QrCode className="w-5 h-5" />
                  Open Camera Scanner
                </button>
              )}

              {qrToken && (
                <div className="text-xs font-mono text-slate-400 truncate bg-slate-900/80 p-2 rounded-lg">
                  Token: {qrToken}
                </div>
              )}
            </div>

            {/* Factor 2: 6-Digit Code */}
            <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/80 space-y-4">
              <span className="font-bold text-sm text-slate-200 flex items-center gap-2">
                <KeyRound className="w-4 h-4 text-emerald-400" />
                Factor 2: 6-Digit Challenge Code
              </span>

              <input
                type="text"
                maxLength={6}
                value={sixDigitCode}
                onChange={(e) => setSixDigitCode(e.target.value.replace(/\D/g, ''))}
                placeholder="Enter 6-digit code from screen"
                className="w-full bg-slate-900/90 text-white text-center font-mono text-2xl tracking-widest font-black py-2.5 px-4 rounded-xl border border-slate-600 focus:border-brand-500 focus:ring-2 focus:ring-brand-500"
              />

              <div className="flex items-center justify-between text-xs text-slate-400">
                <span>Optional Location Check:</span>
                <button
                  type="button"
                  onClick={requestGps}
                  className="inline-flex items-center gap-1 text-brand-400 hover:text-brand-300 font-bold"
                >
                  <MapPin className="w-3.5 h-3.5" />
                  {coords ? 'Location Acquired' : 'Verify GPS'}
                </button>
              </div>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-3.5 px-6 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-base shadow-lg transition flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {submitting ? 'Evaluating Verification...' : 'Submit Attendance Check-In'}
            <ChevronRight className="w-5 h-5" />
          </button>
        </form>
      </div>

      {/* Attendance History */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 space-y-4">
        <h3 className="font-black text-slate-900 text-lg flex items-center gap-2">
          <Calendar className="w-5 h-5 text-brand-600" />
          Personal Attendance Records
        </h3>

        {history.length === 0 ? (
          <p className="text-sm text-slate-500 text-center py-8">No attendance records yet.</p>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-xs font-bold text-slate-600 uppercase tracking-wider">
                <tr>
                  <th className="px-4 py-3">Date</th>
                  <th className="px-4 py-3">Subject</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Verification Level</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {history.map((h) => (
                  <tr key={h.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-600 font-mono text-xs">{h.recordedAt.substring(0, 10)}</td>
                    <td className="px-4 py-3 font-semibold text-slate-900">{h.subjectName}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold ${
                          h.status === 'PRESENT'
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : h.status === 'LATE'
                            ? 'bg-amber-50 text-amber-700 border border-amber-200'
                            : 'bg-rose-50 text-rose-700 border border-rose-200'
                        }`}
                      >
                        {h.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-500 font-mono">{h.verificationStatus}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
