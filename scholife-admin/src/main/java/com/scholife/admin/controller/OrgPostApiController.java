package com.scholife.admin.controller;

import com.scholife.admin.model.RoleAssignment;
import com.scholife.admin.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST endpoint consumed by Flutter OrgPostService on port 8080.
 * GET /api/org-post/my-organizations?studentId=2021-00001
 * Returns officer assignments so the mobile app shows the post FAB.
 */
@RestController
@RequestMapping("/api/org-post")
@RequiredArgsConstructor
public class OrgPostApiController {

    private final OrganizationService orgService;

    @GetMapping("/my-organizations")
    public ResponseEntity<List<Map<String, Object>>> getMyOrgs(
            @RequestParam String studentId) {

        List<RoleAssignment> assignments = orgService.getAssignmentsByStudentId(studentId);

        List<Map<String, Object>> result = assignments.stream().map(a -> Map.<String, Object>of(
            "assignmentId",     a.getId(),
            "organizationId",   a.getOrganization().getId(),
            "organizationName", a.getOrganization().getName(),
            "acronym",          a.getOrganization().getAcronym() != null
                                    ? a.getOrganization().getAcronym() : "",
            "roleName",         a.getRole().getRoleName()
        )).toList();

        return ResponseEntity.ok(result);
    }
}
