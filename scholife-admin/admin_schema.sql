-- =============================================================================
-- admin_schema.sql
-- Run this AFTER Schoolifetrue_db.sql to add the admin dashboard tables.
-- mysql -u root -p Schoolifetrue_db < admin_schema.sql
-- =============================================================================

USE Schoolifetrue_db;

-- ─────────────────────────────────────────────────────────────────────────────
-- ADMIN USERS (web dashboard accounts)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS admin_users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    full_name   VARCHAR(160)  NOT NULL,
    email       VARCHAR(150)  NOT NULL,
    role        ENUM('SUPER_ADMIN','OSA','ADAA','ADAF','DO') NOT NULL DEFAULT 'OSA',
    status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    office      VARCHAR(100)  DEFAULT NULL,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    last_login  DATETIME      DEFAULT NULL
);

-- ─────────────────────────────────────────────────────────────────────────────
-- ORGANIZATIONS (created by admin dashboard)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS organizations (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    acronym     VARCHAR(20)   DEFAULT NULL,
    type        VARCHAR(80)   DEFAULT NULL,
    description TEXT          DEFAULT NULL,
    status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────────────────────────────────────
-- ORG ROLES (officer appointments)
-- Each row = one officer appointment in one org
-- role_name: President | Vice President | Secretary | Treasurer | Auditor | PIO
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS org_roles (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    organization_id INT          NOT NULL,
    student_id      INT          NOT NULL,
    role_name       VARCHAR(60)  NOT NULL,
    assigned_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    assigned_by     INT          DEFAULT NULL,   -- admin_users.id
    UNIQUE KEY uq_org_role (organization_id, role_name),
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)      REFERENCES students(id)      ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────────────────────────────────
-- ACTIVITY LOGS (admin audit trail)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS activity_logs (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    admin_id    INT          NOT NULL,
    admin_name  VARCHAR(160) NOT NULL,
    admin_role  VARCHAR(30)  DEFAULT NULL,
    action      VARCHAR(60)  NOT NULL,
    details     TEXT         DEFAULT NULL,
    target_type VARCHAR(50)  DEFAULT NULL,
    target_id   INT          DEFAULT NULL,
    ip_address  VARCHAR(50)  DEFAULT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id   (admin_id),
    INDEX idx_created_at (created_at),
    INDEX idx_action     (action)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- REPORTS (from mobile app Settings → Report a Problem)
-- Already referenced in Flask reports.py — just ensure it exists
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reports (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT          NOT NULL,
    subject     VARCHAR(200) NOT NULL,
    message     TEXT         NOT NULL,
    status      ENUM('OPEN','IN_PROGRESS','RESOLVED') NOT NULL DEFAULT 'OPEN',
    admin_reply TEXT         DEFAULT NULL,
    replied_at  DATETIME     DEFAULT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────────────────────────────────────
-- EVENT ATTENDANCE (if not already created by Flask db.create_all())
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS event_attendance (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    event_id    BIGINT NOT NULL,
    student_id  INT    NOT NULL,
    attended_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_event_student (event_id, student_id),
    FOREIGN KEY (event_id)   REFERENCES events(id)   ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────────────────────────────────────────
-- FCM TOKEN column on students (if not already added by Flask)
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE students
    ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(500) DEFAULT NULL;

-- ─────────────────────────────────────────────────────────────────────────────
-- SEED: default super admin  (password: Admin@2024)
-- Change password immediately after first login!
-- Generated with BCrypt rounds=10
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO admin (username, password, full_name, email, role, office)
VALUES (
    'superadmin',
    '$2a$10$N.zmdr9zkzoGtM.w3EeGq.bkJj/vqPr9jXiWjVtHWKoVLBlbXE/Vu',  -- Admin@2024
    'Super Administrator',
    'superadmin@scholife.edu',
    'SUPER_ADMIN',
    'Main Office'
);

-- ─────────────────────────────────────────────────────────────────────────────
-- SEED: sample organizations
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO organizations (name, acronym, type, description) VALUES
    ('Computer Science Society',    'CSS',   'Academic', 'The official organization for CS students.'),
    ('Student Government Association','SGA', 'Political','Represents all students in university governance.'),
    ('Campus Ministry',             'CM',    'Religious','Faith-based community for all denominations.'),
    ('University Athletic Association','UAA','Sports',   'Coordinates all sports programs and intramurals.');
