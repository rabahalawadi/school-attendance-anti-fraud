import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import {
  ShieldCheck,
  Lock,
  User,
  AlertCircle,
  ArrowRight,
  Sparkles,
  BookOpen,
  GraduationCap,
  Shield,
  CheckCircle2
} from 'lucide-react';

export const Login: React.FC = () => {
  const { login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [quickLoadingRole, setQuickLoadingRole] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login(username, password);
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (u: string, p: string, roleName: string, targetPath: string) => {
    setUsername(u);
    setPassword(p);
    setError(null);
    setQuickLoadingRole(roleName);

    try {
      await login(u, p);
      navigate(targetPath);
    } catch (err: any) {
      setError(err.message || `Failed to sign in as ${roleName}`);
    } finally {
      setQuickLoadingRole(null);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden selection:bg-brand-500 selection:text-white">
      {/* Decorative ambient background glows */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-brand-600/15 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-10 right-10 w-80 h-80 bg-indigo-600/10 rounded-full blur-[100px] pointer-events-none" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="flex justify-center">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-brand-600 to-indigo-500 flex items-center justify-center text-white shadow-xl shadow-brand-500/25 ring-1 ring-white/20">
            <ShieldCheck className="w-8 h-8" />
          </div>
        </div>
        <h1 className="mt-5 text-center text-3xl font-extrabold text-white tracking-tight sm:text-4xl">
          VeriAttend
        </h1>
        <p className="mt-2 text-center text-sm text-slate-400">
          Smart Multi-Factor School Attendance & Anti-Fraud Platform
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-lg px-2 relative z-10">
        {/* Quick 1-Click Access Cards */}
        <div className="mb-6 bg-slate-900/80 backdrop-blur-md p-4 rounded-2xl border border-slate-800 shadow-xl">
          <div className="flex items-center justify-between mb-3 px-1">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-amber-400" />
              1-Click Demo Login (Instant Access)
            </span>
            <span className="text-[11px] text-slate-500 font-mono">Password: Password@123456</span>
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <button
              type="button"
              onClick={() => handleQuickLogin('admin', 'Password@123456', 'Admin', '/admin')}
              disabled={loading || quickLoadingRole !== null}
              className="group text-left p-3 rounded-xl bg-slate-800/80 hover:bg-slate-800 border border-slate-700/80 hover:border-indigo-500/50 transition-all flex items-start gap-2.5 relative"
            >
              <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400 group-hover:scale-105 transition">
                <Shield className="w-4 h-4" />
              </div>
              <div className="min-w-0">
                <div className="text-xs font-bold text-white group-hover:text-indigo-300 transition">
                  System Admin
                </div>
                <div className="text-[11px] text-slate-400 truncate">admin</div>
              </div>
              {quickLoadingRole === 'Admin' && (
                <div className="absolute right-2 top-3 w-4 h-4 border-2 border-indigo-400 border-t-transparent rounded-full animate-spin" />
              )}
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('teacher.ahmad', 'Password@123456', 'Teacher', '/teacher')}
              disabled={loading || quickLoadingRole !== null}
              className="group text-left p-3 rounded-xl bg-slate-800/80 hover:bg-slate-800 border border-slate-700/80 hover:border-emerald-500/50 transition-all flex items-start gap-2.5 relative"
            >
              <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400 group-hover:scale-105 transition">
                <BookOpen className="w-4 h-4" />
              </div>
              <div className="min-w-0">
                <div className="text-xs font-bold text-white group-hover:text-emerald-300 transition">
                  Teacher Ahmad
                </div>
                <div className="text-[11px] text-slate-400 truncate">teacher.ahmad</div>
              </div>
              {quickLoadingRole === 'Teacher' && (
                <div className="absolute right-2 top-3 w-4 h-4 border-2 border-emerald-400 border-t-transparent rounded-full animate-spin" />
              )}
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('student.tariq', 'Password@123456', 'Student Tariq', '/student')}
              disabled={loading || quickLoadingRole !== null}
              className="group text-left p-3 rounded-xl bg-slate-800/80 hover:bg-slate-800 border border-slate-700/80 hover:border-sky-500/50 transition-all flex items-start gap-2.5 relative"
            >
              <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400 group-hover:scale-105 transition">
                <GraduationCap className="w-4 h-4" />
              </div>
              <div className="min-w-0">
                <div className="text-xs font-bold text-white group-hover:text-sky-300 transition">
                  Student Tariq
                </div>
                <div className="text-[11px] text-slate-400 truncate">student.tariq (10-A)</div>
              </div>
              {quickLoadingRole === 'Student Tariq' && (
                <div className="absolute right-2 top-3 w-4 h-4 border-2 border-sky-400 border-t-transparent rounded-full animate-spin" />
              )}
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('student.layla', 'Password@123456', 'Student Layla', '/student')}
              disabled={loading || quickLoadingRole !== null}
              className="group text-left p-3 rounded-xl bg-slate-800/80 hover:bg-slate-800 border border-slate-700/80 hover:border-purple-500/50 transition-all flex items-start gap-2.5 relative"
            >
              <div className="p-2 rounded-lg bg-purple-500/10 text-purple-400 group-hover:scale-105 transition">
                <GraduationCap className="w-4 h-4" />
              </div>
              <div className="min-w-0">
                <div className="text-xs font-bold text-white group-hover:text-purple-300 transition">
                  Student Layla
                </div>
                <div className="text-[11px] text-slate-400 truncate">student.layla (10-A)</div>
              </div>
              {quickLoadingRole === 'Student Layla' && (
                <div className="absolute right-2 top-3 w-4 h-4 border-2 border-purple-400 border-t-transparent rounded-full animate-spin" />
              )}
            </button>
          </div>
        </div>

        {/* Standard Credentials Form */}
        <div className="bg-slate-900/90 backdrop-blur-xl py-7 px-6 sm:px-8 rounded-2xl border border-slate-800 shadow-2xl">
          {error && (
            <div className="mb-5 p-3.5 rounded-xl bg-rose-950/60 border border-rose-800/60 flex items-start gap-3 text-rose-300 text-xs">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-rose-400" />
              <span>{error}</span>
            </div>
          )}

          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                {t('username')}
              </label>
              <div className="relative rounded-xl">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                  <User className="h-4 w-4" />
                </div>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2.5 rounded-xl bg-slate-800/90 border border-slate-700 text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 transition"
                  placeholder="admin, teacher.ahmad, student.tariq"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                {t('password')}
              </label>
              <div className="relative rounded-xl">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                  <Lock className="h-4 w-4" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2.5 rounded-xl bg-slate-800/90 border border-slate-700 text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 transition"
                  placeholder="••••••••••••"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 flex justify-center items-center gap-2 py-3 px-4 rounded-xl text-sm font-bold text-white bg-gradient-to-r from-brand-600 to-indigo-600 hover:from-brand-500 hover:to-indigo-500 shadow-lg shadow-brand-600/30 transition-all disabled:opacity-50"
            >
              {loading ? 'Authenticating...' : t('login')}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
