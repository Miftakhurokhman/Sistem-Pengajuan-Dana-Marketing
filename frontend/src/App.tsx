import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { SubmissionListPage } from './pages/SubmissionListPage';
import { SubmissionDetailPage } from './pages/SubmissionDetailPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Route Utama: Halaman Pengajuan Dana */}
        <Route path="/" element={<SubmissionListPage />} />
        
        {/* Route Detail & Action Approval */}
        <Route path="/submission/:id" element={<SubmissionDetailPage />} />
      </Routes>
    </BrowserRouter>
  );
}