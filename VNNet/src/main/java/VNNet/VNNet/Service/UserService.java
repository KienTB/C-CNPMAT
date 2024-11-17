package VNNet.VNNet.Service;

import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;

    public User login(String phoneNumber, String password) {
        logger.debug("Processing login for phone number: {}", phoneNumber);

        if (phoneNumber == null || password == null ||
                phoneNumber.trim().isEmpty() || password.trim().isEmpty()) {
            logger.error("Phone number or password is empty");
            throw new IllegalArgumentException("Phone number and password are required");
        }

        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isPresent()) {
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                logger.info("User found and password matched for phone number: {}", phoneNumber);
                return user.get();
            } else {
                logger.warn("Invalid password for phone number: {}", phoneNumber);
            }
        } else {
            logger.warn("No user found with phone number: {}", phoneNumber);
        }
        return null;
    }

    public User registerUser(String phoneNumber, String password, String email, String name, String address) {
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

        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setName(name);
        user.setAddress(address);

        logger.info("Saving new user with phone number: {}", phoneNumber);
        return userRepository.save(user);
    }
}