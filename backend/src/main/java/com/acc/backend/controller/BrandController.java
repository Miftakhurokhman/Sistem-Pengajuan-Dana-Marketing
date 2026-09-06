package com.acc.backend.controller;

import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.ResListBrand;
import com.acc.backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping()
    public ResponseEntity<BaseResponse<List<ResListBrand>>> getBrandDropdown() {
        List<ResListBrand> result = brandService.getBrandDropdown();
        return ResponseEntity.ok(BaseResponse.ok("Berhasil mengambil data list brand", result));
    }
}