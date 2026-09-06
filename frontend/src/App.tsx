import type { ReactNode } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { MainLayout } from './components/layout/MainLayout';
import { SubmissionListPage } from './pages/SubmissionListPage';
import { SubmissionDetailPage } from './pages/SubmissionDetailPage';
import { CreateSubmissionPage } from './pages/CreateSubmissionPage';
import { LoginPage } from './pages/LoginPage';
import { isAuthenticated } from './utils/auth';

const ProtectedRoute = ({ children }: { children: ReactNode }) => {
  return isAuthenticated() ? <>{children}</> : <Navigate to="/login" replace />;
};

const PublicRoute = ({ children }: { children: ReactNode }) => {
  return !isAuthenticated() ? <>{children}</> : <Navigate to="/" replace />;
};

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />

        <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route path="/" element={<SubmissionListPage />} />
          <Route path="/submission/new" element={<CreateSubmissionPage />} />
          <Route path="/submission/:id" element={<SubmissionDetailPage />} />
        </Route>

        <Route path="*" element={<Navigate to={isAuthenticated() ? '/' : '/login'} replace />} />
      </Routes>
    </BrowserRouter>
  );
}