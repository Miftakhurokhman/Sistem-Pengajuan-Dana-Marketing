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
    public ResDetailPengajuanDana getDetailPengajuan(Long id) { // Ubah return type ke DTO murni
        // 1. Cari Entity PengajuanDana
        PengajuanDana entity = pengajuanDanaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data pengajuan dana tidak ditemukan dengan ID: " + id));

        // 2. Pengecekan Soft Delete
        if (Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new RuntimeException("Data pengajuan dana sudah dihapus.");
        }

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
                .approvalHistories(histories)
                .build();
    }
}