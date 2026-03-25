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
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService      studentService;
    private final OrganizationService orgService;
    private final ActivityLogService  logService;
    private final CurrentAdmin        currentAdmin;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String status,
                       Model model) {
        List<Student> students;
        if (q != null && !q.isBlank())
            students = studentService.search(q.trim());
        else if (status != null && !status.isBlank())
            students = studentService.getByStatus(Student.StudentStatus.valueOf(status.toUpperCase()));
        else
            students = studentService.getAll();

        model.addAttribute("students",     students);
        model.addAttribute("q",            q);
        model.addAttribute("statusFilter", status);
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "students/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Student student = studentService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        model.addAttribute("student",      student);
        model.addAttribute("assignments",  orgService.getAssignmentsForStudent(id));
        model.addAttribute("currentAdmin", currentAdmin.get());
        return "students/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        Student.StudentStatus newStatus = Student.StudentStatus.valueOf(status.toUpperCase());
        studentService.setStatus(id, newStatus);
        studentService.findById(id).ifPresent(s -> {
            AdminUser admin = currentAdmin.get();
            if (admin != null)
                logService.log(admin, ActivityLog.LogCategory.STUDENTS,
                    "Set " + s.getFullName() + " to " + newStatus,
                    "Student#" + id, request.getRemoteAddr(), ActivityLog.LogStatus.SUCCESS);
        });
        ra.addFlashAttribute("success", "Status updated to " + newStatus);
        return "redirect:/students/" + id;
    }
}
