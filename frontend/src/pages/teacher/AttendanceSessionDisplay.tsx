import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { apiRequest } from '../../lib/api';
import { ChallengeData, AttendanceRecord } from '../../types';
import { ShieldCheck, Clock, Users, XSquare, CheckCircle2, RefreshCw } from 'lucide-react';

export const AttendanceSessionDisplay: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();

  const [challenge, setChallenge] = useState<ChallengeData | null>(null);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [secondsLeft, setSecondsLeft] = useState<number>(15);
  const [closing, setClosing] = useState(false);

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
    <div className="min-h-screen bg-slate-950 text-white flex flex-col justify-between p-6 sm:p-10 select-none">
      {/* Top Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-6">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-brand-600 flex items-center justify-center text-white shadow-lg shadow-brand-500/30">
            <ShieldCheck className="w-7 h-7" />
          </div>
          <div>
            <span className="text-xs uppercase tracking-widest text-brand-400 font-extrabold block">
              Classroom Attendance Display
            </span>
            <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-white mt-0.5">
              Scan with your Phone to Register
            </h1>
          </div>
        </div>

        <button
          onClick={handleCloseSession}
          disabled={closing}
          className="flex items-center gap-2 px-6 py-3 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-extrabold text-sm uppercase tracking-wider shadow-lg transition"
        >
          <XSquare className="w-5 h-5" />
          {closing ? 'Closing...' : 'Close & Finalize Session'}
        </button>
      </div>

      {/* Main Classroom Screen Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-10 my-auto py-8">
        {/* Left Column: Giant Rotating QR Code */}
        <div className="lg:col-span-2 flex flex-col items-center justify-center bg-slate-900/80 backdrop-blur-md rounded-3xl p-8 sm:p-12 border border-slate-800 shadow-2xl">
          <div className="p-6 bg-white rounded-3xl shadow-2xl border-8 border-brand-500/30">
            {challenge?.qrToken ? (
              <QRCodeSVG
                value={JSON.stringify({
                  sessionId,
                  token: challenge.qrToken,
                  t: Date.now(),
                })}
                size={340}
                level="H"
                includeMargin={true}
              />
            ) : (
              <div className="w-[340px] h-[340px] flex items-center justify-center text-slate-400">
                <RefreshCw className="w-12 h-12 animate-spin text-brand-500" />
              </div>
            )}
          </div>

          {/* Countdown & 6-Digit Code */}
          <div className="mt-8 flex flex-col sm:flex-row items-center gap-6 sm:gap-12">
            <div className="flex items-center gap-3 bg-slate-800/80 px-6 py-3 rounded-2xl border border-slate-700">
              <Clock className="w-6 h-6 text-brand-400 animate-pulse" />
              <div>
                <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Rotation Countdown</div>
                <div className="text-2xl font-black text-brand-400 font-mono">
                  {secondsLeft}s <span className="text-xs text-slate-500 font-normal">remaining</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-3 bg-slate-800/80 px-6 py-3 rounded-2xl border border-slate-700">
              <div>
                <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Or Enter 6-Digit Code</div>
                <div className="text-3xl font-black text-emerald-400 tracking-widest font-mono">
                  {challenge?.sixDigitCode || '------'}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Live Registered Students List */}
        <div className="bg-slate-900/80 backdrop-blur-md rounded-3xl p-6 border border-slate-800 shadow-2xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Users className="w-5 h-5 text-brand-400" />
                <h3 className="font-extrabold text-lg text-white">Registered Students</h3>
              </div>
              <span className="px-3 py-1 rounded-full text-sm font-black bg-brand-500/20 text-brand-400 border border-brand-500/30">
                {records.length} Present
              </span>
            </div>

            <div className="mt-4 space-y-2.5 max-h-[460px] overflow-y-auto pr-2">
              {records.length === 0 ? (
                <div className="text-center py-12 text-slate-500 text-sm">
                  Waiting for students to scan QR code...
                </div>
              ) : (
                records.map((rec) => (
                  <div
                    key={rec.id}
                    className="flex items-center justify-between p-3 rounded-xl bg-slate-800/50 border border-slate-800"
                  >
                    <div>
                      <div className="font-bold text-white text-sm">{rec.studentName}</div>
                      <div className="text-xs text-slate-400 font-mono">{rec.studentNumber}</div>
                    </div>
                    <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-400">
                      <CheckCircle2 className="w-4 h-4" />
                      {rec.status}
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 text-center text-xs text-slate-500">
            Attendance policy snapshot active • Verified by server authority
          </div>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="text-center text-xs text-slate-600 font-mono">
        Session ID: {sessionId} • Rotating tokens prevent remote attendance fraud
      </div>
    </div>
  );
};
