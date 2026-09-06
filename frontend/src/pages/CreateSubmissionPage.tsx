import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, FileText, Landmark, FolderPlus, CheckCircle2 } from 'lucide-react';
import { api } from '../utils/auth';

type BrandOption = {
  id: number;
  brandCode?: string | null;
  brandName?: string | null;
};

type SubmissionFormState = {
  judulKegiatan: string;
  deskripsiKegiatan: string;
  nominalPengajuan: string;
  tanggalKegiatan: string;
  namaBank: string;
  nomorRekening: string;
  brandId: string;
  namaPemilikRekening: string;
  proposalFile: File | null;
};

const MAX_FILE_SIZE_BYTES = 1 * 1024 * 1024;

const initialForm: SubmissionFormState = {
  judulKegiatan: '',
  deskripsiKegiatan: '',
  nominalPengajuan: '',
  tanggalKegiatan: '',
  namaBank: '',
  nomorRekening: '',
  brandId: '',
  namaPemilikRekening: '',
  proposalFile: null,
};

const getTodayString = () => {
  const now = new Date();
  const offset = now.getTimezoneOffset();
  const localDate = new Date(now.getTime() - offset * 60 * 1000);
  return localDate.toISOString().split('T')[0];
};

const isPdfFile = (file: File | null) => {
  if (!file) return false;

  const isPdfType = file.type === 'application/pdf' || file.type === 'application/x-pdf';
  const isPdfName = file.name.toLowerCase().endsWith('.pdf');

  return isPdfType || isPdfName;
};

