package com.acc.backend.config;

import com.acc.backend.domain.entity.MasterUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String SECRET = "SPD_BERIJALAN_ACC_SECRET_KEY_2026_VERY_SECURE_KEY";
    private static final long EXPIRATION_TIME = 86400000; // 24 Jam

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(MasterUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("npk", user.getNpk());
        claims.put("fullName", user.getFullName());
        claims.put("roleCode", user.getRole() != null ? user.getRole().getRoleCode() : null);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getNpk())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractNpk(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String npk = extractNpk(token);
        return (npk.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}