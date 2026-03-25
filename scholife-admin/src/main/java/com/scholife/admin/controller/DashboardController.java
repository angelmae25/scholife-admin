package com.scholife.admin.controller;

import com.scholife.admin.repository.ActivityLogRepository;
import com.scholife.admin.security.CurrentAdmin;
import com.scholife.admin.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StudentService      studentService;
    private final OrganizationService orgService;
    private final AdminUserService    adminService;
    private final ActivityLogRepository logRepo;
    private final CurrentAdmin        currentAdmin;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("currentAdmin",   currentAdmin.get());
        model.addAttribute("totalStudents",  studentService.countAll());
        model.addAttribute("activeStudents", studentService.countActive());
        model.addAttribute("pendingStudents",studentService.countPending());
        model.addAttribute("totalOrgs",      orgService.countAll());
        model.addAttribute("totalAdmins",    adminService.count());
        model.addAttribute("recentLogs",     logRepo.findTop50ByOrderByCreatedAtDesc());
        return "dashboard";
    }
}
