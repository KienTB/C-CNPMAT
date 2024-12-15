package VNNet.VNNet.Service;

import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    @Autowired
    private StudentRepository studentRepository;

    public Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }
    public boolean isStudentBelongToUser(Student student, String phoneNumber) {
        if (student == null || student.getUser() == null) {
            return false;
        }
        return student.getUser().getPhoneNumber().equals(phoneNumber);
    }

    public List<Student> findStudentsByTeacherId(Long teacherId) {
        return studentRepository.findByTeacher_UserId(teacherId);
    }

    public Student registerStudent(String name, String birthDate, String gender, String class_name, User userId, String address, User teacherId) {
        logger.debug("Xử lý đăng ký thông tin học sinh cho userId: {}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }

        Student student = new Student();
        student.setName(name);
        student.setBirthDate(LocalDate.parse(birthDate));
        student.setGender(gender);
        student.setClass_name(class_name);
        student.setUser(userId);
        student.setAddress(address);
        student.setTeacher(teacherId);

        logger.info("Saving new student with userId: {} and teacherId: {}", userId, teacherId);
        return studentRepository.save(student);
    }

    public void deleteStudent(Long studentId) {
        logger.debug("Deleting user with studentId: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + studentId));

        studentRepository.delete(student);
        logger.info("User with userId: {} deleted successfully", studentId);
    }

}
