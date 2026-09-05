import { useNavigate } from 'react-router-dom';
import type { Submission } from '../../types/submission';
import { StatusBadge } from '../ui/StatusBadge';
import { formatRupiah } from '../../utils/formatter';
import { Eye } from 'lucide-react';

interface SubmissionTableProps {
  submissions: Submission[];
}

export const SubmissionTable = ({ submissions }: SubmissionTableProps) => {
  const navigate = useNavigate(); // Hook untuk navigasi antar halaman

  if (submissions.length === 0) {
    return (
      <div className="p-8 text-center text-gray-500 bg-white rounded-xl border border-gray-100">
        Belum ada data pengajuan dana.
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-gray-600">
          <thead className="bg-gray-50 text-gray-700 uppercase text-xs font-semibold border-b border-gray-200">
            <tr>
              <th className="px-6 py-3">No. Pengajuan</th>
              <th className="px-6 py-3">Pemohon & Cabang</th>
              <th className="px-6 py-3">Keperluan</th>
              <th className="px-6 py-3">Nominal</th>
              <th className="px-6 py-3">Tanggal</th>
              <th className="px-6 py-3">Status</th>
              <th className="px-6 py-3 text-center">Aksi</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {submissions.map((item) => (
              <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 font-medium text-gray-900">{item.submissionNo}</td>
                <td className="px-6 py-4">
                  <div className="font-medium text-gray-800">{item.applicantName}</div>
                  <div className="text-xs text-gray-400">{item.branchName}</div>
                </td>
                <td className="px-6 py-4 max-w-xs truncate" title={item.description}>
                  {item.description}
                </td>
                <td className="px-6 py-4 font-semibold text-gray-800">
                  {formatRupiah(item.nominal)}
                </td>
                <td className="px-6 py-4 text-xs text-gray-500">{item.createdAt}</td>
                <td className="px-6 py-4">
                  <StatusBadge status={item.status} />
                </td>
                <td className="px-6 py-4 text-center">
                  <button
                    onClick={() => navigate(`/submission/${item.id}`)}
                    className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-800 font-medium text-xs bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg transition-colors cursor-pointer"
                  >
                    <Eye className="w-3.5 h-3.5" />
                    Detail / Action
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};