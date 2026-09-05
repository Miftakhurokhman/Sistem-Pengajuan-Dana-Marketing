import { Search, RotateCcw, Filter } from 'lucide-react';

export type SearchByCategory = 'submissionNo' | 'applicantName' | 'branchName';

interface SubmissionSearchFilterProps {
  searchBy: SearchByCategory;
  setSearchBy: (category: SearchByCategory) => void;
  searchValue: string;
  setSearchValue: (value: string) => void;
  selectedStatus: string;
  setSelectedStatus: (status: string) => void;
  onReset: () => void;
}

export const SubmissionSearchFilter = ({
  searchBy,
  setSearchBy,
  searchValue,
  setSearchValue,
  selectedStatus,
  setSelectedStatus,
  onReset,
}: SubmissionSearchFilterProps) => {
  return (
    <div className="bg-white p-4 rounded-xl border border-gray-100 shadow-sm space-y-3">
      <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
        
        {/* Dynamic Search Group */}
        <div className="flex flex-1 w-full gap-2">
          {/* Dropdown Kategori Search */}
          <select
            value={searchBy}
            onChange={(e) => setSearchBy(e.target.value as SearchByCategory)}
            className="bg-gray-50 border border-gray-200 text-gray-700 text-xs font-semibold rounded-lg px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
          >
            <option value="submissionNo">Nomor Pengajuan</option>
            <option value="applicantName">Nama Pemohon</option>
            <option value="branchName">Cabang</option>
          </select>

          {/* Input Keyword Search */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              placeholder={`Cari berdasarkan ${
                searchBy === 'submissionNo'
                  ? 'Nomor Pengajuan (misal: REQ-001)'
                  : searchBy === 'applicantName'
                  ? 'Nama Pemohon'
                  : 'Nama Cabang'
              }...`}
              className="w-full pl-9 pr-4 py-2 text-xs border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {/* Filter Status & Reset Action */}
        <div className="flex w-full md:w-auto items-center gap-2 justify-between md:justify-end">
          <div className="flex items-center gap-1.5 bg-gray-50 border border-gray-200 px-3 py-1 rounded-lg">
            <Filter className="w-3.5 h-3.5 text-gray-400" />
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="bg-transparent text-gray-700 text-xs font-semibold focus:outline-none cursor-pointer"
            >
              <option value="ALL">Semua Status</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>

          <button
            onClick={onReset}
            className="inline-flex items-center gap-1.5 text-xs text-gray-600 hover:text-gray-900 bg-gray-100 hover:bg-gray-200 px-3 py-2 rounded-lg font-medium transition-colors cursor-pointer"
            title="Reset Filter"
          >
            <RotateCcw className="w-3.5 h-3.5" /> Reset
          </button>
        </div>

      </div>
    </div>
  );
};