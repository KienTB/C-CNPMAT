package VNNet.VNNet.Controller;

import VNNet.VNNet.Model.Notification;
import VNNet.VNNet.Repository.NotificationRepository;
import VNNet.VNNet.Response.ApiResponse;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/get/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        ApiResponse<List<Notification>> response = new ApiResponse<>(true, "Notifications retrieved successfully", notifications);
        return ResponseEntity.ok(response);
    }
}
