package VNNet.VNNet.Controller;

import VNNet.VNNet.DTO.ApiResponse;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.DTO.RegisterRequest;
import VNNet.VNNet.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping(value = "/user/register", produces = "application/json")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody RegisterRequest registerRequest) throws JsonProcessingException {
        logger.info("Received RegisterRequest: " + registerRequest);
        if (registerRequest.getPhoneNumber() == null || registerRequest.getPhoneNumber().isEmpty() ||
                registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty() ||
                registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty() ||
                registerRequest.getName() == null || registerRequest.getName().isEmpty() ||
                registerRequest.getAddress() == null || registerRequest.getAddress().isEmpty()) {
            logger.warn("Please complete all fields");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Please complete all required fields", null));
        }

        User newUser = userService.registerUser(registerRequest.getPhoneNumber(), registerRequest.getPassword(), registerRequest.getEmail(), registerRequest.getName(), registerRequest.getAddress());

        if (newUser == null) {
            logger.error("Failed to register user");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to register user", null));
        } else {
            logger.info("User registered successfully");
            return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", newUser));
        }
    }
}
