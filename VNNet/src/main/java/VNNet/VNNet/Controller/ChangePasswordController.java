package VNNet.VNNet.Controller;

import VNNet.VNNet.Response.ApiResponse;
import VNNet.VNNet.Response.AuthenticationResponse;
import VNNet.VNNet.Request.ChangePasswordRequest;
import VNNet.VNNet.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChangePasswordController {
    @Autowired
    private UserService userService;

    @PostMapping("/user/change-password")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String phoneNumber = authentication.getName();

            userService.changePassword(
                    phoneNumber,
                    request.getOldPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Password changed successfully",
                    null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    "Current password is incorrect",
                    null
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false,
                    "An error occurred while changing password",
                    null
            ));
        }
    }
}
