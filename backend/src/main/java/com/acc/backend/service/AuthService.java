package com.acc.backend.service;

import com.acc.backend.domain.dto.req.ReqLogin;
import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.ResLogin;

public interface AuthService {
    BaseResponse<ResLogin> login(ReqLogin req);
}