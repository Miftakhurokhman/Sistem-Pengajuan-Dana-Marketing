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