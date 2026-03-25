package com.scholife.admin.security;

import com.scholife.admin.model.AdminUser;
import com.scholife.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAdmin {

    private final AdminUserRepository adminRepo;

    public AdminUser get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return adminRepo.findByEmail(auth.getName()).orElse(null);
    }
}
