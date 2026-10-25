import React, { useState, useEffect } from 'react';
import { apiRequest } from '../../lib/api';
import {
  Download,
  Upload,
  CheckCircle2,
  AlertCircle,
  FileSpreadsheet,
  ArrowRight,
  Sparkles,
  Info,
  Check,
  FileText
} from 'lucide-react';

export const ExcelImport: React.FC = () => {
  const [academicYears, setAcademicYears] = useState<any[]>([]);
  const [selectedYearId, setSelectedYearId] = useState<string>('');
  const [file, setFile] = useState<File | null>(null);

  const [previewData, setPreviewData] = useState<any | null>(null);
  const [loading, setLoading] = useState(false);
  const [committing, setCommitting] = useState(false);
  const [successResult, setSuccessResult] = useState<any | null>(null);
  const [downloadingFormat, setDownloadingFormat] = useState<string | null>(null);

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

  const handleDownloadTemplate = async (format: 'xlsx' | 'csv') => {
    try {
      setDownloadingFormat(format);
      const token = localStorage.getItem('token');
      const url = `/api/v1/students/import/template?format=${format}&token=${token || ''}`;
      window.open(url, '_blank');
    } finally {
      setTimeout(() => setDownloadingFormat(null), 1000);
    }
  };

  const handleLoadSampleData = () => {
    const sampleStudents = [
      {
        row: 2,
        studentNumber: 'STU-3001',
        fullName: 'Zaid Al-Husseini',
        grade: 'Grade 10',
        section: 'Section A',
        email: 'zaid.h@student.school.edu',
        phone: '+962791234567',
        status: 'READY'
      },
      {
        row: 3,
        studentNumber: 'STU-3002',
        fullName: 'Rania Al-Majali',
        grade: 'Grade 10',
        section: 'Section A',
        email: 'rania.m@student.school.edu',
        phone: '+962792345678',
        status: 'READY'
      },
      {
        row: 4,
        studentNumber: 'STU-3003',
        fullName: 'Kareem Nabulsi',
        grade: 'Grade 10',
        section: 'Section B',
        email: 'kareem.n@student.school.edu',
        phone: '+962793456789',
        status: 'READY'
      },
      {
        row: 5,
        studentNumber: 'STU-3004',
        fullName: 'Fatima Mansour',
        grade: 'Grade 10',
        section: 'Section B',
        email: 'fatima.m@student.school.edu',
        phone: '+962794567890',
        status: 'READY'
      }
    ];

    setPreviewData({
      validCount: sampleStudents.length,
      errorCount: 0,
      validRecords: sampleStudents,
      errors: []
    });
    setSuccessResult(null);
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
      alert(`Validation preview failed: ${err.message}`);
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
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-100 mb-2">
            <FileSpreadsheet className="w-3.5 h-3.5" />
            Bulk Academic Ingestion
          </div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">Student Import Wizard</h1>
          <p className="text-sm text-slate-500 mt-1">
            Download standard templates, validate spreadsheets with schema protection, or load test data in 1 click.
          </p>
        </div>

        {/* Download Buttons Card */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={() => handleDownloadTemplate('xlsx')}
            disabled={downloadingFormat === 'xlsx'}
            className="inline-flex items-center gap-2 px-3.5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-sm transition"
            title="Download formatted Excel (.xlsx) template with sample columns"
          >
            <Download className="w-4 h-4" />
            <span>Download Excel (.xlsx)</span>
          </button>

          <button
            onClick={() => handleDownloadTemplate('csv')}
            disabled={downloadingFormat === 'csv'}
            className="inline-flex items-center gap-2 px-3.5 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs shadow-sm transition"
            title="Download lightweight CSV (.csv) template"
          >
            <FileText className="w-4 h-4" />
            <span>Download CSV (.csv)</span>
          </button>
        </div>
      </div>

      {/* Success Banner */}
      {successResult && (
        <div className="p-6 rounded-2xl bg-emerald-50 border border-emerald-200 flex items-start gap-4 shadow-sm animate-in fade-in">
          <CheckCircle2 className="w-8 h-8 text-emerald-600 shrink-0 mt-0.5" />
          <div className="min-w-0 flex-1">
            <h3 className="font-extrabold text-emerald-900 text-lg">Import Completed Successfully</h3>
            <p className="text-sm text-emerald-700 mt-1">
              {successResult.importedCount} student records and grade enrollments were safely committed to the database.
            </p>
            <div className="mt-3 flex items-center gap-2">
              <button
                onClick={() => setSuccessResult(null)}
                className="px-3 py-1.5 rounded-lg bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 transition"
              >
                Import More Records
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 1-Click Test Data vs Upload Box */}
      {!previewData && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 1-Click Test Utility */}
          <div className="lg:col-span-1 bg-gradient-to-br from-indigo-900 via-brand-900 to-slate-900 text-white p-6 rounded-2xl shadow-md border border-indigo-800/50 flex flex-col justify-between">
            <div>
              <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-indigo-500/20 text-indigo-300 font-bold text-xs uppercase tracking-wider mb-3">
                <Sparkles className="w-3.5 h-3.5 text-amber-300 animate-pulse" />
                Zero-Friction Testing
              </div>
              <h3 className="text-lg font-bold text-white">Instant Sample Dataset</h3>
              <p className="text-xs text-indigo-200/90 mt-2 leading-relaxed">
                Want to test the import workflow right now without creating an Excel file?
                Load 4 pre-formatted students directly into the preview pipeline.
              </p>
            </div>

            <button
              type="button"
              onClick={handleLoadSampleData}
              className="mt-6 w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-white hover:bg-indigo-50 text-indigo-900 font-bold text-xs shadow-md transition"
            >
              <Sparkles className="w-4 h-4 text-indigo-600" />
              <span>Load 4 Sample Students Now</span>
            </button>
          </div>

          {/* Real Upload Box */}
          <div className="lg:col-span-2 bg-white p-6 sm:p-8 rounded-2xl shadow-sm border border-slate-200">
            <h3 className="text-lg font-bold text-slate-900 mb-1">Upload Spreadsheet File</h3>
            <p className="text-xs text-slate-500 mb-6">
              Upload your school's .xlsx or .csv roster. Columns will be automatically mapped and sanitized.
            </p>

            <form onSubmit={handlePreview} className="space-y-5">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-1.5">
                    Target Academic Year
                  </label>
                  <select
                    value={selectedYearId}
                    onChange={(e) => setSelectedYearId(e.target.value)}
                    className="w-full text-sm rounded-xl border border-slate-300 py-2.5 px-3 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 bg-white"
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
                    File Attachment (.xlsx or .csv)
                  </label>
                  <input
                    type="file"
                    accept=".xlsx,.csv"
                    required
                    onChange={(e) => setFile(e.target.files?.[0] || null)}
                    className="block w-full text-sm text-slate-500 file:mr-3 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-bold file:bg-brand-50 file:text-brand-700 hover:file:bg-brand-100 cursor-pointer"
                  />
                </div>
              </div>

              {/* Guidelines Info */}
              <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-600 flex items-start gap-2.5">
                <Info className="w-4 h-4 text-brand-600 shrink-0 mt-0.5" />
                <span>
                  Supported headers: <strong>Student Number</strong>, <strong>Full Name</strong>, <strong>Grade</strong>, <strong>Section</strong>, <strong>Email</strong>, <strong>Phone</strong>.
                  Formula injection tags (=, +, -, @) are automatically neutralized.
                </span>
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
                {loading ? 'Validating & Parsing Spreadsheet...' : 'Validate and Preview Records'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Preview Table */}
      {previewData && (
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 space-y-6 animate-in fade-in">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-black text-slate-900">Import Validation Staging Grid</h2>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
                  {previewData.validCount} Valid
                </span>
                {previewData.errorCount > 0 && (
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-100 text-rose-800">
                    {previewData.errorCount} Issues
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-500 mt-1">
                Verify the student details below before saving them to the school database.
              </p>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setPreviewData(null)}
                className="px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition"
              >
                Reset / Back
              </button>
              <button
                type="button"
                onClick={handleConfirm}
                disabled={committing || previewData.validCount === 0}
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs uppercase tracking-wide shadow-md transition disabled:opacity-50"
              >
                {committing ? (
                  <>
                    <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    <span>Committing Records...</span>
                  </>
                ) : (
                  <>
                    <span>Confirm Import ({previewData.validCount} Students)</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
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
                  <th className="px-4 py-3">Grade & Section</th>
                  <th className="px-4 py-3">Contact Email</th>
                  <th className="px-4 py-3">Validation Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {previewData.validRecords.map((r: any, idx: number) => (
                  <tr key={idx} className="hover:bg-slate-50 transition">
                    <td className="px-4 py-3 font-mono text-xs text-slate-500">{r.row || idx + 1}</td>
                    <td className="px-4 py-3 font-mono font-bold text-slate-900">{r.studentNumber}</td>
                    <td className="px-4 py-3 font-semibold text-slate-800">{r.fullName}</td>
                    <td className="px-4 py-3 text-slate-600">
                      <span className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-slate-100 text-slate-700">
                        {r.grade} • {r.section}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs font-mono text-slate-500">{r.email || '—'}</td>
                    <td className="px-4 py-3">
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                        <Check className="w-3 h-3 text-emerald-600" /> Ready to Import
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
