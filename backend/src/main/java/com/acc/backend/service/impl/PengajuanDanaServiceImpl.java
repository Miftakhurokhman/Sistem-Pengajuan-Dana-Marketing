package com.acc.backend.service.impl;

import com.acc.backend.domain.dto.req.ReqApprovePengajuanDana;
import com.acc.backend.domain.dto.req.ReqCreatePengajuanDana;
import com.acc.backend.domain.dto.req.ReqRejectPengajuanDana;
import com.acc.backend.domain.dto.res.*;
import com.acc.backend.domain.entity.*;
import com.acc.backend.repository.LogApprovalHistoryRepository;
import com.acc.backend.repository.MasterApprovalLimitRepository;
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
    private final MasterApprovalLimitRepository masterApprovalLimitRepository;

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

        List<ResApprovalHistory> histories = logApprovalHistoryRepository.findByPengajuanDanaIdOrderByActionDateAsc(entity.getId())
                .stream()
                .map(history -> ResApprovalHistory.builder()
                        .id(history.getId())
                        .approverName(history.getApprover() != null ? history.getApprover().getFullName() : null)
                        .approverRole(history.getApproverRole())
                        .action(history.getStatus())
                        .notes(history.getNotes())
                        .actionAt(history.getActionDate())
                        .build())
                .toList();

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
                .createdBy(currentUser.getNpk())
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

    @Override
    @Transactional
    public ResDetailPengajuanDana approvePengajuanDana(Long id, ReqApprovePengajuanDana request, MasterUser currentUser) {
        // 1. Cari data pengajuan dana
        PengajuanDana pengajuan = pengajuanDanaRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Pengajuan dana dengan ID " + id + " tidak ditemukan."));

        String currentStatus = pengajuan.getStatus();
        String userRole = currentUser.getRole().getRoleCode(); // misal: "BM", "RRSH", "BRM", "RRSDH", "CMSO", "COO"
        BigDecimal nominalPengajuan = pengajuan.getNominalPengajuan();

        // 2. Validasi Hak Akses Role & Scope (Cabang, Area, Brand)
        validateRoleAndScope(pengajuan, currentUser, currentStatus, userRole);

        // 3. Tentukan Status Selanjutnya berdasarkan Nominal & Limit Role
        String nextStatus = determineNextStatus(currentStatus, userRole, nominalPengajuan, currentUser);

        // 4. Update Data Approval Pengajuan Dana
        pengajuan.setStatus(nextStatus);
        pengajuan.setUpdatedAt(LocalDateTime.now());

        String catatan = (request != null && request.getCatatan() != null) ? request.getCatatan() : null;
        pengajuan.setUpdatedBy(currentUser.getNpk());

        // 5. Simpan Perubahan
        PengajuanDana savedPengajuan = pengajuanDanaRepository.save(pengajuan);

        // 6. Simpan Log Approval History
        LogApprovalHistory approvalLog = LogApprovalHistory.builder()
                .pengajuanDana(savedPengajuan)
                .approver(currentUser)
                .approverRole(userRole)
                .status("Disetujui") // Aksi yang dilakukan oleh approver
                .notes(catatan != null ? catatan : "Disetujui oleh " + currentUser.getFullName() + " (" + userRole + ")")
                .actionDate(LocalDateTime.now())
                .isDeleted(false)
                .createdBy(currentUser.getNpk())
                .createdAt(LocalDateTime.now())
                .build();

        logApprovalHistoryRepository.save(approvalLog);

        return getDetailPengajuan(savedPengajuan.getId(), currentUser);
    }

    @Override
    @Transactional
    public ResDetailPengajuanDana rejectPengajuanDana(Long id, ReqRejectPengajuanDana request, MasterUser currentUser) {
        // 1. Cari data pengajuan dana
        PengajuanDana pengajuan = pengajuanDanaRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Pengajuan dana dengan ID " + id + " tidak ditemukan."));

        String currentStatus = pengajuan.getStatus();
        String userRole = currentUser.getRole().getRoleCode();

        // 2. Validasi Hak Akses Role & Scope (Sama seperti approve)
        validateRoleAndScope(pengajuan, currentUser, currentStatus, userRole);

        // 3. Update Status Pengajuan Dana ke "Ditolak"
        pengajuan.setStatus("Ditolak");
        pengajuan.setUpdatedAt(LocalDateTime.now());
        pengajuan.setUpdatedBy(currentUser.getNpk());

        // 4. Simpan Perubahan Pengajuan Dana
        PengajuanDana savedPengajuan = pengajuanDanaRepository.save(pengajuan);

        // 5. Simpan Log Approval History
        LogApprovalHistory rejectLog = LogApprovalHistory.builder()
                .pengajuanDana(savedPengajuan)
                .approver(currentUser)
                .approverRole(userRole)
                .status("Ditolak")
                .notes(request.getCatatan())
                .actionDate(LocalDateTime.now())
                .isDeleted(false)
                .createdBy(currentUser.getNpk())
                .createdAt(LocalDateTime.now())
                .build();

        logApprovalHistoryRepository.save(rejectLog);

        // 6. Return Response Detail Pengajuan
        return getDetailPengajuan(savedPengajuan.getId(), currentUser);
    }

    @Override
    @Transactional
    public ResDetailPengajuanDana pencairkanPengajuanDana(Long id, MultipartFile buktiTransfer, MasterUser currentUser) {
        PengajuanDana pengajuan = pengajuanDanaRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Pengajuan dana dengan ID " + id + " tidak ditemukan."));

        String roleCode = currentUser.getRole() != null ? currentUser.getRole().getRoleCode() : "";
        String normalizedRole = roleCode.toUpperCase();

        if (!normalizedRole.contains("PIC FINANCE") && !normalizedRole.contains("FINANCE")) {
            throw new RuntimeException("Hanya PIC Finance yang berhak melakukan pencairan dana.");
        }

        if (!"Siap Dicairkan".equalsIgnoreCase(pengajuan.getStatus())) {
            throw new RuntimeException("Pengajuan dana belum siap untuk dicairkan.");
        }

        String buktiTransferUrl = savePencairanFile(buktiTransfer);

        pengajuan.setPencairanUrl(buktiTransferUrl);
        pengajuan.setStatus("Dicairkan");
        pengajuan.setUpdatedAt(LocalDateTime.now());
        pengajuan.setUpdatedBy(currentUser.getNpk());

        PengajuanDana savedPengajuan = pengajuanDanaRepository.save(pengajuan);

        LogApprovalHistory cashOutLog = LogApprovalHistory.builder()
                .pengajuanDana(savedPengajuan)
                .approver(currentUser)
                .approverRole(currentUser.getRole().getRoleCode())
                .status("Dicairkan")
                .notes("Bukti transfer diunggah dan pencairan dilakukan oleh " + currentUser.getFullName())
                .actionDate(LocalDateTime.now())
                .isDeleted(false)
                .createdBy(currentUser.getNpk())
                .createdAt(LocalDateTime.now())
                .build();

        logApprovalHistoryRepository.save(cashOutLog);

        return getDetailPengajuan(savedPengajuan.getId(), currentUser);
    }

    /**
     * Helper untuk memvalidasi Role dan Scope (Cabang, Area, Brand)
     */
    private void validateRoleAndScope(PengajuanDana pengajuan, MasterUser user, String status, String roleCode) {
        switch (status) {
            case "Menunggu Approval BM":
                if (!"BM".equalsIgnoreCase(roleCode)) {
                    throw new RuntimeException("Hanya BM yang berhak melakukan approval pada tahap ini.");
                }
                // Validasi Scope Cabang
                if (user.getBranch() == null || pengajuan.getBranch() == null ||
                        !user.getBranch().getId().equals(pengajuan.getBranch().getId())) {
                    throw new RuntimeException("Anda hanya dapat menyetujui pengajuan dana untuk cabang Anda.");
                }
                break;

            case "Menunggu Approval RRSH":
                if (!"RRSH".equalsIgnoreCase(roleCode)) {
                    throw new RuntimeException("Hanya RRSH yang berhak melakukan approval pada tahap ini.");
                }
                // Validasi Scope Area (misal dari Branch -> Area)
                if (user.getBranch().getArea() == null || pengajuan.getArea() == null ||
                        !user.getBranch().getArea().getId().equals(pengajuan.getBranch().getArea().getId())) {
                    throw new RuntimeException("Anda hanya dapat menyetujui pengajuan dana untuk area Anda.");
                }
                break;

            case "Menunggu Approval BRM":
                if (!"BRM".equalsIgnoreCase(roleCode)) {
                    throw new RuntimeException("Hanya BRM yang berhak melakukan approval pada tahap ini.");
                }
                // Validasi Scope Brand
                if (user.getBrand() == null || pengajuan.getBrand() == null ||
                        !user.getBrand().getId().equals(pengajuan.getBrand().getId())) {
                    throw new RuntimeException("Anda hanya dapat menyetujui pengajuan dana untuk brand Anda.");
                }
                break;

            case "Menunggu Approval RRSDH":
                if (!"RRSDH".equalsIgnoreCase(roleCode)) {
                    throw new RuntimeException("Hanya RRSDH yang berhak melakukan approval pada tahap ini.");
                }
                break;

            case "Menunggu Approval CMSO":
                if (!"CMSO".equalsIgnoreCase(roleCode)) {
                    throw new RuntimeException("Hanya CMSO yang berhak melakukan approval pada tahap ini.");
                }
                break;

            case "Menunggu Approval COO":
                if (!"COO".equalsIgnoreCase(roleCode)) {
                    throw new RuntimeException("Hanya COO yang berhak melakukan approval pada tahap ini.");
                }
                break;

            default:
                throw new RuntimeException("Pengajuan dana tidak dalam status yang memerlukan approval (Status: " + status + ").");
        }
    }

    /**
     * Helper untuk menentukan status selanjutnya berdasarkan Maksimal Nominal Approval
     */
    private String determineNextStatus(String currentStatus, String roleCode, BigDecimal nominal, MasterUser user) {
        // Ambil max limit nominal dari Role atau User (misal dari masterRole)
        BigDecimal maxLimit = masterApprovalLimitRepository
                .findByRoleIdAndIsActiveTrueAndIsDeletedFalse(user.getRole().getId())
                .map(MasterApprovalLimit::getMaxNominal)
                .orElse(BigDecimal.ZERO);

        switch (roleCode.toUpperCase()) {
            case "BM":
                return "Menunggu Approval RRSH";

            case "RRSH":
                return "Menunggu Approval BRM";

            case "BRM":
                if (maxLimit != null && nominal.compareTo(maxLimit) <= 0) {
                    return "Siap Dicairkan";
                }
                return "Menunggu Approval RRSDH";

            case "RRSDH":
                if (maxLimit != null && nominal.compareTo(maxLimit) <= 0) {
                    return "Siap Dicairkan";
                }
                return "Menunggu Approval CMSO";

            case "CMSO":
                if (maxLimit != null && nominal.compareTo(maxLimit) <= 0) {
                    return "Siap Dicairkan";
                }
                return "Menunggu Approval COO";

            case "COO":
                return "Siap Dicairkan";

            default:
                throw new RuntimeException("Role pengguna tidak terdaftar dalam matriks approval.");
        }
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

    private String savePencairanFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File bukti transfer wajib diunggah.");
        }

        long maxSizeBytes = 1 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("Ukuran file bukti transfer tidak boleh lebih dari 1 MB.");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        boolean isPdfExtension = originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf");
        boolean isPdfContentType = contentType != null && (
                contentType.equalsIgnoreCase("application/pdf") ||
                        contentType.equalsIgnoreCase("application/x-pdf")
        );

        if (!isPdfExtension && !isPdfContentType) {
            throw new RuntimeException("Format file bukti transfer harus berupa PDF.");
        }

        try {
            String uploadDir = "uploads/pencairan/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + uploadDir + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file bukti transfer: " + e.getMessage());
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