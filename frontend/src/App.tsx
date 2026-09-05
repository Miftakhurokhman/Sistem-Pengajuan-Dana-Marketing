import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { MainLayout } from './components/layout/MainLayout';
import { SubmissionListPage } from './pages/SubmissionListPage';
import { SubmissionDetailPage } from './pages/SubmissionDetailPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Route dengan Layout Wrapper (Sidebar & TopBar) */}
        <Route element={<MainLayout />}>
          <Route path="/" element={<SubmissionListPage />} />
          <Route path="/submission/:id" element={<SubmissionDetailPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}