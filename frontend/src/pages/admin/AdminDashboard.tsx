import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../../lib/api';
import { useLanguage } from '../../context/LanguageContext';
import {
  Users,
  GraduationCap,
  Layers,
  ShieldAlert,
  Clock,
  Download,
  Upload,
  Sliders,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Laptop
} from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const { t } = useLanguage();
  const [summary, setSummary] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const fetchSummary = async () => {
    try {
      const data = await apiRequest<any>('/reports/attendance/summary');
      setSummary(data);
    } catch (err) {
      console.error('Failed to load summary', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSummary();
  }, []);

  const handleExport = (format: 'xlsx' | 'csv') => {
    const token = localStorage.getItem('token');
    window.open(`/api/v1/reports/attendance/export?format=${format}&token=${token}`, '_blank');
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
      {/* Top Banner & Quick Actions */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">Administrator Portal</h1>
          <p className="text-sm text-slate-500 mt-1">
            School-wide attendance management, multi-factor anti-fraud policies, and student rosters
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2.5">
          <Link
            to="/admin/anti-fraud"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-brand-50 hover:bg-brand-100 text-brand-700 font-bold text-sm border border-brand-200 transition"
          >
            <Sliders className="w-4 h-4" />
            Anti-Fraud Policies
          </Link>
          <Link
            to="/admin/excel-import"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-sm border border-slate-200 transition"
          >
            <Upload className="w-4 h-4" />
            Import Excel
          </Link>
          <button
            onClick={() => handleExport('xlsx')}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm shadow-sm transition"
          >
            <Download className="w-4 h-4" />
            Export (.xlsx)
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
            <GraduationCap className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-900">{summary?.totalStudents || 0}</div>
            <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mt-0.5">Enrolled Students</div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center shrink-0">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-900">{summary?.totalTeachers || 0}</div>
            <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mt-0.5">Active Teachers</div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
            <Layers className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-900">{summary?.totalSections || 0}</div>
            <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mt-0.5">Class Sections</div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-black text-slate-900">{summary?.overallAttendancePercentage || 100}%</div>
            <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mt-0.5">Overall Attendance</div>
          </div>
        </div>
      </div>

      {/* Breakdown Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
          <h2 className="text-lg font-black text-slate-900 mb-4">Attendance Status Breakdown</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="p-4 rounded-xl bg-emerald-50/70 border border-emerald-100">
              <span className="text-xs font-bold text-emerald-800 uppercase tracking-wider block">Present</span>
              <span className="text-2xl font-black text-emerald-900 mt-1 block">{summary?.presentCount || 0}</span>
            </div>
            <div className="p-4 rounded-xl bg-amber-50/70 border border-amber-100">
              <span className="text-xs font-bold text-amber-800 uppercase tracking-wider block">Late</span>
              <span className="text-2xl font-black text-amber-900 mt-1 block">{summary?.lateCount || 0}</span>
            </div>
            <div className="p-4 rounded-xl bg-rose-50/70 border border-rose-100">
              <span className="text-xs font-bold text-rose-800 uppercase tracking-wider block">Absent</span>
              <span className="text-2xl font-black text-rose-900 mt-1 block">{summary?.absentCount || 0}</span>
            </div>
            <div className="p-4 rounded-xl bg-sky-50/70 border border-sky-100">
              <span className="text-xs font-bold text-sky-800 uppercase tracking-wider block">Excused</span>
              <span className="text-2xl font-black text-sky-900 mt-1 block">{summary?.excusedCount || 0}</span>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-black text-slate-900 mb-2">Anti-Fraud Alerts</h2>
            <p className="text-xs text-slate-500 mb-4">Records requiring administrative or teacher oversight</p>
            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 rounded-xl bg-amber-50 text-amber-900 border border-amber-200 text-sm">
                <span className="flex items-center gap-2 font-medium">
                  <Clock className="w-4 h-4 text-amber-600" />
                  Initial Window Reviews
                </span>
                <span className="font-extrabold text-base">{summary?.pendingReviewCount || 0}</span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-xl bg-rose-50 text-rose-900 border border-rose-200 text-sm">
                <span className="flex items-center gap-2 font-medium">
                  <ShieldAlert className="w-4 h-4 text-rose-600" />
                  Suspicious / Flagged Attempts
                </span>
                <span className="font-extrabold text-base">{summary?.suspiciousAttemptsCount || 0}</span>
              </div>
            </div>
          </div>

          <Link
            to="/admin/anti-fraud"
            className="mt-4 inline-flex items-center justify-center gap-2 w-full py-2.5 px-4 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs uppercase tracking-wider transition"
          >
            Review Anti-Fraud Engine Controls
          </Link>
        </div>
      </div>
    </div>
  );
};
