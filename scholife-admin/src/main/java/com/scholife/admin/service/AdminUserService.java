package com.scholife.admin.service;

import com.scholife.admin.model.AdminUser;
import com.scholife.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUser> getAll() { return adminRepo.findAllByOrderByCreatedAtDesc(); }
    public Optional<AdminUser> findById(Long id) { return adminRepo.findById(id); }
    public Optional<AdminUser> findByEmail(String email) { return adminRepo.findByEmail(email); }
    public long count() { return adminRepo.count(); }
    public boolean existsAny() { return adminRepo.count() > 0; }

    public AdminUser create(AdminUser admin, String rawPassword) {
        if (adminRepo.existsByEmail(admin.getEmail()))
            throw new IllegalArgumentException("Email already in use: " + admin.getEmail());
        admin.setPassword(passwordEncoder.encode(rawPassword));
        return adminRepo.save(admin);
    }

    public AdminUser save(AdminUser admin) { return adminRepo.save(admin); }

    public void updatePassword(AdminUser admin, String rawPassword) {
        admin.setPassword(passwordEncoder.encode(rawPassword));
        adminRepo.save(admin);
    }

    public void toggleStatus(Long id) {
        adminRepo.findById(id).ifPresent(a -> {
            a.setStatus(a.getStatus() == AdminUser.AdminStatus.ACTIVE
                    ? AdminUser.AdminStatus.INACTIVE : AdminUser.AdminStatus.ACTIVE);
            adminRepo.save(a);
        });
    }
}
