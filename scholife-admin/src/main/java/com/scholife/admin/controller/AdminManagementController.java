package com.scholife.admin.controller;

import com.scholife.admin.model.*;
import com.scholife.admin.security.CurrentAdmin;
import com.scholife.admin.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admins")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminUserService   adminService;
    private final ActivityLogService logService;
    private final CurrentAdmin       currentAdmin;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("admins",       adminService.getAll());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "admins/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("admin",        new AdminUser());
        model.addAttribute("roles",        AdminUser.AdminRole.values());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "admins/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute AdminUser admin,
                         @RequestParam String rawPassword,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        try {
            AdminUser saved = adminService.create(admin, rawPassword);
            AdminUser me = currentAdmin.get();
            if (me != null)
                logService.log(me, ActivityLog.LogCategory.SYSTEM,
                    "Created admin: " + saved.getFullName() + " (" + saved.getRole() + ")",
                    "Admin#" + saved.getId(),
                    request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
            ra.addFlashAttribute("success", "Admin account created for " + saved.getFullName());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admins/new";
        }
        return "redirect:/admins";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("admin",        adminService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Admin not found")));
        model.addAttribute("roles",        AdminUser.AdminRole.values());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "admins/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute AdminUser updated,
                         @RequestParam(required = false) String rawPassword,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        AdminUser admin = adminService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        admin.setFirstName(updated.getFirstName());
        admin.setLastName(updated.getLastName());
        admin.setEmail(updated.getEmail());
        admin.setRole(updated.getRole());
        admin.setEmployeeNumber(updated.getEmployeeNumber());
        if (rawPassword != null && !rawPassword.isBlank())
            adminService.updatePassword(admin, rawPassword);
        else
            adminService.save(admin);
        AdminUser me = currentAdmin.get();
        if (me != null)
            logService.log(me, ActivityLog.LogCategory.SYSTEM,
                "Updated admin: " + admin.getFullName(),
                "Admin#" + id,
                request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        ra.addFlashAttribute("success", "Admin updated.");
        return "redirect:/admins";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        adminService.toggleStatus(id);
        adminService.findById(id).ifPresent(a -> {
            AdminUser me = currentAdmin.get();
            if (me != null)
                logService.log(me, ActivityLog.LogCategory.SYSTEM,
                    "Set admin " + a.getFullName() + " to " + a.getStatus(),
                    "Admin#" + id,
                    request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        });
        ra.addFlashAttribute("success", "Admin status toggled.");
        return "redirect:/admins";
    }
}
