package com.acc.backend.service;

import com.acc.backend.domain.dto.res.ResListBrand;

import java.util.List;

public interface BrandService {

    List<ResListBrand> getBrandDropdown();
}