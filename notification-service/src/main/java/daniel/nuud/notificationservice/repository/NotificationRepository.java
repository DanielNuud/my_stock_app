package daniel.nuud.notificationservice.repository;

import daniel.nuud.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop200ByUserKeyOrderByCreatedAtDesc(String userKey);

    List<Notification> findByUserKeyAndCreatedAtAfterOrderByCreatedAtDesc(String userKey, Instant since);

    Optional<Notification> findByDedupeKey(String dedupeKey);
}
