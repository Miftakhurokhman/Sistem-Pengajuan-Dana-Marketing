import type { Submission } from '../types/submission';

export const MOCK_SUBMISSIONS: Submission[] = [
  {
    id: '1',
    submissionNo: 'REQ-202609-001',
    applicantName: 'Budi Santoso',
    branchName: 'ACC Jakarta Selatan',
    nominal: 15000000,
    description: 'Kegiatan Exhibition & Display Unit di Mall Kasablanka selama weekend.',
    status: 'Menunggu Approval BM',
    createdAt: '2026-09-04 09:00',
    proposalUrl: '/sample-proposal.pdf',
  },
  {
    id: '2',
    submissionNo: 'REQ-202609-002',
    applicantName: 'Siti Rahma',
    branchName: 'ACC Bandung',
    nominal: 45000000,
    description: 'Sponsor Gathering Komunitas Otomotif Jawa Barat dan promo bunga rendah.',
    status: 'Menunggu Approval BRM',
    createdAt: '2026-09-03 14:30',
    proposalUrl: '/sample-proposal.pdf',
  },
  {
    id: '3',
    submissionNo: 'REQ-202608-089',
    applicantName: 'Ahmad Faisal',
    branchName: 'ACC Surabaya',
    nominal: 80000000,
    description: 'Digital Marketing Campaign & Canvassing Regional Jawa Timur.',
    status: 'Dicairkan',
    createdAt: '2026-08-28 11:15',
    proposalUrl: '/sample-proposal.pdf',
  },
];