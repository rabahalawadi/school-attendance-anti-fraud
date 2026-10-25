package com.school.attendance.config;

import com.school.attendance.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // For H2 Console
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/attendance/demo/**",
                                "/api/v1/students/import/template",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/h2-console/**"
                        ).permitAll()

                        // Admin Only Endpoints
                        .requestMatchers(
                                "/api/v1/academic-years/**",
                                "/api/v1/grades/**",
                                "/api/v1/sections/**",
                                "/api/v1/subjects/**",
                                "/api/v1/enrollments/**",
                                "/api/v1/verification/policies/**",
                                "/api/v1/devices/**",
                                "/api/v1/audit-logs/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/teachers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/teachers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/students/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/students/**").hasRole("ADMIN")

                        // Teacher / Admin Session & Verification Management
                        .requestMatchers(
                                "/api/v1/attendance/sessions/*/open",
                                "/api/v1/attendance/sessions/*/close",
                                "/api/v1/attendance/sessions/*/cancel",
                                "/api/v1/attendance/sessions/*/challenge",
                                "/api/v1/attendance/records/*/confirm",
                                "/api/v1/attendance/records/*/reject",
                                "/api/v1/offline-sync/**"
                        ).hasAnyRole("TEACHER", "ADMIN")

                        // Student Endpoints
                        .requestMatchers(
                                "/api/v1/attendance/register",
                                "/api/v1/attendance/my-records",
                                "/api/v1/attendance/records/*/correction-requests"
                        ).hasRole("STUDENT")

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
