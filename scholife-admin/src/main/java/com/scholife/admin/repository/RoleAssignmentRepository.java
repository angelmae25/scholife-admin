package com.scholife.admin.repository;

import com.scholife.admin.model.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {
    List<RoleAssignment> findByOrganizationId(Long orgId);
    List<RoleAssignment> findByStudentId(Long studentId);
    Optional<RoleAssignment> findByOrganizationIdAndRoleId(Long orgId, Long roleId);
    List<RoleAssignment> findByStudentStudentId(String studentId);
}
