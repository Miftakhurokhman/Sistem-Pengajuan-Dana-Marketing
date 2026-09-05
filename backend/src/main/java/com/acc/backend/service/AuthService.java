package com.acc.backend.service;

import com.acc.backend.domain.dto.req.ReqLogin;
import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.ResLogin;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    BaseResponse<ResLogin> login(ReqLogin req, HttpServletRequest request);
    BaseResponse<String> logout(String bearerToken);
}