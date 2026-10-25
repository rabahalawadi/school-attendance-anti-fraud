import React, { useState, useEffect } from 'react';
import { apiRequest } from '../../lib/api';
import { Download, Upload, CheckCircle2, AlertCircle, FileSpreadsheet, ArrowRight } from 'lucide-react';

export const ExcelImport: React.FC = () => {
  const [academicYears, setAcademicYears] = useState<any[]>([]);
  const [selectedYearId, setSelectedYearId] = useState<string>('');
  const [file, setFile] = useState<File | null>(null);

  const [previewData, setPreviewData] = useState<any | null>(null);
  const [loading, setLoading] = useState(false);
  const [committing, setCommitting] = useState(false);
  const [successResult, setSuccessResult] = useState<any | null>(null);

  useEffect(() => {
    fetchAcademicYears();
  }, []);

  const fetchAcademicYears = async () => {
    try {
      const list = await apiRequest<any[]>('/academic-years');
      setAcademicYears(list);
      if (list.length > 0) {
        setSelectedYearId(list[0].id);
      }
    } catch (err) {
      console.error('Failed to load academic years', err);
    }
  };

  const handleDownloadTemplate = () => {
    const token = localStorage.getItem('token');
    window.open(`/api/v1/students/import/template?token=${token}`, '_blank');
  };

  const handlePreview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file || !selectedYearId) return;

    setLoading(true);
    setPreviewData(null);
    setSuccessResult(null);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('academicYearId', selectedYearId);

    try {
      const data = await apiRequest<any>('/students/import/preview', {
        method: 'POST',
        body: formData,
      });
      setPreviewData(data);
    } catch (err: any) {
      alert(`Preview failed: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async () => {
    if (!previewData || !selectedYearId) return;

    setCommitting(true);
    try {
      const res = await apiRequest<any>('/students/import/confirm', {
        method: 'POST',
        body: JSON.stringify({
          records: previewData.validRecords,
          academicYearId: selectedYearId,
        }),
      });
      setSuccessResult(res);
      setPreviewData(null);
      setFile(null);
    } catch (err: any) {
      alert(`Import commit failed: ${err.message}`);
    } finally {
      setCommitting(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">Student Excel Import Wizard</h1>
          <p className="text-sm text-slate-500 mt-1">
            Bulk import student accounts and classroom enrollments with schema validation and duplicate checks
          </p>
        </div>
        <button
          onClick={handleDownloadTemplate}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-sm border border-slate-200 transition"
        >
          <Download className="w-4 h-4" />
          Download Sample .xlsx
        </button>
      </div>

      {/* Success Banner */}
      {successResult && (
        <div className="p-6 rounded-2xl bg-emerald-50 border border-emerald-200 flex items-start gap-4">
          <CheckCircle2 className="w-8 h-8 text-emerald-600 shrink-0" />
          <div>
            <h3 className="font-extrabold text-emerald-900 text-lg">Import Completed Successfully</h3>
            <p className="text-sm text-emerald-700 mt-1">
              {successResult.importedCount} student records and enrollments committed to database without duplicates.
            </p>
          </div>
        </div>
      )}

      {/* Upload Box */}
      {!previewData && (
        <form onSubmit={handlePreview} className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1.5">
                Target Academic Year
              </label>
              <select
                value={selectedYearId}
                onChange={(e) => setSelectedYearId(e.target.value)}
                className="w-full text-sm rounded-xl border border-slate-300 py-2.5 px-3 focus:ring-brand-500"
              >
                {academicYears.map((y) => (
                  <option key={y.id} value={y.id}>
                    {y.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1.5">
                Select .xlsx or .csv Spreadsheet
              </label>
              <input
                type="file"
                accept=".xlsx,.csv"
                required
                onChange={(e) => setFile(e.target.files?.[0] || null)}
                className="block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-bold file:bg-brand-50 file:text-brand-700 hover:file:bg-brand-100 cursor-pointer"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || !file}
            className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm shadow-md transition disabled:opacity-50"
          >
            {loading ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Upload className="w-4 h-4" />
            )}
            {loading ? 'Analyzing Spreadsheet...' : 'Validate and Preview Records'}
          </button>
        </form>
      )}

      {/* Preview Table */}
      {previewData && (
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
            <div>
              <h2 className="text-lg font-black text-slate-900">Pre-Import Validation Report</h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Found {previewData.validCount} valid record(s) and {previewData.errorCount} issue(s)
              </p>
            </div>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setPreviewData(null)}
                className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs uppercase transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirm}
                disabled={committing || previewData.validCount === 0}
                className="inline-flex items-center gap-2 px-5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs uppercase shadow-md transition disabled:opacity-50"
              >
                {committing ? 'Importing...' : `Confirm Import (${previewData.validCount} Students)`}
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="overflow-x-auto rounded-xl border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-xs font-bold text-slate-600 uppercase tracking-wider">
                <tr>
                  <th className="px-4 py-3">Row</th>
                  <th className="px-4 py-3">Student Number</th>
                  <th className="px-4 py-3">Full Name</th>
                  <th className="px-4 py-3">Class & Section</th>
                  <th className="px-4 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {previewData.validRecords.map((r: any, idx: number) => (
                  <tr key={idx} className="hover:bg-slate-50">
                    <td className="px-4 py-2.5 font-mono text-xs text-slate-500">{r.row}</td>
                    <td className="px-4 py-2.5 font-mono font-bold text-slate-900">{r.studentNumber}</td>
                    <td className="px-4 py-2.5 font-semibold text-slate-800">{r.fullName}</td>
                    <td className="px-4 py-2.5 text-slate-600">{r.grade} - {r.section}</td>
                    <td className="px-4 py-2.5">
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                        <CheckCircle2 className="w-3 h-3" /> Valid
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
