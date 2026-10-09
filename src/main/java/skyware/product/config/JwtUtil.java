package skyware.product.config;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import skyware.product.skywareapi.superadmin.dto.SuperAdminDto;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    public String generateToken(String email, String role, Long id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        claims.put("email", email);
        return createToken(claims, email);
    }
    public String generateSuperAdminToken(SuperAdminDto superAdminDto){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", superAdminDto.getId());
        claims.put("role", "SUPERADMIN");
        claims.put("email", superAdminDto.getEmail());
        claims.put("mobile",superAdminDto.getMobile());
        claims.put("fullname",superAdminDto.getFullName());

        return createToken(claims, superAdminDto.getEmail());
    }
    public String generateFormToken(Long formId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("formId", formId);
        claims.put("type", "FORM_CSV_TEMPLATE");
        return createToken(claims, "form_" + formId);
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
