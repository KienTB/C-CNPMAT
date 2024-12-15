package VNNet.VNNet.Service;

import VNNet.VNNet.Response.AuthenticationResponse;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    public AuthenticationResponse login(String phoneNumber, String password) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone number: " + phoneNumber));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String jwtToken = jwtService.generateToken(userDetails);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(user.getUserId())
                .role(user.getRole())
                .name(user.getName())
                .teacherId(user.getTeacherId())
                .build();
    }

    public User registerUser(String phoneNumber, String password, String email, String name, String address, String role) {
        logger.debug("Processing user registration for phone number: {}", phoneNumber);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            logger.warn("User already exists with phone number: {}", phoneNumber);
            throw new IllegalArgumentException("Phone number already registered");
        }
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid role. Must be one of: parent, teacher, admin");
        }

        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setName(name);
        user.setAddress(address);
        user.setRole(role);

        user = userRepository.save(user);

        if ("teacher".equals(role)) {
            user.setTeacherId(user.getUserId());
            user = userRepository.save(user);
        }

        logger.info("User registered successfully with phone number: {} and role: {}", phoneNumber, role);
        return user;
    }

    private boolean isValidRole(String role) {
        return "parent".equals(role) || "teacher".equals(role) || "admin".equals(role);
    }

    public AuthenticationResponse changePassword(String phoneNumber, String oldPassword, String newPassword, String confirmPassword) {
        logger.debug("Processing password change for phone number: {}", phoneNumber);

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone number: " + phoneNumber));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPassword())
                .authorities(user.getRole())
                .build();

        String jwtToken = jwtService.generateToken(userDetails);

        logger.info("Password successfully changed for user with phone number: {}", phoneNumber);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(user.getUserId())
                .role(user.getRole())
                .name(user.getName())
                .build();
    }

    public User updateUser(Long userId, String phoneNumber, String email, String name, String address, String role) {
        logger.debug("Admin updating user info for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            userRepository.findByPhoneNumber(phoneNumber)
                    .filter(existingUser -> !existingUser.getUserId().equals(userId))
                    .ifPresent(existingUser -> {
                        throw new IllegalArgumentException("Phone number already in use by another user");
                    });
            user.setPhoneNumber(phoneNumber);
        }

        if (email != null && !email.trim().isEmpty()) {
            userRepository.findByEmail(email)
                    .filter(existingUser -> !existingUser.getUserId().equals(userId))
                    .ifPresent(existingUser -> {
                        throw new IllegalArgumentException("Email already in use by another user");
                    });
            user.setEmail(email);
        }

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }
        if (address != null && !address.trim().isEmpty()) {
            user.setAddress(address);
        }
        if (role != null && isValidRole(role)) {
            user.setRole(role);
        } else if (role != null) {
            throw new IllegalArgumentException("Invalid role. Must be one of: parent, teacher, admin");
        }

        logger.info("User info updated for userId: {}", userId);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        logger.debug("Deleting user with userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        userRepository.delete(user);
        logger.info("User with userId: {} deleted successfully", userId);
    }
}
