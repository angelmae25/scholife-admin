package com.scholife.admin.security;

import com.scholife.admin.model.AdminUser;
import com.scholife.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminUserRepository adminRepo;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            AdminUser admin = adminRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + email));
            if (admin.getStatus() == AdminUser.AdminStatus.INACTIVE)
                throw new UsernameNotFoundException("Account deactivated.");
            admin.setLastLogin(LocalDateTime.now());
            adminRepo.save(admin);
            return User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())))
                    .build();
        };
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService());
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authProvider())
                .authorizeHttpRequests(auth -> auth
                        // ── Static assets ────────────────────────────────────────────
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // ── Auth pages ───────────────────────────────────────────────
                        .requestMatchers("/login", "/setup").permitAll()

                        // ── Flutter mobile API — open to the local network ───────────
                        // FIX: This was missing. Without it, every Flutter call to
                        // /api/org-post/** gets a 302 redirect to /login instead of
                        // the actual data. Flutter doesn't follow session-based
                        // redirects — it just gets a 302 response and crashes.
                        .requestMatchers("/api/org-post/**").permitAll()

                        // ── Admin-only pages ─────────────────────────────────────────
                        .requestMatchers("/admins/**").hasRole("SUPER_ADMIN")

                        // ── Everything else requires an admin session ────────────────
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // ── Disable CSRF for the Flutter REST API ────────────────────────
                // CSRF protection is form-based and doesn't apply to mobile REST
                // clients. Without this, POST from Flutter would also get a 403.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                );

        return http.build();
    }
}