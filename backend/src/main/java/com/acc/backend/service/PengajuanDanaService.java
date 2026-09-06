package com.acc.backend.service;

import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.PageResponse;
import com.acc.backend.domain.dto.res.ResDetailPengajuanDana;
import com.acc.backend.domain.dto.res.ResListPengajuanDana;
import com.acc.backend.domain.entity.MasterUser;
import org.springframework.security.core.Authentication;

public interface PengajuanDanaService {
    PageResponse<ResListPengajuanDana> getListPengajuan(
            MasterUser user,
            int page,
            int size,
            String sortBy,
            String sortDir,
            Boolean isNeedApproval,
            String searchBy,
            String statusValue
    );

    ResDetailPengajuanDana getDetailPengajuan(Long id);
}