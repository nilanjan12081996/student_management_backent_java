package skyware.product.config;




import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import skyware.product.config.ratelimit.middleware.RateLimitFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    // Remove UserDetailsService dependency - not needed!

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/skyware/api/company/list",
                                "/skyware/api/company/all",
                                "/skyware/api/company",
                                "/skyware/api/company/toggle-status",
                                "/skyware/api/company/toggle-status/**",
                                "/skyware/api/company/status/**",
                                "/skyware/api/company/*/toggle-status",
                                "/skyware/api/teacher/list",
                                "/skyware/api/teacher/all",
                                "/skyware/api/teacher",
                                "/skyware/api/teacher/toggle-status",
                                "/skyware/api/teacher/toggle-status/**",
                                "/skyware/api/teacher/status/**",
                                "/skyware/api/teacher/*/toggle-status",
                                "/skyware/api/teacher/map-specialisation",
                                "/skyware/api/teacher/specialisation/**",
                                "/skyware/api/teacher-specialisation/**",
                                "/skyware/api/teacher/map-company",
                                "/skyware/api/teacher/company/**",
                                "/skyware/api/teacher-company/**",
                                "/skyware/api/specialisation/**"
                        ).hasAnyRole("SUPERADMIN", "ADMIN")
                        .requestMatchers(
                                "/public/**",
                                "/static/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/uploads/**",
                                "/public/uploads/**",
                                "/actuator/**",
                                "/error",
                                "/api/auth/**",
                                "/skyware/api/superadmin/**",
                                "/skyware/api/company/**",
                                "/skyware/api/teacher/**"// Basic auth endpoints (login, register)
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/superadmin/**").hasRole("SUPERADMIN")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            Map<String, Object> responseBody = new HashMap<>();
                            responseBody.put("status", false);
                            responseBody.put("statusCode", 401);
                            responseBody.put("message", "Unauthorized: Valid Super Admin token is required.");
                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(responseBody));
                        })
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Remove authenticationProvider - not needed for JWT-only validation
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Remove authenticationProvider() bean
    // Remove authenticationManager() bean

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH","DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean("loginTaskExecutor")
    public ThreadPoolTaskExecutor loginTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("otp-email-");
        executor.initialize();
        return executor;
    }

}
