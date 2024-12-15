package VNNet.VNNet.Controller;

import VNNet.VNNet.Model.Notification;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.NotificationRepository;
import VNNet.VNNet.Repository.UserRepository;
import VNNet.VNNet.Request.NotificationRegisterRequest;
import VNNet.VNNet.Request.UpdateNotificationRequest;
import VNNet.VNNet.Response.ApiResponse;
import VNNet.VNNet.Service.NotificationService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/get/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        ApiResponse<List<Notification>> response = new ApiResponse<>(true, "Notifications retrieved successfully", notifications);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/admin/register/notification", produces = "application/json")
    public ResponseEntity<ApiResponse<Notification>> registerNotification(
            @RequestBody NotificationRegisterRequest notificationRegisterRequest) throws JsonProcessingException {
        logger.info("Yêu cầu thông báo nhận được: " + notificationRegisterRequest);

        if (notificationRegisterRequest.getTitle() == null || notificationRegisterRequest.getTitle().isEmpty() ||
                notificationRegisterRequest.getContent() == null || notificationRegisterRequest.getContent().isEmpty() ||
                notificationRegisterRequest.getUserId() == null) {
            logger.warn("Vui lòng điền đầy đủ thông tin vào tất cả các ô trống");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Vui lòng điền vào tất cả ô trống", null));
        }

        Notification existingNotification = notificationRepository.findByTitle(notificationRegisterRequest.getTitle()).orElse(null);
        if (existingNotification != null) {
            logger.warn("Thông báo đã tồn tại với tiêu đề: {}", notificationRegisterRequest.getTitle());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "Thông báo đã tồn tại", null));
        }

        User user = userRepository.findById(notificationRegisterRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + notificationRegisterRequest.getUserId()));

        Notification newNotification = new Notification();
        newNotification.setTitle(notificationRegisterRequest.getTitle());
        newNotification.setContent(notificationRegisterRequest.getContent());
        newNotification.setUser(user);

        Notification savedNotification = notificationRepository.save(newNotification);

        if (savedNotification == null) {
            logger.error("Đăng ký thông báo thất bại");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Đăng ký thông báo thất bại", null));
        }

        logger.info("Đăng ký thông báo thành công");
        return ResponseEntity.ok(new ApiResponse<>(true, "Đăng ký thông báo thành công", savedNotification));
    }

    @PutMapping("/admin/update/notification/{notificationId}")
    public ResponseEntity<ApiResponse<Notification>> updateNotification(
            @PathVariable Long notificationId,
            @RequestBody UpdateNotificationRequest updateNotificationRequest) {

        logger.info("Updating notification with ID: {}", notificationId);

        if (updateNotificationRequest.getTitle() == null || updateNotificationRequest.getTitle().isEmpty() ||
                updateNotificationRequest.getContent() == null || updateNotificationRequest.getContent().isEmpty()) {
            logger.warn("Invalid input: title or content is missing");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Title and content cannot be empty", null));
        }

        try {
            Notification updatedNotification = notificationService.updateNotification(
                    notificationId,
                    updateNotificationRequest.getTitle(),
                    updateNotificationRequest.getContent()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification updated successfully", updatedNotification));

        } catch (IllegalArgumentException e) {
            logger.error("Error updating notification: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error while updating notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }
    }

    @DeleteMapping("/admin/delete/notification/{notificationId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long notificationId) {
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

            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Notification deleted successfully", null)
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
