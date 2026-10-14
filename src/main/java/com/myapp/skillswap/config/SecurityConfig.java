package com.myapp.skillswap.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Value("${app.security.csrf.enabled:false}")
    private boolean csrfEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<String> patterns = new java.util.ArrayList<>(origins);
        patterns.add("https://*.vercel.app");
        patterns.add("https://vercel.app");
        patterns.add("https://*.onrender.com");
        patterns.add("http://localhost:[*]");
        patterns.add("http://127.0.0.1:[*]");

        configuration.setAllowedOriginPatterns(patterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                            throws ServletException, IOException {
                        String uri = request.getRequestURI();
                        if (uri.endsWith("/api/auth/register") || uri.endsWith("/api/auth/login")) {
                            HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
                                @Override
                                public String getHeader(String name) {
                                    if ("authorization".equalsIgnoreCase(name)) {
                                        return null;
                                    }
                                    return super.getHeader(name);
                                }

                                @Override
                                public Enumeration<String> getHeaders(String name) {
                                    if ("authorization".equalsIgnoreCase(name)) {
                                        return Collections.emptyEnumeration();
                                    }
                                    return super.getHeaders(name);
                                }
                            };
                            filterChain.doFilter(wrapped, response);
                            return;
                        }
                        filterChain.doFilter(request, response);
                    }
                }, BasicAuthenticationFilter.class)
                .cors(Customizer.withDefaults());

        if (csrfEnabled) {
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(request -> {
                        String auth = request.getHeader("Authorization");
                        return auth != null && auth.startsWith("Basic ");
                    })
            );
        } else {
            http.csrf(AbstractHttpConfigurer::disable);
        }

        http
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/skills", "/api/skills/{id}").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/{userId}/profile", "/api/users/{userId}/skills").permitAll()
                        .requestMatchers("/api/skills/matches").authenticated()
                        .requestMatchers("/api/skills/offered").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()
                        .requestMatchers("/api/swaps/**").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            String message = (authException != null && authException.getMessage() != null)
                                    ? authException.getMessage()
                                    : "Full authentication is required to access this resource";
                            response.getWriter().write(String.format(
                                    "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                                    LocalDateTime.now(),
                                    message.replace("\"", "\\\"")
                            ));
                        })
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            String message = (authException != null && authException.getMessage() != null)
                                    ? authException.getMessage()
                                    : "Full authentication is required to access this resource";
                            response.getWriter().write(String.format(
                                    "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}",
                                    LocalDateTime.now(),
                                    message.replace("\"", "\\\"")
                            ));
                        })
                );

        return http.build();
    }
}
