package VNNet.VNNet.Controller;

import VNNet.VNNet.ApiResponse;
import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        Student student = studentService.findStudentById(studentId);

        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Student not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Student found", student));
    }
}
