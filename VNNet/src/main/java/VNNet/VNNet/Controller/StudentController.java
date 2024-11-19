package VNNet.VNNet.Controller;

import VNNet.VNNet.DTO.ApiResponse;
import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @GetMapping(value = "/students/{studentId}", produces = "application/json")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable int studentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String phoneNumber = userDetails.getUsername();

        try {
            Student student = studentService.findStudentById(studentId);

            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Student not found", null));
            }

            // Kiểm tra xem student có thuộc về user đang đăng nhập không
            User parentUser = student.getUser();
            if (parentUser == null || !parentUser.getPhoneNumber().equals(phoneNumber)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "You don't have permission to view this student", null));
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Student found", student));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving student: " + e.getMessage(), null));
        }
    }
}
