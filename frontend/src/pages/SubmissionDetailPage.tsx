import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MOCK_SUBMISSIONS } from '../data/mockSubmissions';
import { StatusBadge } from '../components/ui/StatusBadge';
import { formatRupiah } from '../utils/formatter';
import { 
  ArrowLeft, 
  CheckCircle2, 
  XCircle, 
  Send, 
  FileText, 
  ExternalLink, 
  Download 
} from 'lucide-react';

export const SubmissionDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [notes, setNotes] = useState('');

  const submission = MOCK_SUBMISSIONS.find((item) => item.id === id);

  if (!submission) {
    return (
      <div className="min-h-screen bg-gray-50 p-8 flex flex-col items-center justify-center">
        <p className="text-gray-500 mb-4">Data pengajuan tidak ditemukan.</p>
        <button
          onClick={() => navigate('/')}
          className="text-blue-600 underline text-sm font-medium"
        >
          Kembali ke Daftar Pengajuan
        </button>
      </div>
    );
  }

  const handleApprove = () => {
    alert(`Pengajuan ${submission.submissionNo} disetujui dengan catatan: "${notes}"`);
    navigate('/');
  };

  const handleReject = () => {
    alert(`Pengajuan ${submission.submissionNo} ditolak dengan catatan: "${notes}"`);
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto space-y-6">
        
        {/* Tombol Kembali */}
        <button
          onClick={() => navigate('/')}
          className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 font-medium transition-colors cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" /> Kembali ke Daftar Pengajuan
        </button>

        {/* Card Detail Pengajuan */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-6">
          <div className="flex justify-between items-start border-b border-gray-100 pb-4">
            <div>
              <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
                Nomor Pengajuan
              </span>
              <h1 className="text-2xl font-bold text-gray-800">{submission.submissionNo}</h1>
            </div>
            <StatusBadge status={submission.status} />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm">
            <div>
              <p className="text-gray-400 text-xs font-medium">Nama Pemohon</p>
              <p className="font-semibold text-gray-800 text-base">{submission.applicantName}</p>
            </div>
            <div>
              <p className="text-gray-400 text-xs font-medium">Cabang</p>
              <p className="font-semibold text-gray-800 text-base">{submission.branchName}</p>
            </div>
            <div>
              <p className="text-gray-400 text-xs font-medium">Nominal Pengajuan</p>
              <p className="font-bold text-blue-600 text-xl">{formatRupiah(submission.nominal)}</p>
            </div>
            <div>
              <p className="text-gray-400 text-xs font-medium">Tanggal Pengajuan</p>
              <p className="font-semibold text-gray-800 text-base">{submission.createdAt}</p>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-4">
            <p className="text-gray-400 text-xs font-medium mb-1">Keperluan / Deskripsi Kegiatan</p>
            <div className="p-4 bg-gray-50 rounded-lg text-gray-700 text-sm leading-relaxed">
              {submission.description}
            </div>
          </div>
        </div>

        {/* Card Render PDF Proposal (Wajib) */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
          <div className="flex items-center justify-between border-b border-gray-100 pb-3">
            <div className="flex items-center gap-2">
              <FileText className="w-5 h-5 text-red-600" />
              <h2 className="font-semibold text-gray-800 text-base">Dokumen Proposal (Wajib)</h2>
            </div>
            <div className="flex items-center gap-2">
              <a
                href={submission.proposalUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 text-xs text-gray-600 hover:text-blue-600 font-medium bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded-lg transition-colors"
              >
                <ExternalLink className="w-3.5 h-3.5" /> Buka Tab Baru
              </a>
              <a
                href={submission.proposalUrl}
                download
                className="inline-flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700 font-medium bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg transition-colors"
              >
                <Download className="w-3.5 h-3.5" /> Unduh
              </a>
            </div>
          </div>

          {/* Container iFrame PDF Viewer */}
          <div className="w-full h-[500px] bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
            <iframe
              src={submission.proposalUrl}
              title={`Proposal-${submission.submissionNo}`}
              className="w-full h-full border-0"
            />
          </div>
        </div>

        {/* Form Action Approval */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
          <h2 className="font-semibold text-gray-800 flex items-center gap-2">
            <Send className="w-4 h-4 text-blue-600" /> Aksi Approval
          </h2>
          
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Catatan / Alasan (Opsional)
            </label>
            <textarea
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Masukkan catatan persetujuan atau alasan penolakan..."
              className="w-full p-3 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              onClick={handleReject}
              className="px-5 py-2.5 rounded-lg border border-red-200 bg-red-50 hover:bg-red-100 text-red-700 font-medium text-sm flex items-center gap-2 transition-colors cursor-pointer"
            >
              <XCircle className="w-4 h-4" /> Tolak Pengajuan
            </button>
            <button
              onClick={handleApprove}
              className="px-5 py-2.5 rounded-lg bg-green-600 hover:bg-green-700 text-white font-medium text-sm flex items-center gap-2 transition-colors cursor-pointer shadow-sm"
            >
              <CheckCircle2 className="w-4 h-4" /> Setujui Pengajuan
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};