package com.scholife.admin.controller;

import com.scholife.admin.model.AdminUser;
import com.scholife.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AdminUserService adminService;

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String expired,
                        Model model) {
        if (error   != null) model.addAttribute("error",   "Invalid email or password.");
        if (logout  != null) model.addAttribute("message", "Logged out successfully.");
        if (expired != null) model.addAttribute("error",   "Session expired. Please log in again.");
        return "login";
    }

    @GetMapping("/setup")
    public String setupForm(Model model) {
        if (adminService.existsAny()) return "redirect:/login";
        model.addAttribute("admin", new AdminUser());
        return "setup";
    }

    @PostMapping("/setup")
    public String setup(@ModelAttribute AdminUser admin,
                        @RequestParam String rawPassword,
                        RedirectAttributes ra) {
        if (adminService.existsAny()) return "redirect:/login";
        admin.setRole(AdminUser.AdminRole.SUPER_ADMIN);
        admin.setStatus(AdminUser.AdminStatus.ACTIVE);
        adminService.create(admin, rawPassword);
        ra.addFlashAttribute("message", "Super Admin created. Please log in.");
        return "redirect:/login";
    }
}