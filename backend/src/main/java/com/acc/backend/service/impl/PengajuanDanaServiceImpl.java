package com.acc.backend.service.impl;

import com.acc.backend.domain.dto.res.PageResponse;
import com.acc.backend.domain.dto.res.ResListPengajuanDana;
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
            String search,
            String status) {

        // 1. Ekstrak data role, userId, branchId, dan areaId langsung dari entity MasterUser
        String roleCode = (user.getRole() != null) ? user.getRole().getRoleCode() : null;
        Long userId = user.getId();
        Long branchId = (user.getBranch() != null) ? user.getBranch().getId() : null;
        Long areaId = (user.getBranch() != null && user.getBranch().getArea() != null)
                ? user.getBranch().getArea().getId() : null;

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
                search,
                status
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
}