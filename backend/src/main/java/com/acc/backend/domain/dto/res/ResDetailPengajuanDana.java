package com.acc.backend.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResDetailPengajuanDana {
    // Tab 1: Detail Utama Pengajuan
    private Long id;
    private String nomorPengajuan;
    private String judulKegiatan;
    private BigDecimal nominalPengajuan;
    private String status;
    private LocalDate tanggalKegiatan;
    private String deskripsi;
    private String branchName;
    private String areaName;
    private String brandName;
    private String requesterName;
    private String proposalUrl;
    private String pencairanUrl;
    private String namaBank;
    private String nomorRekening;
    private String namaPemilikRekening;
    private Boolean berhakApprove;
    private Boolean berhakMencairkan;

    // Tab 2: Riwayat Approval
    private List<ResApprovalHistory> approvalHistories;
}