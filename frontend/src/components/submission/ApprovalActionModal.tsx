import { useState } from 'react';
import { CheckCircle2, AlertTriangle, X } from 'lucide-react';

interface ApprovalActionModalProps {
  isOpen: boolean;
  type: 'approve' | 'reject' | null;
  submissionNo: string;
  onClose: () => void;
  onSubmit: (type: 'approve' | 'reject', reason: string) => void;
}

export const ApprovalActionModal = ({
  isOpen,
  type,
  submissionNo,
  onClose,
  onSubmit,
}: ApprovalActionModalProps) => {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  if (!isOpen || !type) return null;

  const handleSubmit = () => {
    if (type === 'reject' && !reason.trim()) {
      setError('Alasan penolakan wajib diisi!');
      return;
    }
    onSubmit(type, reason);
    setReason('');
    setError('');
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-xl shadow-xl max-w-md w-full overflow-hidden border border-gray-100">
        
        {/* Modal Header */}
        <div className="flex items-center justify-between p-5 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            {type === 'approve' ? (
              <div className="p-2 bg-green-100 text-green-700 rounded-lg">
                <CheckCircle2 className="w-5 h-5" />
              </div>
            ) : (
              <div className="p-2 bg-red-100 text-red-700 rounded-lg">
                <AlertTriangle className="w-5 h-5" />
              </div>
            )}
            <div>
              <h3 className="font-bold text-gray-800 text-base">
                {type === 'approve' ? 'Setujui Pengajuan Dana' : 'Tolak Pengajuan Dana'}
              </h3>
              <p className="text-xs text-gray-500">{submissionNo}</p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 p-1 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-5 space-y-4">
          <p className="text-xs text-gray-600 leading-relaxed">
            {type === 'approve'
              ? 'Apakah Anda yakin ingin menyetujui pengajuan dana ini? Anda dapat menambahkan catatan opsional di bawah ini.'
              : 'Silakan masukkan alasan penolakan pengajuan dana ini agar pemohon dapat melakukan perbaikan.'}
          </p>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1.5">
              {type === 'approve' ? 'Catatan (Opsional)' : 'Alasan Penolakan (Wajib)'}
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => {
                setReason(e.target.value);
                if (e.target.value.trim()) setError('');
              }}
              placeholder={
                type === 'approve'
                  ? 'Masukkan catatan persetujuan...'
                  : 'Contoh: Anggaran melebihi batas kuota Q2...'
              }
              className={`w-full p-3 border rounded-lg text-sm focus:outline-none focus:ring-2 ${
                error
                  ? 'border-red-500 focus:ring-red-200'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
            />
            {error && <p className="text-xs text-red-600 mt-1 font-medium">{error}</p>}
          </div>
        </div>

        {/* Modal Footer */}
        <div className="p-4 bg-gray-50 border-t border-gray-100 flex justify-end gap-2">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-200/60 transition-colors cursor-pointer"
          >
            Batal
          </button>
          
          <button
            onClick={handleSubmit}
            className={`px-4 py-2 rounded-lg text-sm font-medium text-white transition-all cursor-pointer shadow-sm ${
              type === 'approve'
                ? 'bg-green-600 hover:bg-green-700'
                : 'bg-red-600 hover:bg-red-700'
            }`}
          >
            {type === 'approve' ? 'Konfirmasi Setuju' : 'Konfirmasi Tolak'}
          </button>
        </div>

      </div>
    </div>
  );
};