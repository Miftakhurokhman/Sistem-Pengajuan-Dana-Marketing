import { useState } from 'react';
import { MOCK_SUBMISSIONS } from '../data/mockSubmissions';
import { SubmissionTable } from '../components/submission/SubmissionTable';
import { Plus, Wallet } from 'lucide-react';

export const SubmissionListPage = () => {
  const [submissions] = useState(MOCK_SUBMISSIONS);

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-6xl mx-auto space-y-6">
        
        {/* Header Halaman Pengajuan Dana */}
        <div className="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Pengajuan Dana Marketing</h1>
            <p className="text-sm text-gray-500">Kelola dan pantau seluruh status pengajuan dana cabang</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 text-sm font-medium transition-all cursor-pointer">
            <Plus className="w-4 h-4" /> Buat Pengajuan Baru
          </button>
        </div>

        {/* Container Tabel */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Wallet className="w-5 h-5 text-blue-600" />
              <h2 className="font-semibold text-gray-800 text-lg">Daftar Pengajuan Dana</h2>
            </div>
          </div>
          
          <SubmissionTable submissions={submissions} />
        </div>

      </div>
    </div>
  );
};