package VNNet.VNNet.Repository;

import VNNet.VNNet.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByTeacher_UserId(Long teacherId);
}
