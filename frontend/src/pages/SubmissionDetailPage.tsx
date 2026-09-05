import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MOCK_SUBMISSIONS } from '../data/mockSubmissions';
import { StatusBadge } from '../components/ui/StatusBadge';
import { ApprovalTimeline } from '../components/submission/ApprovalTimeline';
import { ApprovalActionModal } from '../components/submission/ApprovalActionModal';
import { formatRupiah } from '../utils/formatter';
import type { ApprovalHistory } from '../types/submission';
import { 
  ArrowLeft, 
  CheckCircle2, 
  XCircle, 
  FileText, 
  ExternalLink, 
  Download,
  History,
  Info
} from 'lucide-react';

const MOCK_APPROVAL_HISTORY: ApprovalHistory[] = [
  {
    id: 1,
    role: 'PIC Sales (Pemohon)',
    approverName: 'Ahmad Subagja',
    status: 'Submitted',
    date: '10 Mei 2026, 09:15 WIB',
    notes: 'Pengajuan dana untuk event pameran otomotif Mall Kelapa Gading.',
  },
  {
    id: 2,
    role: 'Branch Relationship Manager (BRM)',
    approverName: 'Siti Rahmawati',
    status: 'Approved',
    date: '10 Mei 2026, 14:20 WIB',
    notes: 'Rincian anggaran kegiatan pameran sudah sesuai dengan alokasi budget Q2 cabang.',
  },
  {
    id: 3,
    role: 'Branch Manager (BM)',
    approverName: 'Miftakhurokman',
    status: 'Pending',
    date: '-',
    notes: 'Menunggu persetujuan Branch Manager.',
  },
];

export const SubmissionDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [activeTab, setActiveTab] = useState<'info' | 'history'>('info');
  const [modalType, setModalType] = useState<'approve' | 'reject' | null>(null);

  const submission = MOCK_SUBMISSIONS.find((item) => item.id === id);

  if (!submission) {
    return (
      <div className="max-w-6xl mx-auto p-12 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col items-center justify-center text-center">
        <p className="text-gray-500 mb-4 font-medium">Data pengajuan tidak ditemukan.</p>
        <button
          onClick={() => navigate('/')}
          className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" /> Kembali ke Daftar Pengajuan
        </button>
      </div>
    );
  }

  const handleModalSubmit = (type: 'approve' | 'reject', reason: string) => {
    if (type === 'approve') {
      alert(`Pengajuan ${submission.submissionNo} DISETUJUI.\nCatatan: ${reason || '-'}`);
    } else {
      alert(`Pengajuan ${submission.submissionNo} DITOLAK.\nAlasan: ${reason}`);
    }
    setModalType(null);
    navigate('/');
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
      
      {/* Header Page */}
      <div className="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <div className="space-y-1">
          <button
            onClick={() => navigate('/')}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-blue-600 hover:text-blue-700 transition-colors cursor-pointer mb-1"
          >
            <ArrowLeft className="w-3.5 h-3.5" /> Kembali ke Daftar Pengajuan
          </button>
          
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-800">Detail Pengajuan Dana</h1>
            <span className="text-gray-300 font-light">|</span>
            <span className="text-lg font-bold text-gray-600">{submission.submissionNo}</span>
          </div>
          
          <p className="text-sm text-gray-500">
            Informasi lengkap, verifikasi dokumen proposal, dan riwayat persetujuan
          </p>
        </div>

        <StatusBadge status={submission.status} />
      </div>

      {/* Tab Container */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="border-b border-gray-100 px-6 pt-4 flex gap-8 bg-gray-50/50">
          <button
            onClick={() => setActiveTab('info')}
            className={`pb-4 text-sm font-semibold flex items-center gap-2 transition-all border-b-2 cursor-pointer ${
              activeTab === 'info'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            <Info className="w-4 h-4" />
            <span>Detail & Proposal</span>
          </button>

          <button
            onClick={() => setActiveTab('history')}
            className={`pb-4 text-sm font-semibold flex items-center gap-2 transition-all border-b-2 cursor-pointer ${
              activeTab === 'history'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            <History className="w-4 h-4" />
            <span>Riwayat Persetujuan</span>
            <span className="px-2 py-0.5 rounded-full text-xs font-bold bg-gray-200 text-gray-700">
              {MOCK_APPROVAL_HISTORY.length}
            </span>
          </button>
        </div>

        <div className="p-6">
          {activeTab === 'info' ? (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm bg-gray-50 p-5 rounded-xl border border-gray-100">
                <div>
                  <p className="text-gray-400 text-xs font-medium">Nama Pemohon</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.applicantName}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Cabang</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.branchName}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Nominal Pengajuan</p>
                  <p className="font-bold text-blue-600 text-xl mt-0.5">{formatRupiah(submission.nominal)}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Tanggal Pengajuan</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.createdAt}</p>
                </div>
                <div className="md:col-span-2 border-t border-gray-200/60 pt-3">
                  <p className="text-gray-400 text-xs font-medium mb-1">Keperluan / Deskripsi Kegiatan</p>
                  <p className="text-gray-700 text-sm leading-relaxed">{submission.description}</p>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <FileText className="w-5 h-5 text-red-600" />
                    <h3 className="font-semibold text-gray-800 text-sm">Dokumen Proposal (PDF)</h3>
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

                <div className="w-full h-[500px] bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
                  <iframe
                    src={submission.proposalUrl}
                    title={`Proposal-${submission.submissionNo}`}
                    className="w-full h-full border-0"
                  />
                </div>
              </div>
            </div>
          ) : (
            <ApprovalTimeline history={MOCK_APPROVAL_HISTORY} />
          )}
        </div>
      </div>

      {/* Action Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex items-center justify-between">
        <div>
          <h3 className="font-semibold text-gray-800 text-sm">Aksi Persetujuan</h3>
          <p className="text-xs text-gray-500">
            Tentukan tindakan persetujuan untuk pengajuan dana {submission.submissionNo}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setModalType('reject')}
            className="px-5 py-2.5 rounded-lg border border-red-200 bg-red-50 hover:bg-red-100 text-red-700 font-medium text-sm flex items-center gap-2 transition-all cursor-pointer"
          >
            <XCircle className="w-4 h-4" /> Tolak Pengajuan
          </button>
          <button
            onClick={() => setModalType('approve')}
            className="px-5 py-2.5 rounded-lg bg-green-600 hover:bg-green-700 text-white font-medium text-sm flex items-center gap-2 transition-all cursor-pointer shadow-sm"
          >
            <CheckCircle2 className="w-4 h-4" /> Setujui Pengajuan
          </button>
        </div>
      </div>

      {/* Pop-up Modal */}
      <ApprovalActionModal
        isOpen={modalType !== null}
        type={modalType}
        submissionNo={submission.submissionNo}
        onClose={() => setModalType(null)}
        onSubmit={handleModalSubmit}
      />

    </div>
  );
};