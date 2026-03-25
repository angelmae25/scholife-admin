package com.scholife.admin.repository;

import com.scholife.admin.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllByOrderByCreatedAtDesc();
    List<Student> findByStatus(Student.StudentStatus status);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentId(String studentId);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = 'ACTIVE'")
    long countActive();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = 'PENDING'")
    long countPending();

    List<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(
            String firstName, String lastName, String studentId);
}
