package VNNet.VNNet.Service;

import VNNet.VNNet.Model.Notification;
import VNNet.VNNet.Model.Student;
import VNNet.VNNet.Model.User;
import VNNet.VNNet.Repository.NotificationRepository;
import VNNet.VNNet.Repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification updateNotification(Long notificationId, String title, String content) {
        logger.debug("Updating notification with ID: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));

        notification.setTitle(title);
        notification.setContent(content);

        Notification updatedNotification = notificationRepository.save(notification);
        logger.info("Notification with ID: {} updated successfully", notificationId);

        return updatedNotification;
    }
    public void deleteNotification(Long notificationId) {
        logger.debug("Deleting notification with userId: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + notificationId));

        notificationRepository.delete(notification);
        logger.info("User with userId: {} deleted successfully", notificationId);
    }
}
