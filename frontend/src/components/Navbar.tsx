import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { getPendingOfflineRecords, syncOfflineRecords } from '../lib/offlineDb';
import { ShieldCheck, LogOut, Globe, Wifi, WifiOff, RefreshCw, User } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, role, logout } = useAuth();
  const { lang, setLang, t } = useLanguage();
  const navigate = useNavigate();

  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [pendingCount, setPendingCount] = useState(0);
  const [isSyncing, setIsSyncing] = useState(false);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    const checkPending = async () => {
      const records = await getPendingOfflineRecords();
      setPendingCount(records.length);
    };

    checkPending();
    const interval = setInterval(checkPending, 10000);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearInterval(interval);
    };
  }, []);

  const handleSync = async () => {
    try {
      setIsSyncing(true);
      const res = await syncOfflineRecords();
      setPendingCount(0);
      alert(`Synchronized ${res.synced} offline record(s) successfully.`);
    } catch (err: any) {
      alert(`Sync failed: ${err.message}`);
    } finally {
      setIsSyncing(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-50 bg-white/95 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Title */}
          <Link to="/" className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-700 to-brand-500 flex items-center justify-center text-white shadow-md shadow-brand-500/20">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <span className="font-black text-slate-900 tracking-tight text-xl block leading-none">
                VeriAttend
              </span>
              <span className="text-[11px] text-brand-600 font-bold tracking-wider uppercase mt-1 block">
                {role ? t(`role_${role.toLowerCase()}`) : 'Smart Attendance Platform'}
              </span>
            </div>
          </Link>

          {/* Actions & Utilities */}
          <div className="flex items-center gap-3">
            {/* Offline & Sync Status */}
            <div className="hidden sm:flex items-center gap-2">
              {isOnline ? (
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
                  <Wifi className="w-3.5 h-3.5" /> Online
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
                  <WifiOff className="w-3.5 h-3.5" /> Offline Mode
                </span>
              )}

              {pendingCount > 0 && (
                <button
                  onClick={handleSync}
                  disabled={isSyncing || !isOnline}
                  className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-brand-50 text-brand-700 hover:bg-brand-100 border border-brand-200 transition"
                  title="Click to synchronize offline records"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${isSyncing ? 'animate-spin' : ''}`} />
                  {pendingCount} Pending Sync
                </button>
              )}
            </div>

            {/* Language Switcher */}
            <button
              onClick={() => setLang(lang === 'en' ? 'ar' : 'en')}
              className="p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition flex items-center gap-1.5 text-xs font-bold"
              title="Change Language"
            >
              <Globe className="w-4 h-4" />
              <span>{lang === 'en' ? 'العربية' : 'English'}</span>
            </button>

            {/* User Profile & Logout */}
            {user ? (
              <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
                <div className="hidden md:block text-right">
                  <div className="text-sm font-semibold text-slate-900 leading-none">{user.fullName}</div>
                  <div className="text-xs text-slate-500 font-mono mt-0.5">{user.identifier}</div>
                </div>
                <button
                  onClick={handleLogout}
                  className="p-2 rounded-lg text-rose-600 hover:bg-rose-50 transition"
                  title={t('logout')}
                >
                  <LogOut className="w-5 h-5" />
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-700 text-white font-medium text-sm shadow-sm transition"
              >
                <User className="w-4 h-4" />
                {t('login')}
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
