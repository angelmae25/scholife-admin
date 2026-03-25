# Scholife Admin Dashboard — Spring Boot

A full-featured web admin dashboard for the Scholife mobile app platform.  
Built with **Spring Boot 3.2**, **Thymeleaf**, **Spring Security**, **JPA/Hibernate** and **MySQL**.

---

## Features

| Feature | Description |
|---|---|
| **Student Registry** | View all students registered via the mobile app, search by name/ID, filter by status (Active / Pending / Inactive), activate or deactivate accounts |
| **Organizations** | Create organizations, edit details, toggle active/inactive status |
| **Officer Appointments** | Appoint students to 6 officer roles per org: President, Vice President, Secretary, Treasurer, Auditor, PIO |
| **Admin Accounts** | Super Admin can create/manage other admin accounts with roles: SUPER_ADMIN, OSA, ADAA, ADAF, DO |
| **Activity Logs** | Full audit trail of every admin action — searchable, filterable by admin, paginated |
| **Mobile API Bridge** | `GET /api/org-post/my-organizations?studentId=xxx` — consumed by Flutter OrgPostService on port 8080 |

---

## Project Structure

```
scholife-admin/
├── pom.xml
├── admin_schema.sql               ← Run this after Schoolifetrue_db.sql
├── src/main/java/com/scholife/admin/
│   ├── ScholifeAdminApplication.java
│   ├── config/                    (none needed — security in security/)
│   ├── controller/
│   │   ├── AuthController.java         ← /login, /setup
│   │   ├── DashboardController.java    ← /dashboard
│   │   ├── StudentController.java      ← /students
│   │   ├── OrganizationController.java ← /organizations
│   │   ├── AdminManagementController.java ← /admins (SUPER_ADMIN only)
│   │   ├── ActivityLogController.java  ← /logs
│   │   └── OrgPostApiController.java   ← /api/org-post (REST, for mobile)
│   ├── model/
│   │   ├── Student.java           ← mirrors Flask students table
│   │   ├── AdminUser.java         ← admin_users table
│   │   ├── Organization.java      ← organizations table
│   │   ├── OrgRole.java           ← org_roles table
│   │   └── ActivityLog.java       ← activity_logs table
│   ├── repository/                ← Spring Data JPA interfaces
│   ├── security/
│   │   ├── SecurityConfig.java    ← BCrypt, form login, role-based access
│   │   └── CurrentAdmin.java      ← helper to get logged-in admin
│   └── service/
│       ├── StudentService.java
│       ├── OrganizationService.java
│       ├── AdminUserService.java
│       └── ActivityLogService.java
└── src/main/resources/
    ├── application.properties
    ├── static/css/main.css        ← Scholife brand styles
    └── templates/
        ├── login.html
        ├── setup.html             ← first-time super admin creation
        ├── dashboard.html
        ├── fragments/layout.html  ← sidebar + head fragments
        ├── students/
        │   ├── list.html
        │   └── detail.html
        ├── organizations/
        │   ├── list.html
        │   ├── form.html
        │   └── detail.html        ← officer appointment UI
        ├── admins/
        │   ├── list.html
        │   └── form.html
        └── logs/
            └── list.html
```

---

## Setup Instructions

### Step 1 — MySQL

Run the two SQL files in order:

```bash
# 1. Main schema (if not already done from the Flask backend)
mysql -u root -p < Schoolifetrue_db.sql

# 2. Admin dashboard extra tables
mysql -u root -p Schoolifetrue_db < admin_schema.sql
```

The seed data creates:
- Default super admin: **username:** `superadmin` **password:** `Admin@2024`
- 4 sample organizations
- ⚠️ Change the super admin password immediately after first login!

### Step 2 — Configure Database Password

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/Schoolifetrue_db?...
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE
```

### Step 3 — Run with IntelliJ IDEA / VS Code

```bash
# From the project root
mvn spring-boot:run
```

Or open in IntelliJ → Run `ScholifeAdminApplication.java`

The dashboard starts at: **http://localhost:8080**

### Step 4 — First Login

1. Go to http://localhost:8080/login
2. Username: `superadmin` | Password: `Admin@2024`
3. Or go to http://localhost:8080/setup if no admin exists yet

---

## Running Alongside Flask

Both servers share the same MySQL database:

| Service | Port | Purpose |
|---|---|---|
| Flask (Python) | 5000 | Mobile app REST + Socket.IO |
| Spring Boot (Java) | 8080 | Admin web dashboard + org API |
| MySQL | 3306 | Shared database |

Flutter's `OrgPostService` connects to Spring Boot on port 8080:
```dart
const String _springBootBase = 'http://192.168.1.26:8080/api/org-post';
```
This is already wired — no changes needed on the Flutter side.

---

## Role Permissions

| Role | Students | Organizations | Officers | Admins | Logs |
|---|---|---|---|---|---|
| SUPER_ADMIN | ✅ Full | ✅ Full | ✅ Full | ✅ Full | ✅ |
| OSA | ✅ Full | ✅ Full | ✅ Full | ❌ | ✅ |
| ADAA | ✅ View | ✅ View | ✅ View | ❌ | ✅ |
| ADAF | ✅ View | ✅ View | ✅ View | ❌ | ✅ |
| DO | ✅ View | ✅ View | ✅ View | ❌ | ✅ |

---

## Mobile Integration Notes

When you appoint an officer in the admin dashboard, the Flutter mobile app  
automatically sees the change through `OrgPostService.fetchMyOrganizations()`.  
The student will then see the FAB button to post news/events on their home screen.
