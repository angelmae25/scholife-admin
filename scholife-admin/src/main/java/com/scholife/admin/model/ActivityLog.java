package com.scholife.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Maps to the real `activity_log` table (note: singular, not plural).
 */
@Entity
@Table(name = "activity_log")
@Data
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column
    private LogCategory category;

    @Column
    private String entity;

    @Column
    private String event;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column
    private LogStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id")
    private AdminUser admin;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum LogCategory {
        MESSAGES, ORGANIZATIONS, ROLES, STUDENTS, SYSTEM
    }

    public enum LogStatus {
        FAILED, PENDING, SUCCESS
    }
}
