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

    private final OrganizationRepository   orgRepo;
    private final RoleAssignmentRepository assignmentRepo;
    private final RoleRepository           roleRepo;
    private final StudentRepository        studentRepo;

    public List<Organization> getAll()               { return orgRepo.findAllByOrderByNameAsc(); }
    public Optional<Organization> findById(Long id)  { return orgRepo.findById(id); }
    public Organization save(Organization org)       { return orgRepo.save(org); }
    public long countAll()                           { return orgRepo.count(); }

    public void toggleStatus(Long id) {
        orgRepo.findById(id).ifPresent(o -> {
            o.setStatus(o.getStatus() == Organization.OrgStatus.ACTIVE
                    ? Organization.OrgStatus.INACTIVE : Organization.OrgStatus.ACTIVE);
            orgRepo.save(o);
        });
    }

    public List<RoleAssignment> getAssignments(Long orgId) {
        return assignmentRepo.findByOrganizationId(orgId);
    }

    public List<RoleAssignment> getAssignmentsForStudent(Long studentId) {
        return assignmentRepo.findByStudentId(studentId);
    }

    // Used by mobile OrgPostApi
    public List<RoleAssignment> getAssignmentsByStudentId(String studentId) {
        return assignmentRepo.findByStudentStudentId(studentId);
    }

    public List<Role> getAllRoles() { return roleRepo.findAll(); }

    @Transactional
    public RoleAssignment appoint(Long orgId, Long studentId, Long roleId, AdminUser assignedBy) {
        Organization org     = orgRepo.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Org not found"));
        Student      student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Role         role    = roleRepo.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        // Remove existing holder of this role in this org
        assignmentRepo.findByOrganizationIdAndRoleId(orgId, roleId)
                .ifPresent(assignmentRepo::delete);

        RoleAssignment ra = new RoleAssignment();
        ra.setOrganization(org);
        ra.setStudent(student);
        ra.setRole(role);
        ra.setAssignedBy(assignedBy);
        return assignmentRepo.save(ra);
    }

    @Transactional
    public void removeAssignment(Long assignmentId) {
        assignmentRepo.deleteById(assignmentId);
    }
}
