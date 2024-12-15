package VNNet.VNNet.Controller;

import VNNet.VNNet.Model.Notification;
import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.NotificationRepository;
import VNNet.VNNet.Repository.StudentRepository;
import VNNet.VNNet.Repository.UserRepository;
import VNNet.VNNet.Request.NotificationRegisterRequest;
import VNNet.VNNet.Request.StudentRegisterRequest;
import VNNet.VNNet.Request.UpdateStudentRequest;
import VNNet.VNNet.Request.UpdateUserRequest;
import VNNet.VNNet.Response.ApiResponse;
import VNNet.VNNet.Service.NotificationService;
import VNNet.VNNet.Service.StudentService;
import VNNet.VNNet.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.aspectj.weaver.ast.Not;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;



    @GetMapping("/get/all/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            ApiResponse<List<User>> response = new ApiResponse<>(false, "User not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("");

        if (!"admin".equals(role)) {
            ApiResponse<List<User>> response = new ApiResponse<>(false, "Access denied. Admin role required.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            List<User> users = (List<User>) userRepository.findAll();
            users.forEach(user -> user.setPassword(null));

            ApiResponse<List<User>> response = new ApiResponse<>(true, "Users retrieved successfully", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            ApiResponse<List<User>> response = new ApiResponse<>(false, "Error retrieving users", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/user/{userId}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest updateUserRequest) {
        try {
            User updatedUser = userService.updateUser(
                    userId,
                    updateUserRequest.getPhoneNumber(),
                    updateUserRequest.getEmail(),
                    updateUserRequest.getName(),
                    updateUserRequest.getAddress(),
                    updateUserRequest.getRole()
            );
            ApiResponse<User> response = new ApiResponse<>(true, "User updated successfully", updatedUser);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ApiResponse<User> response = new ApiResponse<>(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            ApiResponse<User> response = new ApiResponse<>(false, "An unexpected error occurred while updating the user", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            ApiResponse<String> response = new ApiResponse<>(false, "User not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("");

        if (!"admin".equals(role)) {
            ApiResponse<String> response = new ApiResponse<>(false, "Access denied. Admin role required.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            userService.deleteUser(userId);
            ApiResponse<String> response = new ApiResponse<>(true, "User deleted successfully", null);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = new ApiResponse<>(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error deleting user with id: {}", userId, e);
            ApiResponse<String> response = new ApiResponse<>(false, "An unexpected error occurred while deleting the user", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/register/student", produces = "application/json")
    public ResponseEntity<ApiResponse<Student>> registerStudent(@RequestBody StudentRegisterRequest studentRegisterRequest) throws JsonProcessingException {
        logger.info("Received RegisterRequest: " + studentRegisterRequest);
        if (studentRegisterRequest.getName() == null || studentRegisterRequest.getName().isEmpty() ||
                studentRegisterRequest.getBirthDate() == null || studentRegisterRequest.getBirthDate().isEmpty() ||
                studentRegisterRequest.getGender() == null || studentRegisterRequest.getGender().isEmpty() ||
                studentRegisterRequest.getClass_name() == null || studentRegisterRequest.getClass_name().isEmpty() ||
                studentRegisterRequest.getUserId() == null || studentRegisterRequest.getTeacherId() == null ||
                studentRegisterRequest.getAddress() == null || studentRegisterRequest.getAddress().isEmpty()) {
            logger.warn("điền vào tất cả ô trống");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Vui lòng điền vào tất cả ô trống", null));
        }

        User user = userRepository.findById(studentRegisterRequest.getUserId()).orElse(null);
        if (user == null) {
            logger.warn("User không tồn tại với userId: {}", studentRegisterRequest.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Người dùng không tồn tại", null));
        }

        User teacher = userRepository.findById(studentRegisterRequest.getTeacherId()).orElse(null);
        if (teacher == null) {
            logger.warn("Teacher không tồn tại với teacherId: {}", studentRegisterRequest.getTeacherId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Giáo viên không tồn tại", null));
        }

        Student newStudent = studentService.registerStudent(studentRegisterRequest.getName(),
                studentRegisterRequest.getBirthDate(),
                studentRegisterRequest.getGender(),
                studentRegisterRequest.getClass_name(),
                user,
                studentRegisterRequest.getAddress(),
                teacher);

        if (newStudent == null) {
            logger.error("Failed to register student");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to register student", null));
        } else {
            logger.info("Student registered successfully");
            return ResponseEntity.ok(new ApiResponse<>(true, "Student registered successfully", newStudent));
        }
    }

    @GetMapping("/get/all/students")
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            ApiResponse<List<Student>> response = new ApiResponse<>(false, "Student not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("");

        if (!"admin".equals(role)) {
            ApiResponse<List<Student>> response = new ApiResponse<>(false, "Access denied. Admin role required.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            List<Student> students = (List<Student>) studentRepository.findAll();

            ApiResponse<List<Student>> response = new ApiResponse<>(true, "Students retrieved successfully", students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            ApiResponse<List<Student>> response = new ApiResponse<>(false, "Error retrieving students", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/student/{studentId}")
    public ResponseEntity<ApiResponse<Student>> updateStudent(
            @PathVariable Long studentId,
            @RequestBody UpdateStudentRequest updateStudentRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new ApiResponse<>(false, "User not authenticated", null)
                );
            }

            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .orElse("");

            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(false, "Access denied. Admin role required.", null)
                );
            }

            Student existingStudent = studentService.findStudentById(studentId);
            if (existingStudent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(false, "Student not found", null)
                );
            }

            existingStudent.setName(updateStudentRequest.getName());
            existingStudent.setBirthDate(LocalDate.parse(updateStudentRequest.getBirthDate()));
            existingStudent.setGender(updateStudentRequest.getGender());
            existingStudent.setClass_name(updateStudentRequest.getClass_name());
            existingStudent.setAddress(updateStudentRequest.getAddress());
            User user = userRepository.findById(updateStudentRequest.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            existingStudent.setTeacher(user);
            User teacher = userRepository.findById(updateStudentRequest.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            existingStudent.setTeacher(teacher);

            Student updatedStudent = studentRepository.save(existingStudent);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Student updated successfully", updatedStudent)
            );

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error while updating student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, "An unexpected error occurred", null)
            );
        }
    }

    @DeleteMapping("/delete/student/{studentId}")
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable Long studentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new ApiResponse<>(false, "User not authenticated", null)
                );
            }

            String role = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .findFirst()
                    .orElse("");

            if (!"admin".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(false, "Access denied. Admin role required.", null)
                );
            }

            studentService.deleteStudent(studentId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Student deleted successfully", null)
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        } catch (Exception e) {
            logger.error("Unexpected error while deleting student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, "An unexpected error occurred", null)
            );
        }
    }

}

