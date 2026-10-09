package skyware.product.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Check for Token Presence
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            // 2. Parse Claims
            Claims claims = jwtUtil.extractAllClaims(jwt);

            String username = claims.getSubject();
            if (username == null) username = claims.get("email", String.class);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 3. Robust Data Extraction (Handle nulls safely)
                Long userId = extractUserId(claims);
                String email = claims.get("email", String.class);

                // Handle Name variations (Organization has 'name', Users have 'firstname')
                String name = claims.get("name", String.class);
                String firstname = claims.get("firstname", String.class);
                String lastname = claims.get("lastname", String.class);

                if (firstname == null) firstname = name; // Fallback
                if (lastname == null) lastname = "";    // Avoid null

                // 4. Extract Roles (Universal Handler)
                List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
                String role = authorities.isEmpty() ? "USER" : authorities.get(0).getAuthority().replace("ROLE_", "");

                // 5. Build UserDetails
                JwtUserDetails userDetails = new JwtUserDetails(
                        userId,
                        email,
                        role,
                        firstname,
                        lastname,
                        authorities
                );

                // 6. Set Security Context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            // Do NOT throw exception here, just let the request proceed without auth
            // The SecurityConfig will handle the 403 if auth was required.
        }

        filterChain.doFilter(request, response);
    }

    // Helper to safely get ID as Long
    private Long extractUserId(Claims claims) {
        Object idObj = claims.get("id");
        if (idObj instanceof Number) return ((Number) idObj).longValue();
        if (idObj instanceof String) return Long.parseLong((String) idObj);
        return null;
    }

    // Helper to standardise Role extraction
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Check all possible keys where role might be stored
        Object roleObj = claims.get("role");
        if (roleObj == null) roleObj = claims.get("roles");
        if (roleObj == null) roleObj = claims.get("authorities");

        if (roleObj != null) {
            if (roleObj instanceof String) {
                // Ensure it starts with ROLE_
                String r = (String) roleObj;
                if (!r.startsWith("ROLE_")) r = "ROLE_" + r;
                authorities.add(new SimpleGrantedAuthority(r.toUpperCase()));
            } else if (roleObj instanceof List) {
                List<?> list = (List<?>) roleObj;
                for (Object o : list) {
                    String r = o.toString();
                    if (!r.startsWith("ROLE_")) r = "ROLE_" + r;
                    authorities.add(new SimpleGrantedAuthority(r.toUpperCase()));
                }
            }
        }
        return authorities;
    }
}
