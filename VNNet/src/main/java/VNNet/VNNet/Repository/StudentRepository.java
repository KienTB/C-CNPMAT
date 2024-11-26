package VNNet.VNNet.Repository;

import VNNet.VNNet.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findByStudentIdAndUser_UserId(Long studentId, Long userId);
}
