import { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { StatusBadge } from '../components/ui/StatusBadge';
import {
  SubmissionSearchFilter,
  type SearchByCategory,
} from '../components/submission/SubmissionSearchFilter';
import { formatRupiah } from '../utils/formatter';
import { api, getStoredUser } from '../utils/auth';
import { FileText, Eye, Inbox } from 'lucide-react';

type ApiSubmission = {
  id: number;
  nomorPengajuan: string;
  requesterName: string;
  brandName?: string | null;
  nominalPengajuan: number | string;
  status: string;
  tanggalKegiatan?: string | null;
  branchName?: string | null;
};

type TabType = 'need-approval' | 'all-data';

type PaginationMeta = {
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

const PAGE_SIZE = 10;

const formatEventDate = (value?: string | null) => {
  if (!value) return '-';

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;

  return new Intl.DateTimeFormat('id-ID', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(parsed);
};

const hasApprovalTabs = (roleCode?: string) => {
  const normalized = (roleCode ?? '').toUpperCase();
  return !normalized.includes('PIC SALES') && !normalized.includes('SALES');
};

const getDefaultTab = (roleCode?: string): TabType => {
  return hasApprovalTabs(roleCode) ? 'need-approval' : 'all-data';
};

export const SubmissionListPage = () => {
  const navigate = useNavigate();
  const currentUser = getStoredUser();
  const roleCode = currentUser?.roleCode ?? '';
  const showApprovalTabs = hasApprovalTabs(roleCode);

  const [searchBy, setSearchBy] = useState<SearchByCategory>('submissionNo');
  const [searchValue, setSearchValue] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [activeTab, setActiveTab] = useState<TabType>(getDefaultTab(roleCode));
  const [page, setPage] = useState(0);
  const [submissions, setSubmissions] = useState<any[]>([]);
  const [pagination, setPagination] = useState<PaginationMeta>({
    pageNo: 0,
    pageSize: PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
    last: true,
  });
  const [tabCounts, setTabCounts] = useState({
    needApproval: 0,
    allData: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!showApprovalTabs) {
      setTabCounts({ needApproval: 0, allData: 0 });
      setActiveTab('all-data');
      return;
    }

    const loadTabCounts = async () => {
      try {
        const [needApprovalResponse, allDataResponse] = await Promise.all([
          api.get('/api/v1/pengajuan-dana', {
            params: {
              page: 0,
              size: 1,
              isNeedApproval: true,
            },
          }),
          api.get('/api/v1/pengajuan-dana', {
            params: {
              page: 0,
              size: 1,
              isNeedApproval: false,
            },
          }),
        ]);

        const needApprovalTotal = Number(needApprovalResponse.data?.data?.totalElements ?? 0);
        const allDataTotal = Number(allDataResponse.data?.data?.totalElements ?? 0);

        setTabCounts({
          needApproval: needApprovalTotal,
          allData: allDataTotal,
        });

        setActiveTab((prev) => {
          if (prev === 'need-approval' || prev === 'all-data') {
            return prev;
          }

          return needApprovalTotal > 0 ? 'need-approval' : 'all-data';
        });
      } catch (countError) {
        console.warn('Failed to load submission tabs count:', countError);
      }
    };

    void loadTabCounts();
  }, [showApprovalTabs]);

  useEffect(() => {
    const fetchSubmissions = async () => {
      try {
        setLoading(true);
        setError('');

        const response = await api.get('/api/v1/pengajuan-dana', {
          params: {
            page,
            size: PAGE_SIZE,
            isNeedApproval: showApprovalTabs && activeTab === 'need-approval',
          },
        });

        const payload = response.data?.data ?? {};
        const content = payload.content ?? [];

        const mapped = content.map((item: ApiSubmission) => ({
          id: item.id,
          submissionNo: item.nomorPengajuan,
          applicantName: item.requesterName ?? '-',
          brandName: item.brandName ?? '-',
          branchName: item.branchName ?? '-',
          nominal: Number(item.nominalPengajuan ?? 0),
          status: item.status || 'Menunggu Approval BM',
          createdAt: formatEventDate(item.tanggalKegiatan),
          tanggalKegiatan: item.tanggalKegiatan,
        }));

        setSubmissions(mapped);
        setPagination({
          pageNo: Number(payload.pageNo ?? 0),
          pageSize: Number(payload.pageSize ?? PAGE_SIZE),
          totalElements: Number(payload.totalElements ?? 0),
          totalPages: Number(payload.totalPages ?? 0),
          last: Boolean(payload.last ?? true),
        });
      } catch (err) {
        console.error('Failed to fetch pengajuan dana list:', err);
        setError('Gagal memuat data pengajuan dana. Silakan coba lagi.');
        setSubmissions([]);
        setPagination({
          pageNo: 0,
          pageSize: PAGE_SIZE,
          totalElements: 0,
          totalPages: 0,
          last: true,
        });
      } finally {
        setLoading(false);
      }
    };

    void fetchSubmissions();
  }, [activeTab, page, showApprovalTabs]);

  const filteredSubmissions = useMemo(() => {
    return submissions.filter((item) => {
      // const statusFilterMatch =
      //   selectedStatus === 'ALL'
      //     ? true
      //     : selectedStatus === 'PENDING'
      //     ? item.status.startsWith('Menunggu')
      //     : selectedStatus === 'APPROVED'
      //     ? item.status === 'Siap Dicairkan' || item.status === 'Dicairkan'
      //     : item.status === 'Ditolak';

      // if (!statusFilterMatch) {
      //   return false;
      // }

      if (!searchValue.trim()) return true;

      const query = searchValue.toLowerCase();

      if (searchBy === 'submissionNo') {
        return String(item.submissionNo).toLowerCase().includes(query);
      }

      if (searchBy === 'applicantName') {
        return String(item.applicantName).toLowerCase().includes(query);
      }

      if (searchBy === 'branchName') {
        return String(item.branchName).toLowerCase().includes(query);
      }

      return true;
    });
  }, [searchBy, searchValue, selectedStatus, submissions]);

  const handleResetFilter = () => {
    setSearchBy('submissionNo');
    setSearchValue('');
    setSelectedStatus('ALL');
  };

  const handleTabChange = (nextTab: TabType) => {
    setActiveTab(nextTab);
    setPage(0);
  };

  const pageNumbers = useMemo(() => {
    if (pagination.totalPages <= 1) {
      return [] as number[];
    }

    const start = Math.max(1, pagination.pageNo + 1 - 2);
    const end = Math.min(pagination.totalPages, start + 4);
    const adjustedStart = Math.max(1, end - 4);

    return Array.from({ length: end - adjustedStart + 1 }, (_, index) => adjustedStart + index);
  }, [pagination.pageNo, pagination.totalPages]);

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
      <div className="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Daftar Pengajuan Dana</h1>
          <p className="text-sm text-gray-500 mt-1">
            Kelola dan pantau seluruh riwayat pengajuan dana
          </p>
        </div>
      </div>

      {showApprovalTabs && (
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={() => handleTabChange('need-approval')}
            className={`inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition-colors ${
              activeTab === 'need-approval'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'
            }`}
          >
            Need Approval
            <span
              className={`inline-flex min-w-[24px] justify-center rounded-full px-1.5 py-0.5 text-[11px] font-bold ${
                activeTab === 'need-approval' ? 'bg-white/20 text-white' : 'bg-blue-100 text-blue-700'
              }`}
            >
              {tabCounts.needApproval}
            </span>
          </button>

          <button
            type="button"
            onClick={() => handleTabChange('all-data')}
            className={`inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition-colors ${
              activeTab === 'all-data'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'
            }`}
          >
            Semua Data
            <span
              className={`inline-flex min-w-[24px] justify-center rounded-full px-1.5 py-0.5 text-[11px] font-bold ${
                activeTab === 'all-data' ? 'bg-white/20 text-white' : 'bg-blue-100 text-blue-700'
              }`}
            >
              {tabCounts.allData}
            </span>
          </button>
        </div>
      )}

      <SubmissionSearchFilter
        searchBy={searchBy}
        setSearchBy={setSearchBy}
        searchValue={searchValue}
        setSearchValue={setSearchValue}
        selectedStatus={selectedStatus}
        setSelectedStatus={setSelectedStatus}
        onReset={handleResetFilter}
      />

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-sm">
          {error}
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100 text-[11px] font-bold text-gray-500 uppercase tracking-wider">
                <th className="py-3.5 px-4">No. Pengajuan</th>
                <th className="py-3.5 px-4">Pemohon</th>
                <th className="py-3.5 px-4">Brand</th>
                <th className="py-3.5 px-4">Nominal</th>
                <th className="py-3.5 px-4">Tanggal Event</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4 text-center">Aksi</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-sm text-gray-500">
                    Memuat data pengajuan dana...
                  </td>
                </tr>
              ) : filteredSubmissions.length > 0 ? (
                filteredSubmissions.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50/80 transition-colors">
                    <td className="py-3.5 px-4 font-bold text-gray-800 flex items-center gap-2">
                      <FileText className="w-4 h-4 text-blue-600" />
                      {item.submissionNo}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-gray-700">{item.applicantName}</td>
                    <td className="py-3.5 px-4 text-gray-600">{item.brandName}</td>
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
                        Tidak ada data yang cocok dengan kriteria pencarian Anda.
                      </p>
                      <button
                        onClick={handleResetFilter}
                        className="mt-2 text-xs text-blue-600 hover:text-blue-700 font-semibold cursor-pointer underline"
                      >
                        Bersihkan Pencarian
                      </button>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {pagination.totalPages > 1 && (
          <div className="flex flex-col gap-3 border-t border-gray-100 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-xs text-gray-500">
              Menampilkan {pagination.totalElements > 0 ? page * PAGE_SIZE + 1 : 0} -{' '}
              {Math.min((page + 1) * PAGE_SIZE, pagination.totalElements)} dari {pagination.totalElements} data
            </p>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setPage((current) => Math.max(0, current - 1))}
                disabled={page === 0}
                className="rounded-lg border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-600 disabled:cursor-not-allowed disabled:opacity-40 hover:bg-gray-50"
              >
                Sebelumnya
              </button>

              {pageNumbers.map((pageNumber) => (
                <button
                  key={pageNumber}
                  type="button"
                  onClick={() => setPage(pageNumber - 1)}
                  className={`h-8 min-w-[32px] rounded-lg px-2 text-xs font-semibold ${
                    pageNumber - 1 === page
                      ? 'bg-blue-600 text-white'
                      : 'border border-gray-200 bg-white text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  {pageNumber}
                </button>
              ))}

              <button
                type="button"
                onClick={() => setPage((current) => Math.min(pagination.totalPages - 1, current + 1))}
                disabled={pagination.last}
                className="rounded-lg border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-600 disabled:cursor-not-allowed disabled:opacity-40 hover:bg-gray-50"
              >
                Berikutnya
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};