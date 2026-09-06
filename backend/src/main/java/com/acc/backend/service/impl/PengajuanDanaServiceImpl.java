package com.acc.backend.service.impl;

import com.acc.backend.domain.dto.res.*;
import com.acc.backend.domain.entity.MasterUser;
import com.acc.backend.domain.entity.PengajuanDana;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class PengajuanDanaServiceImpl implements PengajuanDanaService {

    private final PengajuanDanaRepository pengajuanDanaRepository;

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

        // 1. Ekstrak data role, userId, branchId, dan areaId langsung dari entity MasterUser
        String roleCode = (user.getRole() != null) ? user.getRole().getRoleCode() : null;
        Long userId = user.getId();
        Long branchId = (user.getBranch() != null) ? user.getBranch().getId() : null;
        Long areaId = (user.getBranch() != null && user.getBranch().getArea() != null)
                ? user.getBranch().getArea().getId() : null;
        Long brandId = (user.getBrand() != null && user.getBrand().getId() != null)
                ? user.getBrand().getId() : null;

        // 2. Tentukan Sorting & Pagination
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Susun Specification Filter
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

        // 4. Eksekusi Query ke Database
        Page<PengajuanDana> entityPage = pengajuanDanaRepository.findAll(spec, pageable);

        // 5. Mapping Entity ke DTO List
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
    public ResDetailPengajuanDana getDetailPengajuan(Long id, MasterUser currentUser) { // Ubah return type ke DTO murni
        // 1. Cari Entity PengajuanDana
        PengajuanDana entity = pengajuanDanaRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Data pengajuan dana tidak ditemukan"));

        validateAccessDetail(currentUser, entity);

        boolean berhakApprove = checkBerhakApprove(currentUser, entity);
        boolean berhakMencairkan = checkBerhakMencairkan(currentUser, entity);

        // Mapping List Riwayat Approval
        List<ResApprovalHistory> histories = entity.getApprovalHistories() != null
                ? entity.getApprovalHistories().stream()
                .map(history -> ResApprovalHistory.builder()
                        .id(history.getId())
                        .approverName(history.getApprover() != null ? history.getApprover().getFullName() : null)
                        .approverRole(history.getApproverRole())
                        .action(history.getStatus()) // Pakai getStatus()
                        .notes(history.getNotes())
                        .actionAt(history.getActionDate()) // Pakai getActionDate()
                        .build())
                .toList()
                : List.of();

        // 4. Mapping ke ResDetailPengajuanDana
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

    /**
     * Pengecekan apakah user yang login berhak melakukan Approval berdasarkan Role, Status, dan Scope Wilayah
     */
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

        // 1. BM -> Hanya jika status "Menunggu Approval BM" & Cabang Sesuai
        if (role.contains("BM") && !role.contains("BRM") && "Menunggu Approval BM".equalsIgnoreCase(status)) {
            return userBranchId != null && userBranchId.equals(entityBranchId);
        }
        // 2. RRSH -> Hanya jika status "Menunggu Approval RRSH" & Area Sesuai
        if (role.contains("RRSH") && "Menunggu Approval RRSH".equalsIgnoreCase(status)) {
            return userAreaId != null && userAreaId.equals(entityAreaId);
        }
        // 3. BRM -> Hanya jika status "Menunggu Approval BRM" & Brand Sesuai
        if (role.contains("BRM") && "Menunggu Approval BRM".equalsIgnoreCase(status)) {
            return userBrandId != null && userBrandId.equals(entityBrandId);
        }
        // 4. RRSDH -> Hanya jika status "Menunggu Approval RRSDH"
        if (role.contains("RRSDH") && "Menunggu Approval RRSDH".equalsIgnoreCase(status)) {
            return true;
        }
        // 5. CMSO -> Hanya jika status "Menunggu Approval CMSO"
        if (role.contains("CMSO") && "Menunggu Approval CMSO".equalsIgnoreCase(status)) {
            return true;
        }
        // 6. COO -> Hanya jika status "Menunggu Approval COO"
        if (role.contains("COO") && "Menunggu Approval COO".equalsIgnoreCase(status)) {
            return true;
        }

        return false;
    }

    /**
     * Pengecekan apakah user berhak mencairkan dana
     */
    private boolean checkBerhakMencairkan(MasterUser user, PengajuanDana entity) {
        if (user == null || user.getRole() == null || entity.getStatus() == null) {
            return false;
        }

        String role = user.getRole().getRoleCode().toUpperCase();
        String status = entity.getStatus();

        // Pencairan hanya diizinkan jika pengajuan sudah sepenuhnya disetujui
        if ("Disetujui".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status) || "Menunggu Pencairan".equalsIgnoreCase(status)) {
            // Sesuaikan role mana yang memegang proses pencairan (misal PIC Sales / Finance)
            return role.contains("PIC_SALES") || role.contains("FINANCE");
        }

        return false;
    }

    /**
     * Pengecekan apakah user memiliki hak akses untuk MELIHAT detail pengajuan ini
     */
    private void validateAccessDetail(MasterUser user, PengajuanDana entity) {
        if (user == null || user.getRole() == null) {
            throw new RuntimeException("Akses ditolak: Data pengguna tidak valid.");
        }

        String role = user.getRole().getRoleCode().toUpperCase();

        // 1. PIC_SALES -> Hanya boleh melihat pengajuan miliknya sendiri
        if (role.contains("PIC_SALES") || role.contains("SALES")) {
            Long requesterId = (entity.getRequester() != null) ? entity.getRequester().getId() : null;
            if (requesterId == null || !requesterId.equals(user.getId())) {
                throw new RuntimeException("Anda tidak memiliki hak akses untuk melihat pengajuan ini.");
            }
        }

        // 2. BM -> Hanya boleh melihat pengajuan di cabangnya
        else if (role.contains("BM") && !role.contains("BRM")) {
            Long userBranchId = (user.getBranch() != null) ? user.getBranch().getId() : null;
            Long entityBranchId = (entity.getBranch() != null) ? entity.getBranch().getId() : null;
            if (userBranchId == null || !userBranchId.equals(entityBranchId)) {
                throw new RuntimeException("Anda tidak memiliki hak akses ke pengajuan cabang lain.");
            }
        }

        // 3. RRSH -> Hanya boleh melihat pengajuan di areanya
        else if (role.contains("RRSH")) {
            Long userAreaId = (user.getBranch() != null && user.getBranch().getArea() != null)
                    ? user.getBranch().getArea().getId() : null;
            Long entityAreaId = (entity.getBranch() != null && entity.getBranch().getArea() != null)
                    ? entity.getBranch().getArea().getId() : null;
            if (userAreaId == null || !userAreaId.equals(entityAreaId)) {
                throw new RuntimeException("Anda tidak memiliki hak akses ke pengajuan area lain.");
            }
        }

        // 4. BRM -> Hanya boleh melihat pengajuan untuk brand-nya
        else if (role.contains("BRM")) {
            Long userBrandId = (user.getBrand() != null) ? user.getBrand().getId() : null;
            Long entityBrandId = (entity.getBrand() != null) ? entity.getBrand().getId() : null;
            if (userBrandId == null || !userBrandId.equals(entityBrandId)) {
                throw new RuntimeException("Anda tidak memiliki hak akses ke pengajuan brand lain.");
            }
        }

        // Role RRSDH, CMSO, COO memiliki akses global (tidak perlu filter scope)
    }
}