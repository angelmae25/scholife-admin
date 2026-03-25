package com.scholife.admin.controller;

import com.scholife.admin.model.*;
import com.scholife.admin.security.CurrentAdmin;
import com.scholife.admin.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService orgService;
    private final StudentService      studentService;
    private final ActivityLogService  logService;
    private final CurrentAdmin        currentAdmin;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orgs",         orgService.getAll());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "organizations/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("org",          new Organization());
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "organizations/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Organization org,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        org.setStatus(Organization.OrgStatus.ACTIVE);
        Organization saved = orgService.save(org);
        AdminUser admin = currentAdmin.get();
        if (admin != null)
            logService.log(admin, ActivityLog.LogCategory.ORGANIZATIONS,
                "Created organization: " + saved.getName(),
                "Organization#" + saved.getId(),
                request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        ra.addFlashAttribute("success", "Organization created successfully.");
        return "redirect:/organizations/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Organization org = orgService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        List<Student> activeStudents = studentService.getByStatus(Student.StudentStatus.ACTIVE);
        List<Role>    roles          = orgService.getAllRoles();
        List<RoleAssignment> assignments = orgService.getAssignments(id);

        model.addAttribute("org",          org);
        model.addAttribute("assignments",  assignments);
        model.addAttribute("roles",        roles);
        model.addAttribute("students",     activeStudents);
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "organizations/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("org",          orgService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Not found")));
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "organizations/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute Organization updated,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        Organization org = orgService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Not found"));
        org.setName(updated.getName());
        org.setAcronym(updated.getAcronym());
        org.setType(updated.getType());
        org.setDescription(updated.getDescription());
        org.setAdviser(updated.getAdviser());
        org.setYearFounded(updated.getYearFounded());
        orgService.save(org);
        AdminUser admin = currentAdmin.get();
        if (admin != null)
            logService.log(admin, ActivityLog.LogCategory.ORGANIZATIONS,
                "Updated organization: " + org.getName(),
                "Organization#" + id,
                request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        ra.addFlashAttribute("success", "Organization updated.");
        return "redirect:/organizations/" + id;
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        orgService.toggleStatus(id);
        orgService.findById(id).ifPresent(o -> {
            AdminUser admin = currentAdmin.get();
            if (admin != null)
                logService.log(admin, ActivityLog.LogCategory.ORGANIZATIONS,
                    "Set " + o.getName() + " to " + o.getStatus(),
                    "Organization#" + id,
                    request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        });
        ra.addFlashAttribute("success", "Organization status updated.");
        return "redirect:/organizations/" + id;
    }

    @PostMapping("/{id}/appoint")
    public String appoint(@PathVariable Long id,
                          @RequestParam Long studentId,
                          @RequestParam Long roleId,
                          HttpServletRequest request,
                          RedirectAttributes ra) {
        AdminUser admin = currentAdmin.get();
        RoleAssignment ra2 = orgService.appoint(id, studentId, roleId, admin);
        if (admin != null)
            logService.log(admin, ActivityLog.LogCategory.ROLES,
                "Appointed " + ra2.getStudent().getFullName()
                    + " as " + ra2.getRole().getRoleName()
                    + " of " + ra2.getOrganization().getName(),
                "RoleAssignment#" + ra2.getId(),
                request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        ra.addFlashAttribute("success",
            ra2.getStudent().getFullName() + " appointed as " + ra2.getRole().getRoleName());
        return "redirect:/organizations/" + id;
    }

    @PostMapping("/assignments/{assignmentId}/remove")
    public String removeAssignment(@PathVariable Long assignmentId,
                                   @RequestParam Long orgId,
                                   HttpServletRequest request,
                                   RedirectAttributes ra) {
        AdminUser admin = currentAdmin.get();
        if (admin != null)
            logService.log(admin, ActivityLog.LogCategory.ROLES,
                "Removed role assignment #" + assignmentId,
                "RoleAssignment#" + assignmentId,
                request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        orgService.removeAssignment(assignmentId);
        ra.addFlashAttribute("success", "Officer removed.");
        return "redirect:/organizations/" + orgId;
    }
}
