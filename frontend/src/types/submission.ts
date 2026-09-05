export type ApprovalStatus = 
  | 'Menunggu Approval BM'
  | 'Menunggu Approval RRSH'
  | 'Menunggu Approval BRM'
  | 'Menunggu Approval RRSDH'
  | 'Menunggu Approval CMSO'
  | 'Menunggu Approval COO'
  | 'Siap Dicairkan'
  | 'Dicairkan'
  | 'Ditolak'
  | 'Kadaluarsa';

export interface Submission {
  id: string;
  submissionNo: string;
  applicantName: string;
  branchName: string;
  nominal: number;
  description: string;
  status: ApprovalStatus;
  createdAt: string;
  proposalUrl: string;
}

export interface ApprovalHistory {
  id: number;
  role: string;
  approverName: string;
  status: 'Submitted' | 'Approved' | 'Pending' | 'Rejected';
  date: string;
  notes?: string;
}

export interface SubmissionItem {
  id: string;
  submissionNo: string;
  applicantName: string;
  branchName: string;
  nominal: number;
  createdAt: string;
  status: string;
  description: string;
  proposalUrl: string;
}