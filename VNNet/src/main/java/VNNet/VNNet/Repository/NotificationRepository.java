package VNNet.VNNet.Repository;

import VNNet.VNNet.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByTitle(String title);
}
