import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { StatusBadge } from '../components/ui/StatusBadge';
import { ApprovalTimeline } from '../components/submission/ApprovalTimeline';
import { ApprovalActionModal } from '../components/submission/ApprovalActionModal';
import { CashoutActionModal } from '../components/submission/CashoutActionModal';
import { formatRupiah } from '../utils/formatter';
import { api, getStoredUser } from '../utils/auth';
import type { ApprovalHistory, ApprovalStatus } from '../types/submission';
import {
  ArrowLeft,
  CheckCircle2,
  FileText,
  ExternalLink,
  Download,
  History,
  Info,
  XCircle,
  Landmark,
  CalendarDays,
} from 'lucide-react';

type ApiSubmissionDetail = {
  id: number;
  nomorPengajuan: string;
  judulKegiatan?: string | null;
  nominalPengajuan?: number | string | null;
  status?: string | null;
  tanggalKegiatan?: string | null;
  deskripsi?: string | null;
  branchName?: string | null;
  areaName?: string | null;
  brandName?: string | null;
  requesterName?: string | null;
  proposalUrl?: string | null;
  pencairanUrl?: string | null;
  namaBank?: string | null;
  nomorRekening?: string | null;
  namaPemilikRekening?: string | null;
  berhakApprove?: boolean | null;
  berhakMencairkan?: boolean | null;
  approvalHistories?: Array<{
    id?: number | null;
    approverName?: string | null;
    approverRole?: string | null;
    action?: string | null;
    notes?: string | null;
    actionAt?: string | null;
  }> | null;
};

