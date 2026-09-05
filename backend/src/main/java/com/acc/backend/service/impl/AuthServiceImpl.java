package com.acc.backend.service.impl;

import com.acc.backend.config.JwtUtil;
import com.acc.backend.domain.dto.req.ReqLogin;
import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.ResLogin;
import com.acc.backend.domain.entity.MasterUser;
import com.acc.backend.exception.UnauthorizedException;
import com.acc.backend.repository.UserRepository;
import com.acc.backend.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public BaseResponse<ResLogin> login(ReqLogin req) {
        // 1. Cari user berdasarkan NPK
        MasterUser user = userRepository.findByNpkAndIsDeletedFalse(req.getNpk())
                .orElseThrow(() -> new UnauthorizedException("NPK tidak terdaftar"));

        // 2. Cek password (plain text check untuk simulasi / ganti BCryptPasswordEncoder jika pakai Spring Security)
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Password yang Anda masukkan salah");
        }

        // 3. Cek status aktif
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Akun Anda sedang tidak aktif");
        }

        String token = jwtUtil.generateToken(user);

        // 4. Build Response
        ResLogin res = ResLogin.builder()
                .userId(user.getId())
                .npk(user.getNpk())
                .fullName(user.getFullName())
                .roleCode(user.getRole().getRoleCode())
                .roleName(user.getRole().getRoleName())
                .branchName(user.getBranch() != null ? user.getBranch().getBranchName() : null)
                .token(token)
                .build();

        return BaseResponse.ok("Login berhasil", res);
    }
}
