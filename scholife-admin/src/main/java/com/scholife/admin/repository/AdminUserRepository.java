package com.scholife.admin.repository;

import com.scholife.admin.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AdminUser> findAllByOrderByCreatedAtDesc();
    List<AdminUser> findByStatus(AdminUser.AdminStatus status);
}
