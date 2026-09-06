package com.acc.backend.service.impl;

import com.acc.backend.domain.dto.req.ReqCreatePengajuanDana;
import com.acc.backend.domain.dto.res.*;
import com.acc.backend.domain.entity.LogApprovalHistory;
import com.acc.backend.domain.entity.MasterBrand;
import com.acc.backend.domain.entity.MasterUser;
import com.acc.backend.domain.entity.PengajuanDana;
import com.acc.backend.repository.LogApprovalHistoryRepository;
import com.acc.backend.repository.MasterBrandRepository;
import com.acc.backend.repository.PengajuanDanaRepository;
import com.acc.backend.service.PengajuanDanaService;
import com.acc.backend.specification.PengajuanDanaSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PengajuanDanaServiceImpl implements PengajuanDanaService {

    // Gunakan private final agar ter-inject otomatis via Lombok @RequiredArgsConstructor
    private final LogApprovalHistoryRepository logApprovalHistoryRepository;
    private final PengajuanDanaRepository pengajuanDanaRepository;
    private final MasterBrandRepository masterBrandRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResListPengajuanDana> getListPengajuan(
            MasterUser user,
            int page,
            int size,
            String sortBy,
            String sortDir,
            Boolean isNeedApproval,
            String searchBy,
            String searchValue) {

        String roleCode = (user.getRole() != null) ? user.getRole().getRoleCode() : null;
        Long userId = user.getId();
        Long branchId = (user.getBranch() != null) ? user.getBranch().getId() : null;
        Long areaId = (user.getBranch() != null && user.getBranch().getArea() != null)
                ? user.getBranch().getArea().getId() : null;
        Long brandId = (user.getBrand() != null && user.getBrand().getId() != null)
                ? user.getBrand().getId() : null;

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<PengajuanDana> spec = PengajuanDanaSpecification.filter(
                roleCode,
                userId,
                branchId,
                areaId,
                brandId,
                isNeedApproval,
                searchBy,
                searchValue
        );

        Page<PengajuanDana> entityPage = pengajuanDanaRepository.findAll(spec, pageable);

        Page<ResListPengajuanDana> dtoPage = entityPage.map(entity -> ResListPengajuanDana.builder()
                .id(entity.getId())
                .nomorPengajuan(entity.getNomorPengajuan())
                .judulKegiatan(entity.getJudulKegiatan())
                .nominalPengajuan(entity.getNominalPengajuan())
                .status(entity.getStatus())
                .tanggalKegiatan(entity.getTanggalKegiatan())
                .branchName(entity.getBranch() != null ? entity.getBranch().getBranchName() : null)
                .areaName(entity.getBranch() != null && entity.getBranch().getArea() != null
                        ? entity.getBranch().getArea().getAreaName() : null)
                .brandName(entity.getBrand() != null ? entity.getBrand().getNamaBrand() : null)
                .requesterName(entity.getRequester() != null ? entity.getRequester().getFullName() : null)
                .build());

        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ResDetailPengajuanDana getDetailPengajuan(Long id, MasterUser currentUser) {
        PengajuanDana entity = pengajuanDanaRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Data pengajuan dana tidak ditemukan"));

        validateAccessDetail(currentUser, entity);

        boolean berhakApprove = checkBerhakApprove(currentUser, entity);
        boolean berhakMencairkan = checkBerhakMencairkan(currentUser, entity);

        List<ResApprovalHistory> histories = entity.getApprovalHistories() != null
                ? entity.getApprovalHistories().stream()
                .map(history -> ResApprovalHistory.builder()
                        .id(history.getId())
                        .approverName(history.getApprover() != null ? history.getApprover().getFullName() : null)
                        .approverRole(history.getApproverRole())
                        .action(history.getStatus())
                        .notes(history.getNotes())
                        .actionAt(history.getActionDate())
                        .build())
                .toList()
                : List.of();

        return ResDetailPengajuanDana.builder()
                .id(entity.getId())
                .nomorPengajuan(entity.getNomorPengajuan())
                .judulKegiatan(entity.getJudulKegiatan())
                .nominalPengajuan(entity.getNominalPengajuan())
                .status(entity.getStatus())
                .tanggalKegiatan(entity.getTanggalKegiatan())
                .deskripsi(entity.getDeskripsiKegiatan())
                .branchName(entity.getBranch() != null ? entity.getBranch().getBranchName() : null)
                .areaName(entity.getBranch() != null && entity.getBranch().getArea() != null
                        ? entity.getBranch().getArea().getAreaName() : null)
                .brandName(entity.getBrand() != null ? entity.getBrand().getNamaBrand() : null)
                .requesterName(entity.getRequester() != null ? entity.getRequester().getFullName() : null)
                .proposalUrl(entity.getProposalUrl())
                .pencairanUrl(entity.getPencairanUrl())
                .namaBank(entity.getNamaBank())
                .nomorRekening(entity.getNomorRekening())
                .namaPemilikRekening(entity.getNamaPemilikRekening())
                .berhakApprove(berhakApprove)
                .berhakMencairkan(berhakMencairkan)
                .approvalHistories(histories)
                .build();
    }

    @Override
    @Transactional
    public ResDetailPengajuanDana createPengajuanDana(ReqCreatePengajuanDana request, MasterUser currentUser) {
        // 1. Validasi Role: Samakan penulisan string role ("PIC_SALES" atau "SALES")
        String roleCode = currentUser.getRole() != null ? currentUser.getRole().getRoleCode().toUpperCase() : "";
        if (!roleCode.contains("PIC SALES") && !roleCode.contains("SALES")) {
            throw new RuntimeException("Akses ditolak: Hanya PIC Sales yang dapat membuat pengajuan dana.");
        }

        MasterBrand brand = masterBrandRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand tidak ditemukan atau sudah tidak aktif."));

        // 2. Generate Nomor Pengajuan Unik
        String nomorPengajuan = generateNomorPengajuan();

        // 3. Status Awal & Upload Proposal File
        String initialStatus = "Menunggu Approval BM";
        String proposalUrl = saveProposalFile(request.getProposalFile());

        // 4. Build Entity PengajuanDana
        PengajuanDana entity = PengajuanDana.builder()
                .nomorPengajuan(nomorPengajuan)
                .judulKegiatan(request.getJudulKegiatan())
                .deskripsiKegiatan(request.getDeskripsiKegiatan())
                .nominalPengajuan(new BigDecimal(request.getNominalPengajuan()))
                .tanggalKegiatan(request.getTanggalKegiatan())
                .namaBank(request.getNamaBank())
                .area(currentUser.getBranch().getArea())
                .nomorRekening(request.getNomorRekening())
                .namaPemilikRekening(request.getNamaPemilikRekening())
                .proposalUrl(proposalUrl) // FIX: Menggunakan variabel proposalUrl dari hasil simpan file
                .status(initialStatus)
                .isActive(true)          // FIX: Explicit set true agar terbaca di query getDetailPengajuan
                .isDeleted(false)        // FIX: Explicit set false agar terbaca di query getDetailPengajuan
                .requester(currentUser)
                .branch(currentUser.getBranch())
                .brand(brand)
                .build();

        PengajuanDana savedEntity = pengajuanDanaRepository.save(entity);

        // 5. Simpan Log History Pertama (SUBMITTED)
        LogApprovalHistory initialLog = LogApprovalHistory.builder()
                .pengajuanDana(savedEntity)
                .approver(currentUser)
                .approverRole(currentUser.getRole().getRoleCode())
                .status("Diajukan")
                .notes("Pengajuan dana dibuat oleh " + currentUser.getFullName())
                .actionDate(LocalDateTime.now())
                .build();

        logApprovalHistoryRepository.save(initialLog);

        // 6. Kembalikan Response Detail Pengajuan
        return getDetailPengajuan(savedEntity.getId(), currentUser);
    }

    private String generateNomorPengajuan() {
        String prefix = "SPD/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "/";
        long countToday = pengajuanDanaRepository.count();
        return prefix + String.format("%04d", countToday + 1);
    }

    private boolean checkBerhakApprove(MasterUser user, PengajuanDana entity) {
        if (user == null || user.getRole() == null || entity.getStatus() == null) {
            return false;
        }

        String role = user.getRole().getRoleCode().toUpperCase();
        String status = entity.getStatus();

        Long userBranchId = user.getBranch() != null ? user.getBranch().getId() : null;
        Long userAreaId = (user.getBranch() != null && user.getBranch().getArea() != null)
                ? user.getBranch().getArea().getId() : null;
        Long userBrandId = user.getBrand() != null ? user.getBrand().getId() : null;

        Long entityBranchId = entity.getBranch() != null ? entity.getBranch().getId() : null;
        Long entityAreaId = (entity.getBranch() != null && entity.getBranch().getArea() != null)
                ? entity.getBranch().getArea().getId() : null;
        Long entityBrandId = entity.getBrand() != null ? entity.getBrand().getId() : null;

        if (role.contains("BM") && !role.contains("BRM") && "Menunggu Approval BM".equalsIgnoreCase(status)) {
            return userBranchId != null && userBranchId.equals(entityBranchId);
        }
        if (role.contains("RRSH") && "Menunggu Approval RRSH".equalsIgnoreCase(status)) {
            return userAreaId != null && userAreaId.equals(entityAreaId);
        }
        if (role.contains("BRM") && "Menunggu Approval BRM".equalsIgnoreCase(status)) {
            return userBrandId != null && userBrandId.equals(entityBrandId);
        }
        if (role.contains("RRSDH") && "Menunggu Approval RRSDH".equalsIgnoreCase(status)) {
            return true;
        }
        if (role.contains("CMSO") && "Menunggu Approval CMSO".equalsIgnoreCase(status)) {
            return true;
        }
        if (role.contains("COO") && "Menunggu Approval COO".equalsIgnoreCase(status)) {
            return true;
        }

        return false;
    }

    /**
     * Helper untuk menyimpan file ke folder lokal server
     * Termasuk validasi format PDF dan ukuran max 1 MB
     */
    private String saveProposalFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File proposal wajib diunggah.");
        }

        // 1. Validasi Ukuran File (Maksimal 1 MB = 1 * 1024 * 1024 bytes)
        long maxSizeBytes = 1 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("Ukuran file proposal tidak boleh lebih dari 1 MB.");
        }

        // 2. Validasi Format File (Cek ekstensi .pdf DAN/ATAU Content-Type)
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        boolean isPdfExtension = originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf");
        boolean isPdfContentType = contentType != null && (
                contentType.equalsIgnoreCase("application/pdf") ||
                        contentType.equalsIgnoreCase("application/x-pdf")
        );

        // Lolos jika ekstensinya .pdf ATAU Content-Type nya valid
        if (!isPdfExtension && !isPdfContentType) {
            throw new RuntimeException("Format file proposal harus berupa PDF.");
        }

        try {
            String uploadDir = "uploads/proposals/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file proposal: " + e.getMessage());
        }
    }

    private boolean checkBerhakMencairkan(MasterUser user, PengajuanDana entity) {
        if (user == null || user.getRole() == null || entity.getStatus() == null) {
            return false;
        }

        String role = user.getRole().getRoleCode().toUpperCase();
        String status = entity.getStatus();

        if ("Siap Dicairkan".equalsIgnoreCase(status)) {
            return role.contains("PIC FINANCE") || role.contains("FINANCE");
        }

        return false;
    }

    private void validateAccessDetail(MasterUser user, PengajuanDana entity) {
        if (user == null || user.getRole() == null) {
            throw new RuntimeException("Akses ditolak: Data pengguna tidak valid.");
        }

        String role = user.getRole().getRoleCode().toUpperCase();

        if (role.contains("PIC_SALES") || role.contains("SALES")) {
            Long requesterId = (entity.getRequester() != null) ? entity.getRequester().getId() : null;
            if (requesterId == null || !requesterId.equals(user.getId())) {
                throw new RuntimeException("Anda tidak memiliki hak akses untuk melihat pengajuan ini.");
            }
        } else if (role.contains("BM") && !role.contains("BRM")) {
            Long userBranchId = (user.getBranch() != null) ? user.getBranch().getId() : null;
            Long entityBranchId = (entity.getBranch() != null) ? entity.getBranch().getId() : null;
            if (userBranchId == null || !userBranchId.equals(entityBranchId)) {
                throw new RuntimeException("Anda tidak memiliki hak akses ke pengajuan cabang lain.");
            }
        } else if (role.contains("RRSH")) {
            Long userAreaId = (user.getBranch() != null && user.getBranch().getArea() != null)
                    ? user.getBranch().getArea().getId() : null;
            Long entityAreaId = (entity.getBranch() != null && entity.getBranch().getArea() != null)
                    ? entity.getBranch().getArea().getId() : null;
            if (userAreaId == null || !userAreaId.equals(entityAreaId)) {
                throw new RuntimeException("Anda tidak memiliki hak akses ke pengajuan area lain.");
            }
        } else if (role.contains("BRM")) {
            Long userBrandId = (user.getBrand() != null) ? user.getBrand().getId() : null;
            Long entityBrandId = (entity.getBrand() != null) ? entity.getBrand().getId() : null;
            if (userBrandId == null || !userBrandId.equals(entityBrandId)) {
                throw new RuntimeException("Anda tidak memiliki hak akses ke pengajuan brand lain.");
            }
        }
    }
}