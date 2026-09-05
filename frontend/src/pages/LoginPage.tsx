import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Lock, Eye, EyeOff, LogIn, AlertCircle } from 'lucide-react';

export const LoginPage = () => {
  const navigate = useNavigate();

  // State Form
  const [npk, setNpk] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  
  // State Feedback & Loading
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Handle Form Submit
  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setError('');

    // Validasi Sederhana
    if (!npk.trim() || !password.trim()) {
      setError('NPK dan Password wajib diisi.');
      return;
    }

    setIsLoading(true);

    // Simulasi Proses Authentication API (dapat diganti dengan Service/Context Auth)
    setTimeout(() => {
      setIsLoading(false);

      // Dummy credentials check untuk testing/demo
      if (npk === '123456' && password === 'password123') {
        // Berhasil login -> Arahkan ke Halaman Utama / List Pengajuan
        navigate('/');
      } else if (npk !== '123456') {
        setError('NPK tidak terdaftar dalam sistem.');
      } else {
        setError('Password yang Anda masukkan salah.');
      }
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center items-center p-4">
      <div className="w-full max-w-md">
        
        {/* Header / Brand Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 bg-blue-600 rounded-xl shadow-md text-white font-bold text-xl mb-3">
            SPD
          </div>
          <h1 className="text-2xl font-bold text-gray-800">Sistem Pengajuan Dana</h1>
          <p className="text-xs text-gray-500 mt-1">
            Silakan masuk menggunakan NPK dan Password Anda
          </p>
        </div>

        {/* Form Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 sm:p-8 space-y-6">
          
          {/* Alert Message jika ada Error */}
          {error && (
            <div className="flex items-center gap-2.5 p-3.5 bg-red-50 border border-red-100 text-red-600 text-xs rounded-xl">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span className="font-medium">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            
            {/* Input NPK */}
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1.5">
                NPK (Nomor Pokok Karyawan)
              </label>
              <div className="relative">
                <User className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  value={npk}
                  onChange={(e) => setNpk(e.target.value)}
                  placeholder="Masukkan NPK (contoh: 123456)"
                  className="w-full pl-9 pr-4 py-2.5 text-xs border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800 font-medium"
                  disabled={isLoading}
                  autoFocus
                />
              </div>
            </div>

            {/* Input Password */}
            <div>
              <div className="flex justify-between items-center mb-1.5">
                <label className="block text-xs font-semibold text-gray-700">
                  Password
                </label>
              </div>
              <div className="relative">
                <Lock className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Masukkan password"
                  className="w-full pl-9 pr-10 py-2.5 text-xs border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-800"
                  disabled={isLoading}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none cursor-pointer"
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            {/* Tombol Submit Login */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-2 inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 disabled:bg-blue-300 text-white font-semibold text-xs rounded-xl shadow-sm transition-colors cursor-pointer"
            >
              {isLoading ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <>
                  <LogIn className="w-4 h-4" />
                  Masuk Sistem
                </>
              )}
            </button>
          </form>

          {/* Box Informasi Akses Demo */}
          <div className="pt-4 border-t border-gray-100 text-center">
            <p className="text-[11px] text-gray-400">
              Kredensial Akses Demo:
            </p>
            <p className="text-[11px] font-mono text-gray-600 mt-1">
              NPK: <span className="font-bold text-gray-800">123456</span> | Password: <span className="font-bold text-gray-800">password123</span>
            </p>
          </div>

        </div>

        {/* Footer info */}
        <p className="text-center text-[11px] text-gray-400 mt-6">
          &copy; {new Date().getFullYear()} PT Berijalan Akses Wijaya. All rights reserved.
        </p>
      </div>
    </div>
  );
};