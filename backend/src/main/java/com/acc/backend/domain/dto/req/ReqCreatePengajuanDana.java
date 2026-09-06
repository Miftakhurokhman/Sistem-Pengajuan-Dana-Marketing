package com.acc.backend.domain.dto.req;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreatePengajuanDana {

    @NotBlank(message = "Judul kegiatan tidak boleh kosong")
    @Size(max = 150, message = "Judul kegiatan maksimal 150 karakter")
    private String judulKegiatan;

    @NotBlank(message = "Deskripsi kegiatan tidak boleh kosong")
    @Size(max = 500, message = "Deskripsi kegiatan maksimal 500 karakter")
    private String deskripsiKegiatan;

    @NotNull(message = "Nominal pengajuan tidak boleh kosong")
    @Positive(message = "Nominal pengajuan harus lebih besar dari 0")
    private BigInteger nominalPengajuan;

    @NotNull(message = "Tanggal kegiatan tidak boleh kosong")
    @FutureOrPresent(message = "Tanggal kegiatan minimal hari ini")
    private LocalDate tanggalKegiatan;

    @NotBlank(message = "Nama bank tidak boleh kosong")
    @Size(max = 100, message = "Nama bank maksimal 100 karakter")
    private String namaBank;

    @NotBlank(message = "Nomor rekening tidak boleh kosong")
    private String nomorRekening;

    @NotNull(message = "Brand ID tidak boleh kosong")
    private Long brandId;

    @NotBlank(message = "Nama pemilik rekening tidak boleh kosong")
    @Size(max = 150, message = "Nama pemilik bank maksimal 150 karakter")
    private String namaPemilikRekening;

    @NotNull(message = "File proposal tidak boleh kosong")
    private MultipartFile proposalFile; // Menerima file PDF langsung
}