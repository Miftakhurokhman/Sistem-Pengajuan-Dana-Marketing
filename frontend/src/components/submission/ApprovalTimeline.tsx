import { CheckCircle2, Clock, UserCheck } from 'lucide-react';
import type { ApprovalHistory } from '../../types/submission';

interface ApprovalTimelineProps {
  history: ApprovalHistory[];
}

export const ApprovalTimeline = ({ history }: ApprovalTimelineProps) => {
  return (
    <div className="relative pl-6 border-l-2 border-gray-200 space-y-6 my-2">
      {history.map((item) => {
        const isApproved = item.status === 'Approved';
        const isSubmitted = item.status === 'Submitted';
        const isPending = item.status === 'Pending';

        return (
          <div key={item.id} className="relative">
            {/* Icon Indicator */}
            <div
              className={`absolute -left-[31px] top-0 w-6 h-6 rounded-full flex items-center justify-center border-2 bg-white ${
                isApproved || isSubmitted
                  ? 'border-green-600 text-green-600'
                  : isPending
                  ? 'border-amber-500 text-amber-500'
                  : 'border-red-600 text-red-600'
              }`}
            >
              {isApproved || isSubmitted ? (
                <CheckCircle2 className="w-3.5 h-3.5" />
              ) : (
                <Clock className="w-3.5 h-3.5 animate-pulse" />
              )}
            </div>

            {/* Item Card */}
            <div className="bg-gray-50 p-4 rounded-lg border border-gray-100 space-y-2">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-gray-800 text-sm">{item.role}</span>
                  <span className="text-gray-400">•</span>
                  <span className="text-xs text-gray-600 font-medium flex items-center gap-1">
                    <UserCheck className="w-3.5 h-3.5 text-gray-400" />
                    {item.approverName}
                  </span>
                </div>
                <span className="text-[11px] font-medium text-gray-400">{item.date}</span>
              </div>

              <div className="flex items-center gap-2 pt-1">
                <span
                  className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${
                    isApproved || isSubmitted
                      ? 'bg-green-100 text-green-700'
                      : isPending
                      ? 'bg-amber-100 text-amber-700'
                      : 'bg-red-100 text-red-700'
                  }`}
                >
                  {item.status === 'Submitted'
                    ? 'Diajukan'
                    : item.status === 'Approved'
                    ? 'Disetujui'
                    : 'Menunggu Process'}
                </span>
              </div>

              {item.notes && (
                <p className="text-xs text-gray-600 bg-white p-3 rounded border border-gray-100 italic mt-2">
                  "{item.notes}"
                </p>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};