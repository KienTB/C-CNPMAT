package VNNet.VNNet.Controller;

import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.UserRepository;
import VNNet.VNNet.Request.UpdateUserRequest;
import VNNet.VNNet.Response.ApiResponse;
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

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

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

}

