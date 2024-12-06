package VNNet.VNNet.Controller;

import VNNet.VNNet.Model.Grade;
import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.GradeRepository;
import VNNet.VNNet.Repository.StudentRepository;
import VNNet.VNNet.Repository.UserRepository;
import VNNet.VNNet.Request.GradeRequest;
import VNNet.VNNet.Response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GradeController {
    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/parent/grade/{studentId}")
    public ResponseEntity<List<Grade>> getGradesByStudentId(@PathVariable Long studentId) {
        List<Grade> grades = gradeRepository.findByStudent_StudentId(studentId);
        return ResponseEntity.ok(grades);
    }

    @PostMapping("/teacher/add/grade")
    public ResponseEntity<ApiResponse<Grade>> addGrade(@RequestBody GradeRequest gradeRequest) {
        Student student = studentRepository.findById(gradeRequest.getStudentId())
                .orElse(null);
        if (student == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Student not found", null));
        }

        User user = userRepository.findById(gradeRequest.getUserId())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "User not found", null));
        }

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setUser(user);
        grade.setSubject(gradeRequest.getSubject());
        grade.setScore(gradeRequest.getScore());
        grade.setTerm(gradeRequest.getTerm());

        gradeRepository.save(grade);
        return ResponseEntity.ok().body(new ApiResponse<>(true, "Grade added successfully", grade));
    }
}
