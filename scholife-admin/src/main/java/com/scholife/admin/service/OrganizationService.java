package com.scholife.admin.service;

import com.scholife.admin.model.*;
import com.scholife.admin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository orgRepo;
    private final RoleAssignmentRepository assignmentRepo;
    private final RoleRepository roleRepo;
    private final StudentRepository studentRepo;

    // =========================
    // ORGANIZATION BASIC METHODS
    // =========================

    public List<Organization> getAll() {
        return orgRepo.findAllByOrderByNameAsc();
    }

    public Optional<Organization> findById(Long id) {
        return orgRepo.findById(id);
    }

    public Organization save(Organization org) {
        return orgRepo.save(org);
    }

    public long countAll() {
        return orgRepo.count();
    }

    public void toggleStatus(Long id) {
        orgRepo.findById(id).ifPresent(o -> {
            o.setStatus(o.getStatus() == Organization.OrgStatus.ACTIVE
                    ? Organization.OrgStatus.INACTIVE
                    : Organization.OrgStatus.ACTIVE);
            orgRepo.save(o);
        });
    }

    // =========================
    // ROLE ASSIGNMENTS
    // =========================

    public List<RoleAssignment> getAssignments(Long orgId) {
        return assignmentRepo.findByOrganizationId(orgId);
    }

    public List<RoleAssignment> getAssignmentsForStudent(Long studentId) {
        return assignmentRepo.findByStudentId(studentId);
    }

    // Used by Flutter Mobile API
    public List<RoleAssignment> getAssignmentsByStudentId(String studentId) {
        return assignmentRepo.findByStudentStudentId(studentId);
    }

    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    // =========================
    // APPOINT OFFICER (USED BY MODAL)
    // =========================

    @Transactional
    public RoleAssignment appoint(Long orgId, Long studentId, Long roleId, AdminUser assignedBy) {

        Organization org = orgRepo.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        // Ensure only one officer per role in the organization
        assignmentRepo.findByOrganizationIdAndRoleId(orgId, roleId)
                .ifPresent(assignmentRepo::delete);

        RoleAssignment assignment = new RoleAssignment();
        assignment.setOrganization(org);
        assignment.setStudent(student);
        assignment.setRole(role);
        assignment.setAssignedBy(assignedBy);

        return assignmentRepo.save(assignment);
    }

    // =========================
    // ASSIGN ROLE BY ROLE NAME
    // =========================

    @Transactional
    public RoleAssignment assignRole(Long orgId, Long studentId, String roleName) {

        Organization org = orgRepo.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Role role = roleRepo.findByRoleNameIgnoreCase(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(roleName);
                    return roleRepo.save(newRole);
                });

        assignmentRepo.findByOrganizationIdAndRoleId(orgId, role.getId())
                .ifPresent(assignmentRepo::delete);

        RoleAssignment assignment = new RoleAssignment();
        assignment.setOrganization(org);
        assignment.setStudent(student);
        assignment.setRole(role);

        return assignmentRepo.save(assignment);
    }

    // =========================
    // REMOVE ROLE ASSIGNMENT
    // =========================

    @Transactional
    public Long removeAssignment(Long assignmentId) {

        RoleAssignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        Long orgId = assignment.getOrganization().getId();

        assignmentRepo.delete(assignment);

        return orgId;
    }
}