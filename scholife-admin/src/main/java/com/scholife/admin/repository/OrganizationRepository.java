package com.scholife.admin.repository;

import com.scholife.admin.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findAllByOrderByNameAsc();
    List<Organization> findByStatus(Organization.OrgStatus status);
}
