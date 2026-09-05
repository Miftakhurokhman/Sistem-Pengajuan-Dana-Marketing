import { Link, useLocation, Outlet } from 'react-router-dom';
import { Wallet, LogOut, User, Building2, ShieldCheck } from 'lucide-react';

export const MainLayout = () => {
  const location = useLocation();

  // Mock data user login (nanti diganti dengan state/context Auth dari Spring Boot)
  const user = {
    name: 'Miftakhurokman',
    role: 'Branch Manager (BM)',
    branch: 'ACC Jakarta Selatan',
  };

  const handleLogout = () => {
    alert('Berhasil logout dari sistem');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 1. TOPBAR / HEADER */}
      <header className="h-16 bg-white border-b border-gray-200 px-6 flex items-center justify-between sticky top-0 z-30">
        {/* Identitas Aplikasi / Brand */}
        <div className="flex items-center gap-3">
          <div className="bg-blue-600 text-white p-2 rounded-lg font-bold text-lg tracking-wider">
            ACC
          </div>
          <div>
            <h1 className="font-bold text-gray-800 text-sm leading-tight">
              PT Astra Credit Companies
            </h1>
            <p className="text-xs text-gray-400">Sistem Pengajuan Dana Marketing</p>
          </div>
        </div>

        {/* Informasi Akun Login & Logout */}
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3 border-r border-gray-200 pr-6">
            <div className="w-9 h-9 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-sm">
              <User className="w-5 h-5" />
            </div>
            <div className="text-right">
              <p className="text-xs font-bold text-gray-800">{user.name}</p>
              <div className="flex items-center justify-end gap-1 text-[11px] text-gray-500">
                <ShieldCheck className="w-3 h-3 text-blue-600" />
                <span>{user.role}</span>
                <span className="text-gray-300">•</span>
                <Building2 className="w-3 h-3 text-gray-400" />
                <span>{user.branch}</span>
              </div>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="flex items-center gap-2 text-xs font-medium text-red-600 hover:text-red-700 hover:bg-red-50 px-3 py-2 rounded-lg transition-colors cursor-pointer"
            title="Keluar dari sistem"
          >
            <LogOut className="w-4 h-4" />
            <span>Logout</span>
          </button>
        </div>
      </header>

      <div className="flex flex-1">
        {/* 2. SIDEBAR NAVIGATION */}
        <aside className="w-64 bg-white border-r border-gray-200 p-4 space-y-2 shrink-0 min-h-[calc(100vh-4rem)]">
          <div className="px-3 py-2 text-[11px] font-bold text-gray-400 uppercase tracking-wider">
            Menu Utama
          </div>

          <nav className="space-y-1">
            <Link
              to="/"
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                location.pathname === '/' || location.pathname.startsWith('/submission')
                  ? 'bg-blue-50 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
              }`}
            >
              <Wallet className="w-4 h-4" />
              <span>Pengajuan Dana</span>
            </Link>
          </nav>
        </aside>

        {/* 3. MAIN CONTENT AREA */}
        <main className="flex-1 p-8 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};