package com.acc.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trn_pengajuan_dana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PengajuanDana extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nomor_pengajuan", nullable = false, unique = true, length = 50)
    private String nomorPengajuan;

    @Column(name = "judul_kegiatan", nullable = false, length = 150)
    private String judulKegiatan;

    @Column(name = "deskripsi_kegiatan", columnDefinition = "TEXT")
    private String deskripsiKegiatan;

    @Column(name = "nominal_pengajuan", nullable = false, precision = 15, scale = 2)
    private BigDecimal nominalPengajuan;

    // --- Informasi Rekening Tujuan Pencairan ---
    @Column(name = "nama_bank", length = 100)
    private String namaBank;

    @Column(name = "nomor_rekening", length = 50)
    private String nomorRekening;

    @Column(name = "nama_pemilik_rekening", length = 150)
    private String namaPemilikRekening;

    // Kolom untuk menyimpan Path / URL file PDF proposal
    @Column(name = "proposal_url", columnDefinition = "TEXT")
    private String proposalUrl;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "tanggal_kegiatan")
    private LocalDate tanggalKegiatan;

    // User yang mengajukan (Requester)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MasterUser requester;

    // Cabang pengajuan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private MasterBranch branch;

    // Area pengajuan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private MasterArea area;

    // Brand terkait pengajuan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private MasterBrand brand;
}