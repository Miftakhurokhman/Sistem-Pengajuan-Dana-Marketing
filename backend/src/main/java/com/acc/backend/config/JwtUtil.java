package com.acc.backend.config;

import com.acc.backend.domain.entity.MasterUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // Secret Key minimal 256-bit (32+ karakter)
    private static final String SECRET = "SPD_BERIJALAN_ACC_SECRET_KEY_2026_VERY_SECURE_KEY";
    private static final long EXPIRATION_TIME = 86400000; // 24 Jam

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(MasterUser user) {
        Map<String, Object> claims = new HashMap<>();

        // Encode payload/claims
        claims.put("userId", user.getId());
        claims.put("npk", user.getNpk());
        claims.put("fullName", user.getFullName());
        claims.put("roleCode", user.getRole().getRoleCode());
        claims.put("roleName", user.getRole().getRoleName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getNpk())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}