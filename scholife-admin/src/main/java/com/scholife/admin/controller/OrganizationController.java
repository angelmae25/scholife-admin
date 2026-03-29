package com.scholife.admin.controller;

import com.scholife.admin.model.*;
import com.scholife.admin.security.CurrentAdmin;
import com.scholife.admin.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService orgService;
    private final StudentService studentService;
    private final ActivityLogService logService;
    private final CurrentAdmin currentAdmin;

    private static final String UPLOAD_DIR = "uploads/org-logos/";

    // =========================
    // LIST ORGANIZATIONS
    // =========================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("orgs", orgService.getAll());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "organizations/list";
    }

    // =========================
    // CREATE FORM
    // =========================
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("org", new Organization());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "organizations/form";
    }

    // =========================
    // CREATE ORGANIZATION
    // =========================
    @PostMapping("/new")
    public String create(@ModelAttribute Organization org,
                         @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                         HttpServletRequest request,
                         RedirectAttributes ra) {

        try {

            if (logoFile != null && !logoFile.isEmpty()) {

                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + logoFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                org.setLogo(fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        org.setStatus(Organization.OrgStatus.ACTIVE);

        Organization saved = orgService.save(org);

        AdminUser admin = currentAdmin.get();

        if (admin != null) {
            logService.log(
                    admin,
                    ActivityLog.LogCategory.ORGANIZATIONS,
                    "Created organization: " + saved.getName(),
                    "Organization#" + saved.getId(),
                    request.getRemoteAddr(),
                    ActivityLog.LogStatus.SUCCESS
            );
        }

        ra.addFlashAttribute("success", "Organization created successfully.");

        return "redirect:/organizations";
    }

    // =========================
    // ORGANIZATION DETAIL
    // =========================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Organization org = orgService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        List<Student> activeStudents =
                studentService.getByStatus(Student.StudentStatus.ACTIVE);

        List<Role> roles = orgService.getAllRoles();

        List<RoleAssignment> assignments =
                orgService.getAssignments(id);

        model.addAttribute("org", org);
        model.addAttribute("assignments", assignments);
        model.addAttribute("roles", roles);
        model.addAttribute("students", activeStudents);
        model.addAttribute("currentAdmin", currentAdmin.get());

        return "organizations/detail";
    }

    // =========================
    // APPOINT OFFICER (Modal)
    // =========================
    @PostMapping("/{id}/appoint")
    public String appoint(@PathVariable Long id,
                          @RequestParam Long studentId,
                          @RequestParam(required = false) Long roleId,
                          @RequestParam(required = false) String customRoleName,
                          RedirectAttributes ra) {

        // If admin typed a custom role name, create/find that role by name
        if (roleId == null || (customRoleName != null && !customRoleName.isBlank())) {
            orgService.assignRole(id, studentId, customRoleName.trim());
        } else {
            AdminUser admin = currentAdmin.get();
            orgService.appoint(id, studentId, roleId, admin);
        }

        ra.addFlashAttribute("success", "Officer appointed successfully.");
        return "redirect:/organizations/" + id;
    }

    // =========================
    // REMOVE OFFICER
    // =========================
    @PostMapping("/assignments/{aid}/remove")
    public String removeAssignment(@PathVariable Long aid,
                                   @RequestParam Long orgId,
                                   RedirectAttributes ra) {

        orgService.removeAssignment(aid);

        ra.addFlashAttribute("success", "Officer removed.");

        return "redirect:/organizations/" + orgId;
    }
}