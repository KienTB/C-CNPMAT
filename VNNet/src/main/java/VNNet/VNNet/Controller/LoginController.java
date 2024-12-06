package VNNet.VNNet.Controller;

import VNNet.VNNet.Response.ApiResponse;
import VNNet.VNNet.Response.AuthenticationResponse;
import VNNet.VNNet.Request.LoginRequest;
import VNNet.VNNet.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @PostMapping(value = "/user/login", produces = "application/json")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody LoginRequest loginRequest) throws JsonProcessingException {
        try {
            logger.info("Login attempt for phone number: {}", loginRequest.getPhoneNumber());

            AuthenticationResponse authResponse = userService.login(
                    loginRequest.getPhoneNumber(),
                    loginRequest.getPassword()
            );

            logger.info("Login successful for phone number: {}", loginRequest.getPhoneNumber());
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Login successful",
                    authResponse
            ));

        } catch (UsernameNotFoundException e) {
            logger.warn("User not found for phone number: {}", loginRequest.getPhoneNumber());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(
                            false,
                            "User not found",
                            null
                    ));

        } catch (BadCredentialsException e) {
            logger.warn("Invalid password for phone number: {}", loginRequest.getPhoneNumber());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(
                            false,
                            "Invalid password",
                            null
                    ));

        } catch (Exception e) {
            logger.error("Error during login for phone number: {}", loginRequest.getPhoneNumber(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(
                            false,
                            "An error occurred during login",
                            null
                    ));
        }
    }
    }
