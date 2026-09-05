import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { MOCK_SUBMISSIONS } from '../data/mockSubmissions';
import { StatusBadge } from '../components/ui/StatusBadge';
import { 
  SubmissionSearchFilter, 
  type SearchByCategory 
} from '../components/submission/SubmissionSearchFilter';
import { formatRupiah } from '../utils/formatter';
import { FileText, Eye, Inbox } from 'lucide-react';

export const SubmissionListPage = () => {
  const navigate = useNavigate();

  // State Filter & Search
  const [searchBy, setSearchBy] = useState<SearchByCategory>('submissionNo');
  const [searchValue, setSearchValue] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('ALL');

  // Filtering Logic
  const filteredSubmissions = useMemo(() => {
    return MOCK_SUBMISSIONS.filter((item) => {
      // 1. Match Filter Status
      const matchesStatus =
        selectedStatus === 'ALL' || item.status.toUpperCase() === selectedStatus;

      // 2. Match Search Keyword Berdasarkan Kategori
      if (!searchValue.trim()) return matchesStatus;

      const query = searchValue.toLowerCase();
      let matchesCategory = false;

      if (searchBy === 'submissionNo') {
        matchesCategory = item.submissionNo.toLowerCase().includes(query);
      } else if (searchBy === 'applicantName') {
        matchesCategory = item.applicantName.toLowerCase().includes(query);
      } else if (searchBy === 'branchName') {
        matchesCategory = item.branchName.toLowerCase().includes(query);
      }

      return matchesStatus && matchesCategory;
    });
  }, [searchBy, searchValue, selectedStatus]);

  // Handle Reset Filter
  const handleResetFilter = () => {
    setSearchBy('submissionNo');
    setSearchValue('');
    setSelectedStatus('ALL');
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
      
      {/* Header Halaman */}
      <div className="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Daftar Pengajuan Dana</h1>
          <p className="text-sm text-gray-500 mt-1">
            Kelola dan pantau seluruh riwayat pengajuan dana cabang
          </p>
        </div>
      </div>

      {/* Toolbar Filter & Pencarian Dinamis */}
      <SubmissionSearchFilter
        searchBy={searchBy}
        setSearchBy={setSearchBy}
        searchValue={searchValue}
        setSearchValue={setSearchValue}
        selectedStatus={selectedStatus}
        setSelectedStatus={setSelectedStatus}
        onReset={handleResetFilter}
      />

      {/* Table Content */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100 text-[11px] font-bold text-gray-500 uppercase tracking-wider">
                <th className="py-3.5 px-4">No. Pengajuan</th>
                <th className="py-3.5 px-4">Pemohon</th>
                <th className="py-3.5 px-4">Cabang</th>
                <th className="py-3.5 px-4">Nominal</th>
                <th className="py-3.5 px-4">Tanggal</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4 text-center">Aksi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 text-xs">
              {filteredSubmissions.length > 0 ? (
                filteredSubmissions.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50/80 transition-colors">
                    <td className="py-3.5 px-4 font-bold text-gray-800 flex items-center gap-2">
                      <FileText className="w-4 h-4 text-blue-600" />
                      {item.submissionNo}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-gray-700">{item.applicantName}</td>
                    <td className="py-3.5 px-4 text-gray-600">{item.branchName}</td>
                    <td className="py-3.5 px-4 font-bold text-gray-900">{formatRupiah(item.nominal)}</td>
                    <td className="py-3.5 px-4 text-gray-500">{item.createdAt}</td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={item.status} />
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <button
                        onClick={() => navigate(`/submission/${item.id}`)}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 hover:bg-blue-100 text-blue-600 rounded-lg font-semibold text-xs transition-colors cursor-pointer"
                      >
                        <Eye className="w-3.5 h-3.5" /> Detail
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                /* Empty State jika data pencarian tidak ditemukan */
                <tr>
                  <td colSpan={7} className="py-12 text-center bg-white">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <div className="p-3 bg-gray-100 rounded-full text-gray-400">
                        <Inbox className="w-6 h-6" />
                      </div>
                      <p className="text-sm font-semibold text-gray-700">
                        Pengajuan tidak ditemukan
                      </p>
                      <p className="text-xs text-gray-400 max-w-xs">
                        Tidak ada data yang cocok dengan kriteria pencarian atau filter yang Anda pilih.
                      </p>
                      <button
                        onClick={handleResetFilter}
                        className="mt-2 text-xs text-blue-600 hover:text-blue-700 font-semibold cursor-pointer underline"
                      >
                        Bersihkan Filter
                      </button>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
};