import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { DemoBar } from './components/DemoBar';
import { Login } from './pages/Login';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { AntiFraudConfig } from './pages/admin/AntiFraudConfig';
import { ExcelImport } from './pages/admin/ExcelImport';
import { TeacherDashboard } from './pages/teacher/TeacherDashboard';
import { AttendanceSessionDisplay } from './pages/teacher/AttendanceSessionDisplay';
import { StudentDashboard } from './pages/student/StudentDashboard';

const ProtectedRoute: React.FC<{ children: React.ReactNode; allowedRoles?: string[] }> = ({
  children,
  allowedRoles,
}) => {
  const { user, role, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50">
        <div className="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && role && !allowedRoles.includes(role)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

const RoleHomeRedirect: React.FC = () => {
  const { role, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50">
        <div className="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!user) return <Navigate to="/login" replace />;
  if (role === 'ADMIN') return <Navigate to="/admin" replace />;
  if (role === 'TEACHER') return <Navigate to="/teacher" replace />;
  return <Navigate to="/student" replace />;
};

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <div className="min-h-screen flex flex-col bg-slate-50">
        <Routes>
          {/* Full Screen Teacher Projector Display (No Navbar) */}
          <Route
            path="/teacher/session/:sessionId"
            element={
              <ProtectedRoute allowedRoles={['TEACHER', 'ADMIN']}>
                <AttendanceSessionDisplay />
              </ProtectedRoute>
            }
          />

          {/* Standard Layout with Navbar */}
          <Route
            path="*"
            element={
              <>
                <DemoBar />
                <Navbar />
                <main className="flex-1">
                  <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/" element={<RoleHomeRedirect />} />

                    {/* Admin Routes */}
                    <Route
                      path="/admin"
                      element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                          <AdminDashboard />
                        </ProtectedRoute>
                      }
                    />
                    <Route
                      path="/admin/anti-fraud"
                      element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                          <AntiFraudConfig />
                        </ProtectedRoute>
                      }
                    />
                    <Route
                      path="/admin/excel-import"
                      element={
                        <ProtectedRoute allowedRoles={['ADMIN']}>
                          <ExcelImport />
                        </ProtectedRoute>
                      }
                    />

                    {/* Teacher Routes */}
                    <Route
                      path="/teacher"
                      element={
                        <ProtectedRoute allowedRoles={['TEACHER', 'ADMIN']}>
                          <TeacherDashboard />
                        </ProtectedRoute>
                      }
                    />

                    {/* Student Routes */}
                    <Route
                      path="/student"
                      element={
                        <ProtectedRoute allowedRoles={['STUDENT']}>
                          <StudentDashboard />
                        </ProtectedRoute>
                      }
                    />

                    <Route path="*" element={<Navigate to="/" replace />} />
                  </Routes>
                </main>
              </>
            }
          />
        </Routes>
      </div>
    </BrowserRouter>
  );
};
