import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../../lib/api';
import { AttendanceRecord, AttendanceSession } from '../../types';
import {
  Calendar,
  Clock,
  Play,
  CheckCircle2,
  XCircle,
  QrCode,
  Users,
  Eye,
  AlertTriangle,
  Monitor,
  Sparkles,
  ArrowRight
} from 'lucide-react';

export const TeacherDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [assignments, setAssignments] = useState<any[]>([]);
  const [activeSessions, setActiveSessions] = useState<AttendanceSession[]>([]);
  const [pendingRecords, setPendingRecords] = useState<AttendanceRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [startingSession, setStartingSession] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const assignList = await apiRequest<any[]>('/teacher-assignments').catch(() => []);
      setAssignments(assignList);

      // Fetch active open sessions
      const sessions = await apiRequest<AttendanceSession[]>('/attendance/sessions?status=OPEN').catch(() => []);
      if (Array.isArray(sessions)) {
        setActiveSessions(sessions);
      }

      // Fetch pending review records
      const records = await apiRequest<AttendanceRecord[]>('/reports/attendance/export?format=json').catch(() => []);
      if (Array.isArray(records)) {
        setPendingRecords(records.filter((r) => r.verificationStatus === 'PENDING_MANUAL'));
      }
    } catch (err) {
      console.error('Failed to load teacher dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStartSession = async (sectionId: string, subjectId: string) => {
    try {
      setStartingSession(true);
      const session = await apiRequest<AttendanceSession>('/attendance/sessions', {
        method: 'POST',
        body: JSON.stringify({ sectionId, subjectId }),
      });

      // Automatically open session
      const opened = await apiRequest<AttendanceSession>(`/attendance/sessions/${session.id}/open`, {
        method: 'POST',
      });

      navigate(`/teacher/session/${opened.id}`);
    } catch (err: any) {
      alert(`Could not start session: ${err.message}`);
    } finally {
      setStartingSession(false);
    }
  };

  const handleQuickDemoSession = async () => {
    try {
      setStartingSession(true);
      const res = await apiRequest<any>('/attendance/demo/launch', { method: 'POST' });
      const id = res.session?.id || 'c0000000-0000-0000-0000-000000000001';
      navigate(`/teacher/session/${id}`);
    } catch (err: any) {
      navigate('/teacher/session/c0000000-0000-0000-0000-000000000001');
    } finally {
      setStartingSession(false);
    }
  };

  const handleConfirm = async (recordId: string) => {
    try {
      await apiRequest(`/attendance/records/${recordId}/confirm`, { method: 'POST' });
      setPendingRecords((prev) => prev.filter((r) => r.id !== recordId));
    } catch (err: any) {
      alert(`Action failed: ${err.message}`);
    }
  };

  const handleReject = async (recordId: string) => {
    const reason = prompt('Please enter rejection reason:');
    if (!reason) return;

    try {
      await apiRequest(`/attendance/records/${recordId}/reject`, {
        method: 'POST',
        body: JSON.stringify({ reason }),
      });
      setPendingRecords((prev) => prev.filter((r) => r.id !== recordId));
    } catch (err: any) {
      alert(`Action failed: ${err.message}`);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Welcome Banner */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-wider font-bold text-brand-600 block">
            Teacher Operations Console
          </span>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight mt-0.5">Faculty Attendance Management</h1>
          <p className="text-sm text-slate-500 mt-1">
            Display rotating anti-fraud challenge codes, monitor live check-ins, and confirm student presence.
          </p>
        </div>

        <button
          onClick={handleQuickDemoSession}
          disabled={startingSession}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950 font-extrabold text-xs shadow-md transition"
        >
          <Sparkles className="w-4 h-4 fill-current" />
          <span>⚡ Launch Live Demo Session</span>
        </button>
      </div>

      {/* Active Open Session Banner (If Any) */}
      {activeSessions.length > 0 && (
        <div className="bg-gradient-to-r from-emerald-900 via-slate-900 to-indigo-950 text-white p-6 rounded-2xl shadow-lg border border-emerald-500/30 flex flex-col sm:flex-row sm:items-center justify-between gap-4 animate-in fade-in">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30">
              <Monitor className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <div className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                Live Session Currently Open
              </div>
              <h3 className="text-lg font-bold text-white mt-1">
                {activeSessions[0].subject?.name || 'Computer Science'} • {activeSessions[0].section?.name || 'Section A'}
              </h3>
              <p className="text-xs text-slate-300">
                Rotating dynamic QR and 6-digit challenge codes active for this class.
              </p>
            </div>
          </div>

          <button
            onClick={() => navigate(`/teacher/session/${activeSessions[0].id}`)}
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black text-xs uppercase tracking-wide shadow-md transition"
          >
            <Monitor className="w-4 h-4" />
            <span>Open Classroom Projector Screen</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Assigned Classes */}
      <div>
        <h2 className="text-lg font-black text-slate-900 mb-4 flex items-center gap-2">
          <Calendar className="w-5 h-5 text-brand-600" />
          Assigned Classes & Subjects
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {assignments.map((assign) => (
            <div
              key={assign.id}
              className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 hover:border-brand-300 hover:shadow-md transition flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-brand-50 text-brand-700">
                    {assign.subject?.code || 'CS101'}
                  </span>
                  <span className="text-xs text-slate-500 font-medium">Room 101</span>
                </div>
                <h3 className="text-lg font-extrabold text-slate-900 mt-3">{assign.subject?.name}</h3>
                <p className="text-sm text-slate-500 mt-0.5">
                  {assign.section?.grade?.name} • {assign.section?.name}
                </p>
              </div>

              <button
                onClick={() => handleStartSession(assign.section?.id, assign.subject?.id)}
                disabled={startingSession}
                className="mt-6 w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm shadow-md transition disabled:opacity-50"
              >
                <Play className="w-4 h-4 fill-current" />
                Start Dynamic QR Session
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Pending Manual Verification Review Queue (Initial 2-Week Window) */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-black text-slate-900 flex items-center gap-2">
              <Clock className="w-5 h-5 text-amber-500" />
              Manual Verification Queue (Initial 14-Day Grace Window)
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Review and confirm student presence before final attendance submission
            </p>
          </div>
          <span className="px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-800">
            {pendingRecords.length} Pending Review
          </span>
        </div>

        {pendingRecords.length === 0 ? (
          <div className="text-center py-10 text-slate-400 text-sm">
            <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
            No pending attendance records awaiting review.
          </div>
        ) : (
          <div className="overflow-x-auto rounded-xl border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-xs font-bold text-slate-600 uppercase tracking-wider">
                <tr>
                  <th className="px-4 py-3">Student ID</th>
                  <th className="px-4 py-3">Student Name</th>
                  <th className="px-4 py-3">Class</th>
                  <th className="px-4 py-3">Recorded Time</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {pendingRecords.map((rec) => (
                  <tr key={rec.id} className="hover:bg-slate-50 transition">
                    <td className="px-4 py-3 font-mono font-bold text-slate-900">{rec.studentNumber}</td>
                    <td className="px-4 py-3 font-semibold text-slate-800">{rec.studentName}</td>
                    <td className="px-4 py-3 text-slate-600">{rec.className}</td>
                    <td className="px-4 py-3 text-slate-500 text-xs font-mono">{rec.recordedAt?.substring(11, 16) || '—'}</td>
                    <td className="px-4 py-3 text-right space-x-2">
                      <button
                        onClick={() => handleConfirm(rec.id)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-emerald-50 text-emerald-700 hover:bg-emerald-100 font-bold text-xs border border-emerald-200 transition"
                      >
                        <CheckCircle2 className="w-3.5 h-3.5" /> Confirm Present
                      </button>
                      <button
                        onClick={() => handleReject(rec.id)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-rose-50 text-rose-700 hover:bg-rose-100 font-bold text-xs border border-rose-200 transition"
                      >
                        <XCircle className="w-3.5 h-3.5" /> Reject
                      </button>
                    </td>
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