const formatEventDate = (value?: string | null) => {
  if (!value) return '-';

  const parsed = new Date(`${value}T00:00:00`);
  if (Number.isNaN(parsed.getTime())) return value;

  return new Intl.DateTimeFormat('id-ID', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(parsed);
};

const formatApprovalDate = (value?: string | null) => {
  if (!value) return '-';

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;

  return new Intl.DateTimeFormat('id-ID', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(parsed);
};

const resolveFileUrl = (url?: string | null) => {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;

  const baseUrl = (api.defaults.baseURL ?? 'http://localhost:8081').replace(/\/$/, '');
  return `${baseUrl}${url.startsWith('/') ? url : `/${url}`}`;
};

export const SubmissionDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<'info' | 'history'>('info');
  const [modalType, setModalType] = useState<'approve' | 'reject' | null>(null);
  const [cashoutModalOpen, setCashoutModalOpen] = useState(false);
  const [submission, setSubmission] = useState<ApiSubmissionDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submittingApproval, setSubmittingApproval] = useState(false);
  const [submittingCashout, setSubmittingCashout] = useState(false);

  const currentUser = getStoredUser();
  const isFinanceRole = (currentUser?.roleCode ?? '').toUpperCase().includes('FINANCE') ||
    (currentUser?.roleCode ?? '').toUpperCase().includes('PIC FINANCE');

  const fetchSubmission = async () => {
    if (!id) return;

    try {
      setLoading(true);
      setError('');

      const response = await api.get(`/api/v1/pengajuan-dana/${id}`);
      setSubmission(response.data?.data ?? null);
    } catch (err) {
      console.error('Failed to fetch pengajuan dana detail:', err);
      setError('Gagal memuat detail pengajuan dana.');
      setSubmission(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchSubmission();
  }, [id]);

  const normalizeAction = (action?: string | null): ApprovalHistory['status'] => {
    const value = (action ?? '').trim().toLowerCase();

    if (value === 'diajukan' || value === 'submitted') return 'Diajukan';
    if (value === 'disetujui' || value === 'approved') return 'Disetujui';
    if (value === 'ditolak' || value === 'rejected') return 'Ditolak';
    if (value === 'kadaluarsa' || value === 'expired') return 'Kadaluarsa';
    if (value === 'dicairkan' || value === 'cashout' || value === 'pencairan') return 'Dicairkan';
    if (value === 'siap dicairkan' || value === 'ready for cashout') return 'Siap Dicairkan';

    return 'Menunggu Proses';
  };

  const approvalHistory: ApprovalHistory[] = (submission?.approvalHistories ?? []).map((item, index) => ({
    id: item.id ?? index + 1,
    role: item.approverRole ?? 'Approval',
    approverName: item.approverName ?? '-',
    status: normalizeAction(item.action),
    date: formatApprovalDate(item.actionAt),
    notes: item.notes ?? undefined,
  }));

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto p-12 bg-white rounded-xl shadow-sm border border-gray-100 text-center text-sm text-gray-500">
        Memuat detail pengajuan dana...
      </div>
    );
  }

  if (error || !submission) {
    return (
      <div className="max-w-6xl mx-auto p-12 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col items-center justify-center text-center">
        <p className="text-gray-500 mb-4 font-medium">{error || 'Data pengajuan tidak ditemukan.'}</p>
        <button
          onClick={() => navigate('/')}
          className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" /> Kembali ke Daftar Pengajuan
        </button>
      </div>
    );
  }

  const safeStatus = (submission.status ?? 'Menunggu Approval BM') as ApprovalStatus;
  const proposalUrl = resolveFileUrl(submission.proposalUrl);
  const pencairanUrl = resolveFileUrl(submission.pencairanUrl);
  const canApprove = Boolean(submission.berhakApprove);
  const canCashout = isFinanceRole && safeStatus === 'Siap Dicairkan';

  const handleModalSubmit = async (type: 'approve' | 'reject', reason: string) => {
    if (!id) return;

    const trimmedReason = reason.trim();
    const payload = type === 'approve'
      ? { catatan: trimmedReason || undefined }
      : { catatan: trimmedReason };

    try {
      setSubmittingApproval(true);
      setError('');

      const endpoint = type === 'approve' ? `/api/v1/pengajuan-dana/${id}/approve` : `/api/v1/pengajuan-dana/${id}/reject`;
      const response = await api.put(endpoint, payload);

      const message = response?.data?.message || (type === 'approve'
        ? 'Pengajuan dana berhasil disetujui.'
        : 'Pengajuan dana berhasil ditolak.');

      alert(message);
      setModalType(null);
      await fetchSubmission();
    } catch (err: any) {
      const backendMessage = err?.response?.data?.message || err?.response?.data?.error || err?.message;
      setError(
        type === 'approve'
          ? `Gagal menyetujui pengajuan: ${backendMessage}`
          : `Gagal menolak pengajuan: ${backendMessage}`
      );
      alert(
        type === 'approve'
          ? `Gagal menyetujui pengajuan: ${backendMessage}`
          : `Gagal menolak pengajuan: ${backendMessage}`
      );
    } finally {
      setSubmittingApproval(false);
    }
  };

  const handleCashoutSubmit = async (file: File) => {
    if (!id) return;

    const formData = new FormData();
    formData.append('buktiTransfer', file);

    try {
      setSubmittingCashout(true);
      setError('');

      const response = await api.put(`/api/v1/pengajuan-dana/${id}/pencairan`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const message = response?.data?.message || 'Pengajuan dana berhasil dicairkan.';
      alert(message);
      setCashoutModalOpen(false);
      await fetchSubmission();
    } catch (err: any) {
      const backendMessage = err?.response?.data?.message || err?.response?.data?.error || err?.message;
      setError(`Gagal melakukan pencairan: ${backendMessage}`);
      alert(`Gagal melakukan pencairan: ${backendMessage}`);
    } finally {
      setSubmittingCashout(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
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
            <span className="text-lg font-bold text-gray-600">{submission.nomorPengajuan}</span>
          </div>

          <p className="text-sm text-gray-500">
            Informasi lengkap, verifikasi dokumen proposal, rekening, dan riwayat persetujuan
          </p>
        </div>

        <StatusBadge status={safeStatus} />
      </div>

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
              {approvalHistory.length}
            </span>
          </button>
        </div>

        <div className="p-6">
          {activeTab === 'info' ? (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm bg-gray-50 p-5 rounded-xl border border-gray-100">
                <div>
                  <p className="text-gray-400 text-xs font-medium">Judul Kegiatan</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.judulKegiatan ?? '-'}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Nama Pemohon</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.requesterName ?? '-'}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Cabang</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.branchName ?? '-'}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Area</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.areaName ?? '-'}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Brand</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5">{submission.brandName ?? '-'}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Nominal Pengajuan</p>
                  <p className="font-bold text-blue-600 text-xl mt-0.5">{formatRupiah(Number(submission.nominalPengajuan ?? 0))}</p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Tanggal Event</p>
                  <p className="font-semibold text-gray-800 text-base mt-0.5 flex items-center gap-2">
                    <CalendarDays className="w-4 h-4 text-blue-600" />
                    {formatEventDate(submission.tanggalKegiatan ?? null)}
                  </p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs font-medium">Status</p>
                  <div className="mt-1.5">
                    <StatusBadge status={safeStatus} />
                  </div>
                </div>
                <div className="md:col-span-2 border-t border-gray-200/60 pt-3">
                  <p className="text-gray-400 text-xs font-medium mb-1">Keperluan / Deskripsi Kegiatan</p>
                  <p className="text-gray-700 text-sm leading-relaxed">{submission.deskripsi ?? '-'}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 rounded-xl border border-gray-100 bg-white p-5 shadow-sm">
                <div className="flex items-start gap-3">
                  <div className="p-2 bg-blue-50 rounded-lg text-blue-600">
                    <Landmark className="w-4 h-4" />
                  </div>
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-gray-400">Bank</p>
                    <p className="text-sm font-semibold text-gray-800 mt-1">{submission.namaBank ?? '-'}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="p-2 bg-blue-50 rounded-lg text-blue-600">
                    <FileText className="w-4 h-4" />
                  </div>
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-gray-400">Nomor Rekening</p>
                    <p className="text-sm font-semibold text-gray-800 mt-1">{submission.nomorRekening ?? '-'}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="p-2 bg-blue-50 rounded-lg text-blue-600">
                    <Info className="w-4 h-4" />
                  </div>
                  <div>
                    <p className="text-[11px] font-medium uppercase tracking-wider text-gray-400">Pemilik Rekening</p>
                    <p className="text-sm font-semibold text-gray-800 mt-1">{submission.namaPemilikRekening ?? '-'}</p>
                  </div>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <FileText className="w-5 h-5 text-red-600" />
                    <h3 className="font-semibold text-gray-800 text-sm">Dokumen Proposal (PDF)</h3>
                  </div>

                  {proposalUrl && (
                    <div className="flex items-center gap-2">
                      <a
                        href={proposalUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 text-xs text-gray-600 hover:text-blue-600 font-medium bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded-lg transition-colors"
                      >
                        <ExternalLink className="w-3.5 h-3.5" /> Buka Tab Baru
                      </a>
                      <a
                        href={proposalUrl}
                        download
                        className="inline-flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700 font-medium bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg transition-colors"
                      >
                        <Download className="w-3.5 h-3.5" /> Unduh
                      </a>
                    </div>
                  )}
                </div>

                {proposalUrl ? (
                  <div className="w-full h-[500px] bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
                    <iframe
                      src={proposalUrl}
                      title={`Proposal-${submission.nomorPengajuan}`}
                      className="w-full h-full border-0"
                    />
                  </div>
                ) : (
                  <div className="w-full rounded-lg border border-dashed border-gray-200 bg-gray-50 p-8 text-center text-sm text-gray-500">
                    Dokumen proposal belum tersedia.
                  </div>
                )}
              </div>

              {safeStatus === 'Dicairkan' && pencairanUrl && (
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <FileText className="w-5 h-5 text-green-600" />
                      <h3 className="font-semibold text-gray-800 text-sm">Bukti Transfer (PDF)</h3>
                    </div>

                    <div className="flex items-center gap-2">
                      <a
                        href={pencairanUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 text-xs text-gray-600 hover:text-blue-600 font-medium bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded-lg transition-colors"
                      >
                        <ExternalLink className="w-3.5 h-3.5" /> Buka Tab Baru
                      </a>
                      <a
                        href={pencairanUrl}
                        download
                        className="inline-flex items-center gap-1 text-xs text-green-600 hover:text-green-700 font-medium bg-green-50 hover:bg-green-100 px-3 py-1.5 rounded-lg transition-colors"
                      >
                        <Download className="w-3.5 h-3.5" /> Unduh
                      </a>
                    </div>
                  </div>

                  <div className="w-full h-[500px] bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
                    <iframe
                      src={pencairanUrl}
                      title={`Bukti-Transfer-${submission.nomorPengajuan}`}
                      className="w-full h-full border-0"
                    />
                  </div>
                </div>
              )}
            </div>
          ) : (
            <ApprovalTimeline history={approvalHistory} />
          )}
        </div>
      </div>

      {(canApprove || canCashout) && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex items-center justify-between gap-3 flex-wrap">
          <div>
            <h3 className="font-semibold text-gray-800 text-sm">
              {canApprove && canCashout ? 'Aksi Persetujuan & Pencairan' : 'Aksi'}
            </h3>
            <p className="text-xs text-gray-500">
              {canApprove
                ? `Tentukan tindakan persetujuan untuk pengajuan dana ${submission.nomorPengajuan}`
                : 'Upload bukti transfer untuk proses pencairan dana.'}
            </p>
          </div>

          <div className="flex items-center gap-3 flex-wrap">
            {canApprove && (
              <>
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
              </>
            )}

            {canCashout && (
              <button
                onClick={() => setCashoutModalOpen(true)}
                className="px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-medium text-sm flex items-center gap-2 transition-all cursor-pointer shadow-sm"
              >
                <FileText className="w-4 h-4" /> Pencairan
              </button>
            )}
          </div>
        </div>
      )}

      <ApprovalActionModal
        isOpen={modalType !== null}
        type={modalType}
        submissionNo={submission.nomorPengajuan}
        isSubmitting={submittingApproval}
        onClose={() => setModalType(null)}
        onSubmit={handleModalSubmit}
      />

      <CashoutActionModal
        isOpen={cashoutModalOpen}
        submissionNo={submission.nomorPengajuan}
        isSubmitting={submittingCashout}
        onClose={() => setCashoutModalOpen(false)}
        onSubmit={handleCashoutSubmit}
      />
    </div>
  );
};