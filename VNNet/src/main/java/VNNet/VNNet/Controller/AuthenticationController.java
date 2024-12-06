package VNNet.VNNet.Controller;

import VNNet.VNNet.Response.AuthenticationResponse;
import VNNet.VNNet.Model.RefreshToken;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Request.RefreshTokenRequest;
import VNNet.VNNet.Service.JwtService;
import VNNet.VNNet.Service.RefreshTokenService;
import VNNet.VNNet.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @PostMapping(value = "/refresh", produces = {"application/json"})
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String accessToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(accessToken)
                .userId(user.getUserId())
                .refreshToken(refreshToken.getToken())
                .build());
    }
}
