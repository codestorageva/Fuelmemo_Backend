package com.example.FuelMemo.Security.jwt;

import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.AuthModule.Repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private final UserRepository userRepository;

    @Value("${JWT_SECRET:afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_EXPIRATION:9000000}")
    private long accessExpiration;

    @Value("${JWT_REFRESH_EXPIRATION:6048000000}")
    private long refreshExpiration;

    private Key signingKey;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // =====================================================
    // 🔐 GENERATE ACCESS TOKEN
    // =====================================================
    public String generateAccessToken(User user, String deviceId) {

        Map<String, Object> claims = new HashMap<>();

        // ✅ SHORT & CONSISTENT KEYS
        claims.put("uid", user.getUserId());

        UserCompany uc = user.getUserCompanies()
                .stream()
                .findFirst()
                .orElse(null);

        if (uc == null || uc.getRole() == null) {
            throw new RuntimeException("Role not found");
        }

        Role role = uc.getRole();
        String roleName = role.getRoleName();

        // ✅ SUPER ADMIN LOGIC
        if (!"ROLE_SUPERADMIN".equalsIgnoreCase(roleName)) {
            Integer companyId = uc.getCompany().getCompanyId();
            claims.put("cid", companyId);
        }

        claims.put("rol", roleName);
        claims.put("ver", user.getTokenVersion() != null ? user.getTokenVersion() : 0);
        claims.put("did", deviceId);

        return buildToken(claims, user.getEmail(), accessExpiration);
    }

    // =====================================================
    // 🔧 TOKEN BUILDER
    // =====================================================
    private String buildToken(Map<String, Object> claims,
                              String subject,
                              long expiration) {

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
    public String getLastActiveToken(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);

        if (user == null) {
            return null;
        }

        return user.getJwtToken();
    }

    // =====================================================
    // ✅ VALIDATION
    // =====================================================
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    // =====================================================
    // 🔍 EXTRACT METHODS (FIXED)
    // =====================================================

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("JWT token has expired");
        } catch (Exception ex) {
            throw new RuntimeException("Invalid JWT token");
        }
    }

    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("uid", Integer.class));
    }

    public Integer extractCompanyId(String token) {
        return extractClaim(token, claims -> claims.get("cid", Integer.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public Integer extractTokenVersion(String token) {
        return extractClaim(token, claims -> claims.get("ver", Integer.class));
    }

    public String extractDeviceId(String token) {
        return extractClaim(token, claims -> claims.get("did", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // =====================================================
    // 🔐 INTERNAL METHODS
    // =====================================================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration)
                .before(new Date());
    }



    // =====================================================
    // ❌ CUSTOM EXCEPTION
    // =====================================================
    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) {
            super(message);
        }
    }
}