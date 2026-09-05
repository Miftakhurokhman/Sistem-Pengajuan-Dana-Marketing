import type { ApprovalStatus } from '../../types/submission';

interface StatusBadgeProps {
  status: ApprovalStatus;
}

export const StatusBadge = ({ status }: StatusBadgeProps) => {
  // Menentukan skema warna berdasarkan status
  const getBadgeStyle = (status: ApprovalStatus) => {
    if (status.startsWith('Menunggu')) return 'bg-yellow-100 text-yellow-800 border border-yellow-200';
    if (status === 'Siap Dicairkan') return 'bg-blue-100 text-blue-800 border border-blue-200';
    if (status === 'Dicairkan') return 'bg-green-100 text-green-800 border border-green-200';
    if (status === 'Ditolak') return 'bg-red-100 text-red-800 border border-red-200';
    return 'bg-gray-100 text-gray-800 border border-gray-200'; // Kadaluarsa
  };

  return (
    <span className={`text-xs font-semibold px-2.5 py-1 rounded-full inline-block ${getBadgeStyle(status)}`}>
      {status}
    </span>
  );
};