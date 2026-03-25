package com.scholife.admin.controller;

import com.scholife.admin.model.ActivityLog;
import com.scholife.admin.security.CurrentAdmin;
import com.scholife.admin.service.ActivityLogService;
import com.scholife.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService logService;
    private final AdminUserService   adminService;
    private final CurrentAdmin       currentAdmin;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "30") int size,
                       @RequestParam(required = false)    String q,
                       @RequestParam(required = false)    Long adminId,
                       Model model) {
        Page<ActivityLog> logs;
        if (q != null && !q.isBlank())
            logs = logService.search(q.trim(), page, size);
        else if (adminId != null)
            logs = logService.getByAdmin(adminId, page, size);
        else
            logs = logService.getAll(page, size);

        model.addAttribute("logs",         logs);
        model.addAttribute("q",            q);
        model.addAttribute("adminId",      adminId);
        model.addAttribute("admins",       adminService.getAll());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "logs/list";
    }
}
