package VNNet.VNNet.Controller;

import VNNet.VNNet.ApiResponse;
import VNNet.VNNet.DTO.LoginRequest;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @PostMapping(value = "/user/login", produces = "application/json")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody LoginRequest loginRequest) throws JsonProcessingException {
            User user = userService.login(loginRequest.getPhoneNumber(), loginRequest.getPassword());
            if (user != null) {
                logger.info("Login successful for phone number: {}", loginRequest.getPhoneNumber());
                return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", user));
            }

            logger.warn("Invalid login attempt for phone number: {}", loginRequest.getPhoneNumber());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid phone number or password", null));

        }
    }
