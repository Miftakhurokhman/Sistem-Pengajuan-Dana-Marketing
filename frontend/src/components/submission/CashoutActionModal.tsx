import { useRef, useState } from 'react';
import { UploadCloud, X, FileText } from 'lucide-react';

interface CashoutActionModalProps {
  isOpen: boolean;
  submissionNo: string;
  onClose: () => void;
  onSubmit: (file: File) => Promise<void> | void;
  isSubmitting?: boolean;
}

const MAX_FILE_SIZE = 1 * 1024 * 1024;

export const CashoutActionModal = ({
  isOpen,
  submissionNo,
  onClose,
  onSubmit,
  isSubmitting = false,
}: CashoutActionModalProps) => {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const validateFile = (file: File | null) => {
    if (!file) {
      setError('File bukti transfer wajib diupload.');
      return false;
    }

    const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (!isPdf) {
      setError('Format file harus PDF.');
      return false;
    }

    if (file.size > MAX_FILE_SIZE) {
      setError('Ukuran file maksimal 1 MB.');
      return false;
    }

    setError('');
    return true;
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;
    setSelectedFile(file);
    validateFile(file);
  };

  const handleSubmit = async () => {
    if (!validateFile(selectedFile)) {
      return;
    }

    if (selectedFile) {
      await onSubmit(selectedFile);
      setSelectedFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-xl shadow-xl max-w-md w-full overflow-hidden border border-gray-100">
        <div className="flex items-center justify-between p-5 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-100 text-blue-700 rounded-lg">
              <UploadCloud className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-gray-800 text-base">Upload Bukti Transfer</h3>
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

        <div className="p-5 space-y-4">
          <p className="text-xs text-gray-600 leading-relaxed">
            Upload bukti transfer untuk pencairan dana. File yang diterima hanya format PDF dengan maksimal ukuran 1 MB.
          </p>

          <label className="block">
            <span className="block text-xs font-semibold text-gray-700 mb-1.5">File Bukti Transfer</span>
            <div className="border border-dashed border-gray-300 rounded-lg p-4 bg-gray-50 hover:bg-gray-100 transition-colors">
              <input
                ref={fileInputRef}
                type="file"
                accept="application/pdf"
                onChange={handleFileChange}
                className="hidden"
              />

              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-lg border border-gray-200 bg-white text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors cursor-pointer"
              >
                <UploadCloud className="w-4 h-4" />
                {selectedFile ? 'Ganti File' : 'Pilih File PDF'}
              </button>

              {selectedFile && (
                <div className="mt-3 flex items-center gap-2 rounded-lg bg-white border border-gray-200 px-3 py-2 text-xs text-gray-700">
                  <FileText className="w-4 h-4 text-blue-600" />
                  <span className="truncate">{selectedFile.name}</span>
                </div>
              )}
            </div>
          </label>

          {error && <p className="text-xs text-red-600 font-medium">{error}</p>}
        </div>

        <div className="p-4 bg-gray-50 border-t border-gray-100 flex justify-end gap-2">
          <button
            onClick={onClose}
            disabled={isSubmitting}
            className="px-4 py-2 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-200/60 transition-colors cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
          >
            Batal
          </button>

          <button
            onClick={handleSubmit}
            disabled={isSubmitting || !selectedFile}
            className="px-4 py-2 rounded-lg text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 transition-all cursor-pointer shadow-sm disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? 'Menyimpan...' : 'Simpan Bukti Transfer'}
          </button>
        </div>
      </div>
    </div>
  );
};
