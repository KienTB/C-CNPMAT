package VNNet.VNNet.Controller;

import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.StudentRepository;
import VNNet.VNNet.Repository.UserRepository;
import VNNet.VNNet.Request.StudentRegisterRequest;
import VNNet.VNNet.Response.ApiResponse;
import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Service.StudentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/parent/student/{studentId}", produces = "application/json")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable int studentId) {
        try {
            // Lấy thông tin người dùng đang đăng nhập
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Unauthorized", null));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String phoneNumber = userDetails.getUsername();

            Student student = studentService.findStudentById(Long.valueOf(studentId));
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Student not found", null));
            }

            // Kiểm tra xem học sinh có thuộc về user đang đăng nhập không
            if (!studentService.isStudentBelongToUser(student, phoneNumber)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You don't have permission to view this student", null));
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Student found successfully", student));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null));
        }
    }

    @GetMapping(value = "/teacher/student/{teacherId}", produces = "application/json")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByTeacherId(@PathVariable Long teacherId) {
        try {
            List<Student> students = studentService.findStudentsByTeacherId(teacherId);

            if (students.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "No students found for this teacher", null));
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Students found successfully", students));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null));
        }
    }
}
