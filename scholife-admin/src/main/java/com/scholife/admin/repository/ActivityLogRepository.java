package com.scholife.admin.repository;

import com.scholife.admin.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();
    Page<ActivityLog> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);
    Page<ActivityLog> findByEventContainingIgnoreCaseOrEntityContainingIgnoreCase(
            String event, String entity, Pageable pageable);
}
