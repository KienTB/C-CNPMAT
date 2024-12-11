package VNNet.VNNet.Controller;

import VNNet.VNNet.Response.ApiResponse;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.UserRepository;
import VNNet.VNNet.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/profile")
    public ResponseEntity<ApiResponse<User>> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            ApiResponse<User> response = new ApiResponse<>(false, "User not authenticated", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String phoneNumber = authentication.getName();

            Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                user.setPassword(null);

                ApiResponse<User> response = new ApiResponse<>(true, "User profile retrieved successfully", user);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<User> response = new ApiResponse<>(false, "User not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error retrieving user profile", e);
            ApiResponse<User> response = new ApiResponse<>(false, "Error retrieving user profile", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/admin/get/all/users")
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

}

