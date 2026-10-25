import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { ShieldCheck, Lock, User, AlertCircle, ArrowRight } from 'lucide-react';

export const Login: React.FC = () => {
  const { login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

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

  const fillQuick = (u: string, p: string) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-brand-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="w-16 h-16 rounded-2xl bg-brand-600 flex items-center justify-center text-white shadow-xl shadow-brand-500/30">
            <ShieldCheck className="w-10 h-10" />
          </div>
        </div>
        <h2 className="mt-6 text-center text-3xl font-extrabold text-white tracking-tight">
          {t('system_title')}
        </h2>
        <p className="mt-2 text-center text-sm text-slate-400">
          Sign in to access your authorized attendance portal
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4">
        <div className="bg-white/95 backdrop-blur-xl py-8 px-6 shadow-2xl rounded-2xl sm:px-10 border border-white/20">
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-3 text-rose-700 text-sm">
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label className="block text-sm font-semibold text-slate-700">
                {t('username')}
              </label>
              <div className="mt-1 relative rounded-xl shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <User className="h-5 w-5" />
                </div>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="block w-full pl-11 pr-3 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 text-sm transition"
                  placeholder="admin, teacher.ahmad, student.tariq"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-700">
                {t('password')}
              </label>
              <div className="mt-1 relative rounded-xl shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="h-5 w-5" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full pl-11 pr-3 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 text-sm transition"
                  placeholder="••••••••••••"
                />
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={loading}
                className="w-full flex justify-center items-center gap-2 py-3 px-4 border border-transparent rounded-xl shadow-md text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500 disabled:opacity-50 transition"
              >
                {loading ? 'Authenticating...' : t('login')}
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </form>

          {/* Quick Demo Fill Buttons */}
          <div className="mt-8 pt-6 border-t border-slate-200">
            <p className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-3 text-center">
              Quick Demo Access
            </p>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => fillQuick('admin', 'Password@123456')}
                className="px-2.5 py-2 text-xs font-bold rounded-lg bg-slate-100 hover:bg-brand-50 hover:text-brand-700 text-slate-700 border border-slate-200 transition text-center"
              >
                Admin
              </button>
              <button
                type="button"
                onClick={() => fillQuick('teacher.ahmad', 'Password@123456')}
                className="px-2.5 py-2 text-xs font-bold rounded-lg bg-slate-100 hover:bg-brand-50 hover:text-brand-700 text-slate-700 border border-slate-200 transition text-center"
              >
                Teacher
              </button>
              <button
                type="button"
                onClick={() => fillQuick('student.tariq', 'Password@123456')}
                className="px-2.5 py-2 text-xs font-bold rounded-lg bg-slate-100 hover:bg-brand-50 hover:text-brand-700 text-slate-700 border border-slate-200 transition text-center"
              >
                Student
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
