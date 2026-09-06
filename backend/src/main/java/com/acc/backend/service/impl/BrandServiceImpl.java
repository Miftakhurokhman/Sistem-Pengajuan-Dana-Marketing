package com.acc.backend.service.impl;

import com.acc.backend.domain.dto.res.ResListBrand;
import com.acc.backend.repository.MasterBrandRepository;
import com.acc.backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final MasterBrandRepository masterBrandRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ResListBrand> getBrandDropdown() {
        return masterBrandRepository.findByIsActiveTrueAndIsDeletedFalseOrderByNamaBrandAsc()
                .stream()
                .map(brand -> ResListBrand.builder()
                        .id(brand.getId())
                        .brandCode(brand.getKodeBrand())
                        .brandName(brand.getNamaBrand())
                        .build())
                .toList();
    }
}