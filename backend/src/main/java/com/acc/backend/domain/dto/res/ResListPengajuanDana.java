package com.acc.backend.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResListPengajuanDana {
    private Long id;
    private String nomorPengajuan;
    private String judulKegiatan;
    private BigDecimal nominalPengajuan;
    private String status;
    private LocalDate tanggalKegiatan;
    private String branchName;
    private String areaName;
    private String brandName;
    private String requesterName;
}