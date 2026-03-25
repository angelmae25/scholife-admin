package com.scholife.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Maps to the real `students` table in schoolifetrue_db.
 */
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 100)
    private String course;

    @Column(name = "year_level")
    private String yearLevel;

    @Column
    private String contact;

    @Column(name = "avatar_url", columnDefinition = "LONGTEXT")
    private String avatarUrl;

    @Column(nullable = false)
    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status = StudentStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    // Extra columns that exist in the real DB (from Spring Boot side)
    @Column
    private String address;

    @Column
    private String program;

    @Column(name = "student_number")
    private String studentNumber;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum StudentStatus {
        PENDING, ACTIVE, INACTIVE
    }
}
