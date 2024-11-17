package VNNet.VNNet.Service;

import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    @Autowired
    StudentRepository studentRepository;

    public Student findStudentById(int studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }
}
