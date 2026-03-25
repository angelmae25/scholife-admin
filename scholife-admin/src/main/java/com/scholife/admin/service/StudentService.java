package com.scholife.admin.service;

import com.scholife.admin.model.Student;
import com.scholife.admin.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepo;

    public List<Student> getAll()                           { return studentRepo.findAllByOrderByCreatedAtDesc(); }
    public Optional<Student> findById(Long id)              { return studentRepo.findById(id); }
    public List<Student> getByStatus(Student.StudentStatus s) { return studentRepo.findByStatus(s); }
    public Student save(Student s)                          { return studentRepo.save(s); }
    public long countAll()                                  { return studentRepo.count(); }
    public long countActive()                               { return studentRepo.countActive(); }
    public long countPending()                              { return studentRepo.countPending(); }

    public List<Student> search(String q) {
        return studentRepo
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(q, q, q);
    }

    public void setStatus(Long id, Student.StudentStatus status) {
        studentRepo.findById(id).ifPresent(s -> { s.setStatus(status); studentRepo.save(s); });
    }
}
