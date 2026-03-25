package com.scholife.admin.service;

import com.scholife.admin.model.ActivityLog;
import com.scholife.admin.model.AdminUser;
import com.scholife.admin.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository logRepo;

    public void log(AdminUser admin, ActivityLog.LogCategory category,
                    String event, String entity, String ip,
                    ActivityLog.LogStatus status) {
        ActivityLog log = new ActivityLog();
        log.setAdmin(admin);
        log.setCategory(category);
        log.setEvent(event);
        log.setEntity(entity);
        log.setIpAddress(ip);
        log.setStatus(status);
        logRepo.save(log);
    }

    public Page<ActivityLog> getAll(int page, int size) {
        return logRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public Page<ActivityLog> search(String q, int page, int size) {
        return logRepo.findByEventContainingIgnoreCaseOrEntityContainingIgnoreCase(
                q, q, PageRequest.of(page, size));
    }

    public Page<ActivityLog> getByAdmin(Long adminId, int page, int size) {
        return logRepo.findByAdminIdOrderByCreatedAtDesc(adminId, PageRequest.of(page, size));
    }
}
