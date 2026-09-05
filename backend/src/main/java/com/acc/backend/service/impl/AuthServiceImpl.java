package com.acc.backend.service.impl;

import com.acc.backend.config.JwtUtil;
import com.acc.backend.domain.dto.req.ReqLogin;
import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.ResLogin;
import com.acc.backend.domain.entity.LogBlacklistToken;
import com.acc.backend.domain.entity.LogLogin;
import com.acc.backend.domain.entity.MasterUser;
import com.acc.backend.exception.UnauthorizedException;
import com.acc.backend.repository.BlacklistTokenRepository;
import com.acc.backend.repository.LogLoginRepository;
import com.acc.backend.repository.UserRepository;
import com.acc.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LogLoginRepository logLoginRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, LogLoginRepository logLoginRepository, BlacklistTokenRepository blacklistTokenRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.logLoginRepository = logLoginRepository;
        this.blacklistTokenRepository = blacklistTokenRepository;
    }

    @Override
    public BaseResponse<ResLogin> login(ReqLogin req, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // 1. Cari user berdasarkan NPK (otomatis simpan log jika NPK/User Aktif tidak ditemukan)
        MasterUser user = userRepository.findByNpkAndIsDeletedFalseAndIsActiveTrue(req.getNpk())
                .orElseThrow(() -> {
                    saveLog(req.getNpk(), null, ipAddress, userAgent, "Gagal", "NPK tidak ditemukan atau akun tidak aktif");
                    return new UnauthorizedException("Npk atau password tidak sesuai");
                });

        // 2. Cek password (otomatis simpan log jika password salah)
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            saveLog(req.getNpk(), user, ipAddress, userAgent, "Gagal", "Password salah");
            throw new UnauthorizedException("Npk atau password tidak sesuai");
        }

        if (user.getActiveToken() != null && !user.getActiveToken().isEmpty()) {
            try {
                Date oldExpiry = jwtUtil.extractExpiration(user.getActiveToken());

                // Simpan token lama ke blacklist jika belum di-blacklist
                if (!blacklistTokenRepository.existsByToken(user.getActiveToken())) {
                    LogBlacklistToken forceLogoutToken = LogBlacklistToken.builder()
                            .token(user.getActiveToken())
                            .expiryDate(oldExpiry)
                            .createdBy("SYSTEM_FORCE_LOGOUT_NEW_DEVICE")
                            .build();
                    blacklistTokenRepository.save(forceLogoutToken);
                }
            } catch (Exception e) {
                // Jika token lama sudah expired/invalid saat diparsing, abaikan
            }
        }

        // 3. Generate Token & Save SUCCESS Log
        String token = jwtUtil.generateToken(user);
        user.setActiveToken(token);
        userRepository.save(user);

        saveLog(req.getNpk(), user, ipAddress, userAgent, "Berhasil", null);

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

    @Override
    public BaseResponse<String> logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            // 1. Cek apakah token sudah di-blacklist sebelumnya
            if (!blacklistTokenRepository.existsByToken(token)) {
                Date expiryDate = jwtUtil.extractExpiration(token);

                // 2. Simpan token ke database blacklist
                LogBlacklistToken blacklist = LogBlacklistToken.builder()
                        .token(token)
                        .expiryDate(expiryDate)
                        .createdBy("LOGOUT_USER")
                        .build();

                blacklistTokenRepository.save(blacklist);
            }
        }

        return BaseResponse.ok("Logout berhasil", "Token berhasil dinonaktifkan di server");
    }

    private void saveLog(String npk, MasterUser user, String ipAddress, String userAgent, String status, String failureReason) {
        LogLogin log = LogLogin.builder()
                .npk(npk)
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .failureReason(failureReason)
                .isDeleted(false)
                .createdBy(npk != null ? npk : "SYSTEM")
                .build();
        logLoginRepository.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
