package com.acc.backend.controller;

import com.acc.backend.domain.dto.req.ReqApprovePengajuanDana;
import com.acc.backend.domain.dto.req.ReqCreatePengajuanDana;
import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.PageResponse;
import com.acc.backend.domain.dto.res.ResDetailPengajuanDana;
import com.acc.backend.domain.dto.res.ResListPengajuanDana;
import com.acc.backend.security.CustomUserDetails;
import com.acc.backend.service.PengajuanDanaService; // Sesuaikan package service kamu
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<BaseResponse<ResDetailPengajuanDana>> getDetailPengajuan(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        ResDetailPengajuanDana result = pengajuanDanaService.getDetailPengajuan(id, currentUser.getUser());
        return ResponseEntity.ok(BaseResponse.ok("Berhasil mengambil detail pengajuan dana", result));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ResDetailPengajuanDana>> createPengajuanDana(
            @Valid @ModelAttribute ReqCreatePengajuanDana request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        ResDetailPengajuanDana result = pengajuanDanaService.createPengajuanDana(request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok("Berhasil membuat pengajuan dana", result));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<BaseResponse<ResDetailPengajuanDana>> approvePengajuanDana(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ReqApprovePengajuanDana request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (request == null) {
            request = new ReqApprovePengajuanDana();
        }

        ResDetailPengajuanDana result = pengajuanDanaService.approvePengajuanDana(id, request, currentUser.getUser());
        return ResponseEntity.ok(BaseResponse.ok("Pengajuan dana berhasil disetujui", result));
    }
}