export const CreateSubmissionPage = () => {
  const navigate = useNavigate();
  const [brands, setBrands] = useState<BrandOption[]>([]);
  const [form, setForm] = useState<SubmissionFormState>(initialForm);
  const [errors, setErrors] = useState<Partial<Record<keyof SubmissionFormState, string>>>({});
  const [loadingBrands, setLoadingBrands] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  useEffect(() => {
    const fetchBrands = async () => {
      try {
        setLoadingBrands(true);
        const response = await api.get('/api/v1/brands');
        setBrands(response.data?.data ?? []);
      } catch (err) {
        console.error('Failed to fetch brands:', err);
      } finally {
        setLoadingBrands(false);
      }
    };

    void fetchBrands();
  }, []);

  const proposalFileName = useMemo(() => form.proposalFile?.name ?? 'Belum ada file dipilih', [form.proposalFile]);

  const updateField = <K extends keyof SubmissionFormState>(field: K, value: SubmissionFormState[K]) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
    setSubmitError('');
  };

  const validateForm = () => {
    const nextErrors: Partial<Record<keyof SubmissionFormState, string>> = {};

    if (!form.judulKegiatan.trim()) {
      nextErrors.judulKegiatan = 'Judul kegiatan wajib diisi.';
    } else if (form.judulKegiatan.trim().length > 150) {
      nextErrors.judulKegiatan = 'Judul kegiatan maksimal 150 karakter.';
    }

    if (!form.deskripsiKegiatan.trim()) {
      nextErrors.deskripsiKegiatan = 'Deskripsi kegiatan wajib diisi.';
    } else if (form.deskripsiKegiatan.trim().length > 500) {
      nextErrors.deskripsiKegiatan = 'Deskripsi kegiatan maksimal 500 karakter.';
    }

    const nominalValue = Number(form.nominalPengajuan);
    if (!form.nominalPengajuan.trim()) {
      nextErrors.nominalPengajuan = 'Nominal pengajuan wajib diisi.';
    } else if (!Number.isInteger(nominalValue) || nominalValue <= 0) {
      nextErrors.nominalPengajuan = 'Nominal pengajuan harus angka bulat dan lebih besar dari 0.';
    }

    if (!form.tanggalKegiatan) {
      nextErrors.tanggalKegiatan = 'Tanggal kegiatan wajib diisi.';
    } else {
      const selectedDate = new Date(`${form.tanggalKegiatan}T00:00:00`);
      const today = new Date(`${getTodayString()}T00:00:00`);

      if (selectedDate < today) {
        nextErrors.tanggalKegiatan = 'Tanggal kegiatan minimal hari ini.';
      }
    }

    if (!form.namaBank.trim()) {
      nextErrors.namaBank = 'Nama bank wajib diisi.';
    } else if (form.namaBank.trim().length > 100) {
      nextErrors.namaBank = 'Nama bank maksimal 100 karakter.';
    }

    if (!form.nomorRekening.trim()) {
      nextErrors.nomorRekening = 'Nomor rekening wajib diisi.';
    }

    if (!form.brandId) {
      nextErrors.brandId = 'Brand wajib dipilih.';
    }

    if (!form.namaPemilikRekening.trim()) {
      nextErrors.namaPemilikRekening = 'Nama pemilik rekening wajib diisi.';
    } else if (form.namaPemilikRekening.trim().length > 150) {
      nextErrors.namaPemilikRekening = 'Nama pemilik rekening maksimal 150 karakter.';
    }

    if (!form.proposalFile) {
      nextErrors.proposalFile = 'File proposal wajib diunggah.';
    } else if (!isPdfFile(form.proposalFile)) {
      nextErrors.proposalFile = 'Format file proposal harus PDF.';
    } else if (form.proposalFile.size > MAX_FILE_SIZE_BYTES) {
      nextErrors.proposalFile = 'Ukuran file proposal tidak boleh lebih dari 1 MB.';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setSubmitting(true);
      setSubmitError('');

      const payload = new FormData();
      payload.append('judulKegiatan', form.judulKegiatan.trim());
      payload.append('deskripsiKegiatan', form.deskripsiKegiatan.trim());
      payload.append('nominalPengajuan', String(BigInt(form.nominalPengajuan)));
      payload.append('tanggalKegiatan', form.tanggalKegiatan);
      payload.append('namaBank', form.namaBank.trim());
      payload.append('nomorRekening', form.nomorRekening.trim());
      payload.append('brandId', String(form.brandId));
      payload.append('namaPemilikRekening', form.namaPemilikRekening.trim());
      payload.append('proposalFile', form.proposalFile as Blob);

      const response = await api.post('/api/v1/pengajuan-dana', payload, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const createdId = response.data?.data?.id;
      alert(response.data?.message || 'Pengajuan dana berhasil dibuat.');

      if (createdId) {
        navigate(`/submission/${createdId}`);
        return;
      }

      navigate('/');
    } catch (err: any) {
      console.error('Failed to create pengajuan dana:', err);
      const message = err?.response?.data?.message || err?.message || 'Gagal membuat pengajuan dana.';
      setSubmitError(message);
      alert(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-12">
      <div className="flex justify-between items-center bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <div className="space-y-1">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-blue-600 hover:text-blue-700 transition-colors cursor-pointer mb-1"
          >
            <ArrowLeft className="w-3.5 h-3.5" /> Kembali ke Daftar Pengajuan
          </button>

          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-800">Ajukan Dana Baru</h1>
            <span className="text-gray-300 font-light">|</span>
            <span className="text-sm text-gray-500">Form pengajuan dana</span>
          </div>
        </div>

        <div className="hidden sm:flex items-center gap-2 bg-blue-50 text-blue-700 px-3 py-2 rounded-lg text-xs font-semibold">
          <FolderPlus className="w-4 h-4" /> PIC Sales
        </div>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="border-b border-gray-100 px-6 py-4 bg-gray-50/50">
          <div className="flex items-center gap-2 text-sm font-semibold text-gray-700">
            <FileText className="w-4 h-4 text-blue-600" />
            Informasi Pengajuan
          </div>
        </div>

        <div className="p-6 space-y-6">
          {submitError && (
            <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {submitError}
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Judul Kegiatan</label>
              <input
                type="text"
                value={form.judulKegiatan}
                maxLength={150}
                onChange={(e) => updateField('judulKegiatan', e.target.value)}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.judulKegiatan ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
                placeholder="Contoh: Event Launching Brand X"
              />
              {errors.judulKegiatan && <p className="mt-1 text-xs text-red-600">{errors.judulKegiatan}</p>}
            </div>

            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Deskripsi Kegiatan</label>
              <textarea
                rows={4}
                value={form.deskripsiKegiatan}
                maxLength={500}
                onChange={(e) => updateField('deskripsiKegiatan', e.target.value)}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.deskripsiKegiatan ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
                placeholder="Jelaskan tujuan, kegiatan, dan kebutuhan dana secara singkat..."
              />
              <div className="mt-1 flex items-center justify-between gap-2 text-[11px] text-gray-400">
                <span>{errors.deskripsiKegiatan ? <span className="text-red-600">{errors.deskripsiKegiatan}</span> : 'Maksimal 500 karakter'}</span>
                <span>{form.deskripsiKegiatan.length}/500</span>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Nominal Pengajuan</label>
              <input
                type="number"
                min="1"
                step="1"
                value={form.nominalPengajuan}
                onChange={(e) => updateField('nominalPengajuan', e.target.value)}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.nominalPengajuan ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
                placeholder="15000000"
              />
              {errors.nominalPengajuan && <p className="mt-1 text-xs text-red-600">{errors.nominalPengajuan}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Tanggal Kegiatan</label>
              <input
                type="date"
                min={getTodayString()}
                value={form.tanggalKegiatan}
                onChange={(e) => updateField('tanggalKegiatan', e.target.value)}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.tanggalKegiatan ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
              />
              {errors.tanggalKegiatan && <p className="mt-1 text-xs text-red-600">{errors.tanggalKegiatan}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Brand</label>
              <select
                value={form.brandId}
                onChange={(e) => updateField('brandId', e.target.value)}
                disabled={loadingBrands}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.brandId ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
              >
                <option value="">{loadingBrands ? 'Memuat brand...' : 'Pilih brand'}</option>
                {brands.map((brand) => (
                  <option key={brand.id} value={brand.id}>
                    {brand.brandName || brand.brandCode || `Brand ${brand.id}`}
                  </option>
                ))}
              </select>
              {errors.brandId && <p className="mt-1 text-xs text-red-600">{errors.brandId}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Nama Bank</label>
              <input
                type="text"
                value={form.namaBank}
                maxLength={100}
                onChange={(e) => updateField('namaBank', e.target.value)}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.namaBank ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
                placeholder="Contoh: BCA"
              />
              {errors.namaBank && <p className="mt-1 text-xs text-red-600">{errors.namaBank}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Nomor Rekening</label>
              <input
                type="text"
                value={form.nomorRekening}
                onChange={(e) => updateField('nomorRekening', e.target.value)}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.nomorRekening ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
                placeholder="1234567890"
              />
              {errors.nomorRekening && <p className="mt-1 text-xs text-red-600">{errors.nomorRekening}</p>}
            </div>

            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">Nama Pemilik Rekening</label>
              <input
                type="text"
                value={form.namaPemilikRekening}
                maxLength={150}
                onChange={(e) => updateField('namaPemilikRekening', e.target.value)}
                className={`w-full rounded-lg border px-3 py-2.5 text-sm text-gray-800 focus:outline-none focus:ring-2 ${errors.namaPemilikRekening ? 'border-red-400 focus:ring-red-200' : 'border-gray-300 focus:ring-blue-500'}`}
                placeholder="Nama sesuai buku rekening"
              />
              {errors.namaPemilikRekening && <p className="mt-1 text-xs text-red-600">{errors.namaPemilikRekening}</p>}
            </div>
          </div>

          <div className="rounded-xl border border-dashed border-gray-200 bg-gray-50 p-5">
            <div className="flex items-center gap-2 mb-3">
              <Landmark className="w-4 h-4 text-blue-600" />
              <h3 className="font-semibold text-gray-800 text-sm">Upload Proposal PDF</h3>
            </div>

            <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
              <label className="inline-flex items-center justify-center gap-2 rounded-lg border border-blue-200 bg-blue-50 px-4 py-2.5 text-sm font-medium text-blue-700 hover:bg-blue-100 transition-colors cursor-pointer">
                <FileText className="w-4 h-4" /> Pilih File PDF
                <input
                  type="file"
                  accept="application/pdf"
                  className="hidden"
                  onChange={(e) => updateField('proposalFile', e.target.files?.[0] ?? null)}
                />
              </label>

              <span className="text-xs text-gray-500 break-all">{proposalFileName}</span>
            </div>

            <div className="mt-2 text-[11px] text-gray-500">
              Maksimal ukuran file 1 MB. Format yang diterima: PDF.
            </div>

            {errors.proposalFile && <p className="mt-2 text-xs text-red-600">{errors.proposalFile}</p>}
          </div>
        </div>

        <div className="border-t border-gray-100 bg-gray-50/60 px-6 py-4 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="px-4 py-2 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-200/60 transition-colors cursor-pointer"
          >
            Batal
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors cursor-pointer disabled:cursor-not-allowed disabled:opacity-70"
          >
            {submitting ? (
              <>
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                Mengirim...
              </>
            ) : (
              <>
                <CheckCircle2 className="w-4 h-4" /> Ajukan Dana
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
