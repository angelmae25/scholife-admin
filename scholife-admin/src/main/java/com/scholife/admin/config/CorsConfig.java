package com.scholife.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration for the Spring Boot admin API.
 *
 * WHY THIS IS NEEDED:
 * Flutter Web (running in Edge/Chrome) enforces browser CORS policy.
 * Without this, calls from Flutter to /api/org-post/* will be BLOCKED
 * with "Access to XMLHttpRequest has been blocked by CORS policy".
 *
 * PLACEMENT: src/main/java/com/scholife/admin/config/CorsConfig.java
 *
 * This allows:
 *  - Flutter web app (any origin during dev, lock down in production)
 *  - Flutter mobile calls from http://192.168.x.x (local network)
 *  - All standard HTTP methods and headers
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ── Allowed origins ───────────────────────────────────────────────────
        // During development: allow everything.
        // For production, replace with your actual deployed domain(s):
        //   config.setAllowedOrigins(List.of("https://your-app.com"));
        config.setAllowedOriginPatterns(List.of("*"));

        // ── Allowed methods ───────────────────────────────────────────────────
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ── Allowed headers ───────────────────────────────────────────────────
        config.setAllowedHeaders(List.of("*"));

        // ── Expose headers Flutter may need to read ───────────────────────────
        config.setExposedHeaders(List.of("Content-Type", "Authorization"));

        // ── Allow credentials (cookies / auth headers) ────────────────────────
        // NOTE: cannot be true when allowedOriginPatterns = "*" in production.
        // Disable this if you don't use session cookies.
        config.setAllowCredentials(false);

        // ── Preflight cache (seconds) ─────────────────────────────────────────
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to ALL endpoints — including /api/org-post/**
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}