import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiRequest } from '../lib/api';
import {
  Sparkles,
  Shield,
  GraduationCap,
  BookOpen,
  Play,
  Download,
  CheckCircle,
  FileSpreadsheet
} from 'lucide-react';

export const DemoBar: React.FC = () => {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [switching, setSwitching] = useState<string | null>(null);
  const [launching, setLaunching] = useState(false);

  const personas = [
    {
      label: 'Admin',
      role: 'ADMIN',
      username: 'admin',
      name: 'System Admin',
      icon: Shield,
      color: 'bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border-indigo-200 dark:bg-indigo-950 dark:text-indigo-300',
      activeColor: 'bg-indigo-600 text-white border-indigo-600',
      target: '/admin',
    },
    {
      label: 'Teacher Ahmad',
      role: 'TEACHER',
      username: 'teacher.ahmad',
      name: 'Dr. Ahmad Yousef',
      icon: BookOpen,
      color: 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border-emerald-200 dark:bg-emerald-950 dark:text-emerald-300',
      activeColor: 'bg-emerald-600 text-white border-emerald-600',
      target: '/teacher',
    },
    {
      label: 'Student Tariq',
      role: 'STUDENT',
      username: 'student.tariq',
      name: 'Tariq (Grade 10-A)',
      icon: GraduationCap,
      color: 'bg-sky-50 text-sky-700 hover:bg-sky-100 border-sky-200 dark:bg-sky-950 dark:text-sky-300',
      activeColor: 'bg-sky-600 text-white border-sky-600',
      target: '/student',
    },
    {
      label: 'Student Layla',
      role: 'STUDENT',
      username: 'student.layla',
      name: 'Layla (Grade 10-A)',
      icon: GraduationCap,
      color: 'bg-purple-50 text-purple-700 hover:bg-purple-100 border-purple-200 dark:bg-purple-950 dark:text-purple-300',
      activeColor: 'bg-purple-600 text-white border-purple-600',
      target: '/student',
    },
  ];

  const handleSwitchPersona = async (p: typeof personas[0]) => {
    try {
      setSwitching(p.username);
      await login(p.username, 'Password@123456');
      navigate(p.target);
    } catch (err: any) {
      alert(`Persona switch error: ${err.message}`);
    } finally {
      setSwitching(null);
    }
  };

  const handleLaunchDemoSession = async () => {
    try {
      setLaunching(true);
      // Ensure session is launched on backend
      const res = await apiRequest<any>('/attendance/demo/launch', { method: 'POST' });
      const sessionId = res.session?.id || 'c0000000-0000-0000-0000-000000000001';

      // If not logged in as teacher or admin, switch to teacher first
      if (user?.role !== 'TEACHER' && user?.role !== 'ADMIN') {
        await login('teacher.ahmad', 'Password@123456');
      }

      navigate(`/teacher/session/${sessionId}`);
    } catch (err: any) {
      // Fallback direct navigate to demo session
      navigate('/teacher/session/c0000000-0000-0000-0000-000000000001');
    } finally {
      setLaunching(false);
    }
  };

  const handleDownloadTemplate = (format: 'xlsx' | 'csv') => {
    const token = localStorage.getItem('token');
    const url = `/api/v1/students/import/template?format=${format}&token=${token || ''}`;
    window.open(url, '_blank');
  };

  return (
    <aside aria-label="Demo Testing Bar" className="bg-slate-900 border-b border-slate-800 text-slate-200 text-xs py-2 px-4 shadow-inner">
      <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
        {/* Left: Persona Switchers */}
        <div className="flex flex-wrap items-center gap-2">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-brand-500/10 text-brand-400 font-bold border border-brand-500/20 tracking-wide uppercase text-[11px]">
            <Sparkles className="w-3.5 h-3.5 text-brand-400 animate-pulse" />
            <span>Interactive Demo</span>
          </div>

          <span className="text-slate-500 hidden sm:inline">|</span>
          <span className="text-slate-400 font-medium hidden sm:inline">1-Click Switch:</span>

          <div className="flex flex-wrap items-center gap-1.5">
            {personas.map((p) => {
              const Icon = p.icon;
              const isActive = user?.username === p.username;
              const isLoading = switching === p.username;

              return (
                <button
                  key={p.username}
                  onClick={() => handleSwitchPersona(p)}
                  disabled={isLoading}
                  className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md border text-xs font-semibold transition-all shadow-sm ${
                    isActive ? p.activeColor : p.color
                  }`}
                  title={`Switch to ${p.name}`}
                >
                  <Icon className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
                  <span>{p.label}</span>
                  {isActive && <CheckCircle className="w-3 h-3 ml-0.5 text-white/90" />}
                </button>
              );
            })}
          </div>
        </div>

        {/* Right: Quick Launch & Template Download */}
        <div className="flex items-center gap-2 ml-auto">
          <button
            onClick={handleLaunchDemoSession}
            disabled={launching}
            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-md bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-600 hover:to-amber-700 text-slate-950 font-bold shadow-sm transition"
            title="Open live projector view with dynamic QR rotation"
          >
            <Play className={`w-3 h-3 fill-current ${launching ? 'animate-spin' : ''}`} />
            <span>Live Projector Session</span>
          </button>

          <div className="relative group">
            <button
              className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 font-medium transition"
              title="Download Student Import Templates"
            >
              <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-400" />
              <span>Import Template</span>
            </button>
            <div className="absolute right-0 top-full mt-1 w-44 bg-slate-900 border border-slate-700 rounded-lg shadow-xl p-1 z-50 hidden group-hover:block">
              <button
                onClick={() => handleDownloadTemplate('xlsx')}
                className="w-full text-left flex items-center gap-2 px-2.5 py-1.5 text-xs text-slate-200 hover:bg-slate-800 rounded-md transition"
              >
                <Download className="w-3 h-3 text-emerald-400" />
                <span>Excel (.xlsx)</span>
              </button>
              <button
                onClick={() => handleDownloadTemplate('csv')}
                className="w-full text-left flex items-center gap-2 px-2.5 py-1.5 text-xs text-slate-200 hover:bg-slate-800 rounded-md transition"
              >
                <Download className="w-3 h-3 text-sky-400" />
                <span>CSV (.csv)</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </aside>
  );
};
