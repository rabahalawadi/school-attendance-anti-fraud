import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { apiRequest } from '../../lib/api';
import { ChallengeData, AttendanceRecord } from '../../types';
import {
  ShieldCheck,
  Clock,
  Users,
  XSquare,
  CheckCircle2,
  RefreshCw,
  Copy,
  Check,
  Sparkles,
  ArrowLeft
} from 'lucide-react';

export const AttendanceSessionDisplay: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();

  const [challenge, setChallenge] = useState<ChallengeData | null>(null);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [secondsLeft, setSecondsLeft] = useState<number>(15);
  const [closing, setClosing] = useState(false);
  const [copiedCode, setCopiedCode] = useState(false);
  const [simulating, setSimulating] = useState(false);

  // Poll challenge rotation and registered records
  useEffect(() => {
    if (!sessionId) return;

    const fetchChallenge = async () => {
      try {
        const data = await apiRequest<ChallengeData>(`/attendance/sessions/${sessionId}/challenge`);
        setChallenge(data);
        setSecondsLeft(data.secondsRemaining || 15);
      } catch (err) {
        console.error('Challenge fetch error', err);
      }
    };

    const fetchRecords = async () => {
      try {
        const list = await apiRequest<AttendanceRecord[]>(`/attendance/sessions/${sessionId}/records`);
        setRecords(list);
      } catch (err) {
        console.error('Records fetch error', err);
      }
    };

    fetchChallenge();
    fetchRecords();

    // 1-second countdown tick
    const timer = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev <= 1) {
          fetchChallenge();
          return 15;
        }
        return prev - 1;
      });
    }, 1000);

    // 3-second live records update
    const recordsTimer = setInterval(fetchRecords, 3000);

    return () => {
      clearInterval(timer);
      clearInterval(recordsTimer);
    };
  }, [sessionId]);

  const handleCopyCode = () => {
    if (challenge?.sixDigitCode) {
      navigator.clipboard.writeText(challenge.sixDigitCode);
      setCopiedCode(true);
      setTimeout(() => setCopiedCode(false), 2000);
    }
  };

  const handleSimulateCheckIn = async () => {
    if (!challenge) return;
    try {
      setSimulating(true);
      await apiRequest('/attendance/register', {
        method: 'POST',
        body: JSON.stringify({
          sessionId,
          qrToken: challenge.qrToken,
          sixDigitCode: challenge.sixDigitCode,
          idempotencyKey: crypto.randomUUID(),
        }),
      });

      // Refresh list
      const list = await apiRequest<AttendanceRecord[]>(`/attendance/sessions/${sessionId}/records`);
      setRecords(list);
    } catch (err: any) {
      alert(`Simulation note: ${err.message}`);
    } finally {
      setSimulating(false);
    }
  };

  const handleCloseSession = async () => {
    if (!confirm('Are you sure you want to close this session? Unrecorded enrolled students will be marked absent.')) {
      return;
    }

    try {
      setClosing(true);
      await apiRequest(`/attendance/sessions/${sessionId}/close`, { method: 'POST' });
      alert('Session closed and finalized successfully.');
      navigate('/teacher');
    } catch (err: any) {
      alert(`Failed to close session: ${err.message}`);
    } finally {
      setClosing(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-white flex flex-col justify-between p-6 sm:p-10 select-none relative overflow-hidden">
      {/* Ambient background glows */}
      <div className="absolute top-1/4 left-1/3 -translate-x-1/2 w-[700px] h-[400px] bg-brand-600/10 rounded-full blur-[140px] pointer-events-none" />

      {/* Top Header */}
      <div className="flex flex-wrap items-center justify-between border-b border-slate-800/80 pb-6 gap-4 relative z-10">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/teacher')}
            className="p-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 transition"
            title="Return to Teacher Dashboard"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-brand-600 to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-brand-500/30 ring-1 ring-white/10">
            <ShieldCheck className="w-7 h-7" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs uppercase tracking-widest text-brand-400 font-extrabold block">
                VeriAttend Projector HUD
              </span>
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            </div>
            <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-white mt-0.5">
              Live Classroom Attendance Check-In
            </h1>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleSimulateCheckIn}
            disabled={simulating}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-500/20 hover:bg-indigo-500/30 text-indigo-300 border border-indigo-500/40 text-xs font-bold transition shadow-sm"
            title="Simulate student check-in for instant demo test"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>{simulating ? 'Simulating...' : 'Simulate Check-In'}</span>
          </button>

          <button
            onClick={handleCloseSession}
            disabled={closing}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-extrabold text-xs uppercase tracking-wider shadow-lg transition"
          >
            <XSquare className="w-4 h-4" />
            <span>{closing ? 'Finalizing...' : 'Close & Finalize Session'}</span>
          </button>
        </div>
      </div>

      {/* Main Classroom Screen Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 my-auto py-8 relative z-10">
        {/* Left Column: Giant Rotating QR Code */}
        <div className="lg:col-span-2 flex flex-col items-center justify-center bg-slate-900/80 backdrop-blur-xl rounded-3xl p-8 sm:p-12 border border-slate-800/90 shadow-2xl">
          <div className="p-6 bg-white rounded-3xl shadow-2xl border-8 border-brand-500/25 ring-1 ring-white/20">
            {challenge?.qrToken ? (
              <QRCodeSVG
                value={JSON.stringify({
                  sessionId,
                  token: challenge.qrToken,
                  t: Date.now(),
                })}
                size={320}
                level="H"
                includeMargin={true}
              />
            ) : (
              <div className="w-[320px] h-[320px] flex items-center justify-center text-slate-400">
                <RefreshCw className="w-12 h-12 animate-spin text-brand-500" />
              </div>
            )}
          </div>

          {/* Countdown & 6-Digit Code */}
          <div className="mt-8 flex flex-col sm:flex-row items-center gap-6 sm:gap-10">
            <div className="flex items-center gap-3 bg-slate-800/80 px-6 py-3.5 rounded-2xl border border-slate-700 shadow-inner">
              <Clock className="w-6 h-6 text-brand-400 animate-pulse" />
              <div>
                <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Dynamic QR Rotation</div>
                <div className="text-2xl font-black text-brand-400 font-mono leading-tight">
                  {secondsLeft}s <span className="text-xs text-slate-500 font-normal">remaining</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-3 bg-slate-800/80 px-6 py-3.5 rounded-2xl border border-slate-700 shadow-inner">
              <div>
                <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">6-Digit Code</div>
                <div className="text-3xl font-black text-emerald-400 tracking-widest font-mono leading-tight">
                  {challenge?.sixDigitCode || '482915'}
                </div>
              </div>
              <button
                onClick={handleCopyCode}
                className="p-2 rounded-lg bg-slate-700 hover:bg-slate-600 text-slate-300 transition"
                title="Copy 6-digit code to clipboard"
              >
                {copiedCode ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          </div>
        </div>

        {/* Right Column: Live Registered Students List */}
        <div className="bg-slate-900/80 backdrop-blur-xl rounded-3xl p-6 border border-slate-800/90 shadow-2xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Users className="w-5 h-5 text-brand-400" />
                <h3 className="font-extrabold text-lg text-white">Live Roster Feed</h3>
              </div>
              <span className="px-3 py-1 rounded-full text-xs font-black bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                {records.length} Present
              </span>
            </div>

            <div className="mt-4 space-y-2.5 max-h-[440px] overflow-y-auto pr-1">
              {records.length === 0 ? (
                <div className="text-center py-16 text-slate-500 text-sm">
                  <div className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center mx-auto mb-3 text-slate-400">
                    <Clock className="w-5 h-5 animate-pulse" />
                  </div>
                  Waiting for students to scan QR or enter 6-digit code...
                </div>
              ) : (
                records.map((rec) => (
                  <div
                    key={rec.id}
                    className="flex items-center justify-between p-3 rounded-xl bg-slate-800/60 border border-slate-800 hover:border-slate-700 transition"
                  >
                    <div>
                      <div className="font-bold text-white text-sm">{rec.studentName}</div>
                      <div className="text-xs text-slate-400 font-mono">{rec.studentNumber}</div>
                    </div>
                    <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      {rec.status}
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 text-center text-xs text-slate-400">
            Rotating challenge cryptography • Time authoritative backend validation
          </div>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="text-center text-xs text-slate-500 font-mono relative z-10">
        VeriAttend • Session ID: {sessionId} • Screen Anti-Fraud Rotation Active
      </div>
    </div>
  );
};
