package com.acc.backend.controller;

import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.PageResponse;
import com.acc.backend.domain.dto.res.ResDetailPengajuanDana;
import com.acc.backend.domain.dto.res.ResListPengajuanDana;
import com.acc.backend.security.CustomUserDetails;
import com.acc.backend.service.PengajuanDanaService; // Sesuaikan package service kamu
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pengajuan-dana")
@RequiredArgsConstructor
public class PengajuanDanaController {

    private final PengajuanDanaService pengajuanDanaService;

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<ResListPengajuanDana>>> getListPengajuan(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "true") Boolean isNeedApproval,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) String searchValue

    ) {
        PageResponse<ResListPengajuanDana> result = pengajuanDanaService.getListPengajuan(
                currentUser.getUser(),
                page,
                size,
                sortBy,
                sortDir,
                isNeedApproval,
                searchBy,
                searchValue
        );

        return ResponseEntity.ok(BaseResponse.ok( "Berhasil mengambil daftar pengajuan dana", result));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResDetailPengajuanDana>> getDetailPengajuan(@PathVariable Long id) {
        ResDetailPengajuanDana result = pengajuanDanaService.getDetailPengajuan(id);
        return ResponseEntity.ok(BaseResponse.ok("Berhasil mengambil detail pengajuan dana", result));
    }
}