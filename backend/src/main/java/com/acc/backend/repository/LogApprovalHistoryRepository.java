package com.acc.backend.repository;

import com.acc.backend.domain.entity.LogApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogApprovalHistoryRepository extends JpaRepository<LogApprovalHistory, Long> {

    // Ambil semua log approval berdasarkan ID Pengajuan Dana, diurutkan kronologis (lama ke baru)
    List<LogApprovalHistory> findByPengajuanDanaIdOrderByActionDateAsc(Long pengajuanDanaId);
}