import React, { useEffect, useState } from 'react';
import { apiRequest } from '../../lib/api';
import { AntiFraudPolicyConfig, VerificationPolicyEntity } from '../../types';
import {
  ShieldCheck,
  QrCode,
  KeyRound,
  MapPin,
  Clock,
  Laptop,
  AlertTriangle,
  Save,
  CheckCircle2
} from 'lucide-react';

export const AntiFraudConfig: React.FC = () => {
  const [policyEntity, setPolicyEntity] = useState<VerificationPolicyEntity | null>(null);
  const [config, setConfig] = useState<AntiFraudPolicyConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  useEffect(() => {
    fetchPolicies();
  }, []);

  const fetchPolicies = async () => {
    try {
      const list = await apiRequest<VerificationPolicyEntity[]>('/verification/policies');
      if (list && list.length > 0) {
        const active = list[0];
        setPolicyEntity(active);
        setConfig(JSON.parse(active.policyConfiguration));
      }
    } catch (err) {
      console.error('Failed to load anti-fraud policies', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!config || !policyEntity) return;
    setSaving(true);
    setSaveSuccess(false);

    try {
      await apiRequest(`/verification/policies/${policyEntity.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          policyConfiguration: JSON.stringify(config),
        }),
      });
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err: any) {
      alert(`Failed to save policy: ${err.message}`);
    } finally {
      setSaving(false);
    }
  };

  if (loading || !config) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-black text-slate-900 tracking-tight">Anti-Fraud Engine Configuration</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-brand-100 text-brand-800">
              v{policyEntity?.version || 1} Active
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Independently configure, enable, or tune verification factors. Rules apply deterministically.
          </p>
        </div>
        <button
          onClick={handleSave}
          disabled={saving}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm shadow-md transition disabled:opacity-50"
        >
          {saving ? (
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
          ) : saveSuccess ? (
            <CheckCircle2 className="w-4 h-4 text-emerald-300" />
          ) : (
            <Save className="w-4 h-4" />
          )}
          {saveSuccess ? 'Saved Successfully!' : 'Save & Deploy Policy'}
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Method A: Registered Classroom Device Verification */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
                  <Laptop className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-extrabold text-slate-900 text-base">Method A: Registered Classroom PC</h3>
                  <span className="text-xs text-slate-500">School terminal certificate / kiosk mode</span>
                </div>
              </div>
              <input
                type="checkbox"
                checked={config.deviceCheck.enabled}
                onChange={(e) =>
                  setConfig({
                    ...config,
                    deviceCheck: { ...config.deviceCheck, enabled: e.target.checked },
                  })
                }
                className="w-5 h-5 text-brand-600 rounded focus:ring-brand-500 cursor-pointer"
              />
            </div>

            <div className="mt-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1">
                  Enforcement Level
                </label>
                <select
                  disabled={!config.deviceCheck.enabled}
                  value={config.deviceCheck.required ? 'REQUIRED' : 'OPTIONAL'}
                  onChange={(e) =>
                    setConfig({
                      ...config,
                      deviceCheck: { ...config.deviceCheck, required: e.target.value === 'REQUIRED' },
                    })
                  }
                  className="w-full text-sm rounded-xl border-slate-300 py-2 px-3 border focus:ring-brand-500 disabled:opacity-50"
                >
                  <option value="OPTIONAL">Optional (Advisory risk scoring)</option>
                  <option value="REQUIRED">Required (Blocks check-in on personal device)</option>
                </select>
              </div>

              <div className="p-3 rounded-xl bg-slate-50 text-xs text-slate-600 border border-slate-200 flex items-start gap-2">
                <AlertTriangle className="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
                <span>
                  Never relies on spoofable browser fingerprints. Validates cryptographic classroom kiosk certificates.
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Method B: Dynamic Rotating QR Code */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
                  <QrCode className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-extrabold text-slate-900 text-base">Method B: Rotating Dynamic QR Code</h3>
                  <span className="text-xs text-slate-500">Server-authoritative timed challenge</span>
                </div>
              </div>
              <input
                type="checkbox"
                checked={config.qrCheck.enabled}
                onChange={(e) =>
                  setConfig({
                    ...config,
                    qrCheck: { ...config.qrCheck, enabled: e.target.checked },
                  })
                }
                className="w-5 h-5 text-brand-600 rounded focus:ring-brand-500 cursor-pointer"
              />
            </div>

            <div className="mt-6 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1">
                    Enforcement
                  </label>
                  <select
                    disabled={!config.qrCheck.enabled}
                    value={config.qrCheck.required ? 'REQUIRED' : 'OPTIONAL'}
                    onChange={(e) =>
                      setConfig({
                        ...config,
                        qrCheck: { ...config.qrCheck, required: e.target.value === 'REQUIRED' },
                      })
                    }
                    className="w-full text-sm rounded-xl border-slate-300 py-2 px-3 border focus:ring-brand-500 disabled:opacity-50"
                  >
                    <option value="REQUIRED">Required</option>
                    <option value="OPTIONAL">Optional</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1">
                    Rotation Interval
                  </label>
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      min={5}
                      max={60}
                      disabled={!config.qrCheck.enabled}
                      value={config.qrCheck.rotationSeconds}
                      onChange={(e) =>
                        setConfig({
                          ...config,
                          qrCheck: { ...config.qrCheck, rotationSeconds: parseInt(e.target.value) || 15 },
                        })
                      }
                      className="w-full text-sm rounded-xl border-slate-300 py-2 px-3 border focus:ring-brand-500 disabled:opacity-50"
                    />
                    <span className="text-xs text-slate-500 font-bold">sec</span>
                  </div>
                </div>
              </div>

              <div className="p-3 rounded-xl bg-purple-50 text-xs text-purple-900 border border-purple-200 flex items-start gap-2">
                <CheckCircle2 className="w-4 h-4 text-purple-600 shrink-0 mt-0.5" />
                <span>
                  Default 15s interval aggressively prevents screenshot sharing over group chats to absent friends.
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Method C: Ephemeral 6-Digit Code */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                  <KeyRound className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-extrabold text-slate-900 text-base">Method C: 6-Digit Challenge Code</h3>
                  <span className="text-xs text-slate-500">Displayed beside QR code on screen</span>
                </div>
              </div>
              <input
                type="checkbox"
                checked={config.sixDigitCheck.enabled}
                onChange={(e) =>
                  setConfig({
                    ...config,
                    sixDigitCheck: { ...config.sixDigitCheck, enabled: e.target.checked },
                  })
                }
                className="w-5 h-5 text-brand-600 rounded focus:ring-brand-500 cursor-pointer"
              />
            </div>

            <div className="mt-6 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1">
                    Enforcement
                  </label>
                  <select
                    disabled={!config.sixDigitCheck.enabled}
                    value={config.sixDigitCheck.required ? 'REQUIRED' : 'OPTIONAL'}
                    onChange={(e) =>
                      setConfig({
                        ...config,
                        sixDigitCheck: { ...config.sixDigitCheck, required: e.target.value === 'REQUIRED' },
                      })
                    }
                    className="w-full text-sm rounded-xl border-slate-300 py-2 px-3 border focus:ring-brand-500 disabled:opacity-50"
                  >
                    <option value="OPTIONAL">Optional</option>
                    <option value="REQUIRED">Required</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1">
                    Expiration
                  </label>
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      min={10}
                      max={120}
                      disabled={!config.sixDigitCheck.enabled}
                      value={config.sixDigitCheck.expirationSeconds}
                      onChange={(e) =>
                        setConfig({
                          ...config,
                          sixDigitCheck: {
                            ...config.sixDigitCheck,
                            expirationSeconds: parseInt(e.target.value) || 30,
                          },
                        })
                      }
                      className="w-full text-sm rounded-xl border-slate-300 py-2 px-3 border focus:ring-brand-500 disabled:opacity-50"
                    />
                    <span className="text-xs text-slate-500 font-bold">sec</span>
                  </div>
                </div>
              </div>

              <div className="p-3 rounded-xl bg-slate-50 text-xs text-slate-600 border border-slate-200 flex items-start gap-2">
                <AlertTriangle className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
                <span>Rate limited to 5 attempts with exponential backoff to prevent brute-force guessing.</span>
              </div>
            </div>
          </div>
        </div>

        {/* Method D: GPS Geofencing (Optional/Advisory) */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
                  <MapPin className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-extrabold text-slate-900 text-base">Method D: GPS Geofencing</h3>
                  <span className="text-xs text-slate-500">School perimeter bounding</span>
                </div>
              </div>
              <input
                type="checkbox"
                checked={config.gpsCheck.enabled}
                onChange={(e) =>
                  setConfig({
                    ...config,
                    gpsCheck: { ...config.gpsCheck, enabled: e.target.checked },
                  })
                }
                className="w-5 h-5 text-brand-600 rounded focus:ring-brand-500 cursor-pointer"
              />
            </div>

            <div className="mt-6 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1">
                    Enforcement
                  </label>
                  <select
                    disabled={!config.gpsCheck.enabled}
                    value={config.gpsCheck.required ? 'REQUIRED' : 'OPTIONAL'}
                    onChange={(e) =>
                      setConfig({
                        ...config,
                        gpsCheck: { ...config.gpsCheck, required: e.target.value === 'REQUIRED' },
                      })
                    }
                    className="w-full text-sm rounded-xl border-slate-300 py-2 px-3 border focus:ring-brand-500 disabled:opacity-50"
                  >
                    <option value="OPTIONAL">Optional / Advisory</option>
                    <option value="REQUIRED">Required</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1">
                    Radius Limit
                  </label>
                  <div className="flex items-center gap-2">
                    <input
                      type="number"
                      min={50}
                      max={1000}
                      disabled={!config.gpsCheck.enabled}
                      value={config.gpsCheck.radiusMeters}
                      onChange={(e) =>
                        setConfig({
                          ...config,
                          gpsCheck: { ...config.gpsCheck, radiusMeters: parseFloat(e.target.value) || 150 },
                        })
                      }
                      className="w-full text-sm rounded-xl border-slate-300 py-2 px-3 border focus:ring-brand-500 disabled:opacity-50"
                    />
                    <span className="text-xs text-slate-500 font-bold">meters</span>
                  </div>
                </div>
              </div>

              <div className="p-3 rounded-xl bg-amber-50 text-xs text-amber-900 border border-amber-200 flex items-start gap-2">
                <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
                <span>
                  Important: Classroom PCs lack GPS hardware. Optional mode ensures valid attendance is not blocked.
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Initial 2-Week Manual Verification Mode */}
        <div className="md:col-span-2 bg-gradient-to-r from-slate-900 to-brand-950 text-white p-6 rounded-2xl shadow-sm border border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-white/10 text-white flex items-center justify-center shrink-0">
              <Clock className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-extrabold text-lg tracking-tight">Initial 2-Week Manual Verification Window</h3>
              <p className="text-xs text-slate-300 max-w-xl mt-1 leading-relaxed">
                During deployment or pilot start, attendance submissions are held in Pending Confirmation status for
                authorized teachers to visually confirm student presence before finalization.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-4 shrink-0">
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">
                Duration (Days)
              </label>
              <input
                type="number"
                min={1}
                max={60}
                value={config.manualReview.initialPeriodDays}
                onChange={(e) =>
                  setConfig({
                    ...config,
                    manualReview: {
                      ...config.manualReview,
                      initialPeriodDays: parseInt(e.target.value) || 14,
                    },
                  })
                }
                className="w-24 text-sm font-bold text-slate-900 rounded-xl border-none py-2 px-3"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">
                Active State
              </label>
              <input
                type="checkbox"
                checked={config.manualReview.enabled}
                onChange={(e) =>
                  setConfig({
                    ...config,
                    manualReview: { ...config.manualReview, enabled: e.target.checked },
                  })
                }
                className="w-6 h-6 text-brand-500 rounded focus:ring-brand-400 cursor-pointer"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
