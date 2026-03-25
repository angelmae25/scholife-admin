package com.scholife.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps to the real `organizations` table in schoolifetrue_db.
 */
@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String acronym;

    @Column
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String adviser;

    @Column(name = "year_founded")
    private Integer yearFounded;

    @Enumerated(EnumType.STRING)
    @Column
    private OrgStatus status = OrgStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoleAssignment> roleAssignments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum OrgStatus {
        ACTIVE, INACTIVE
    }
}
