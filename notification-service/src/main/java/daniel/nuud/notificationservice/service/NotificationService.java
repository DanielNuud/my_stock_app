package daniel.nuud.notificationservice.service;

import daniel.nuud.notificationservice.dto.CreateNotificationRequest;
import daniel.nuud.notificationservice.dto.NotificationResponse;
import daniel.nuud.notificationservice.model.Level;
import daniel.nuud.notificationservice.model.Notification;
import daniel.nuud.notificationservice.repository.NotificationRepository;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Long createNotification(CreateNotificationRequest createNotificationRequest) {
        log.info("Received CreateNotificationRequest {}", createNotificationRequest);
        Optional<Notification> existing = notificationRepository.findByDedupeKey(createNotificationRequest.dedupeKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }


        Notification notification = new Notification();
        notification.setUserKey(createNotificationRequest.userKey());
        notification.setTitle(createNotificationRequest.title());
        notification.setMessage(createNotificationRequest.message());
        notification.setLevel(Level.valueOf(
                Optional.ofNullable(createNotificationRequest.level()).orElse("INFO").toUpperCase()
        ));
        notification.setDedupeKey(createNotificationRequest.dedupeKey());

        return notificationRepository.save(notification).getId();
    }

    public List<NotificationResponse> listNotifications(String userKey, @Nullable Instant since) {

        List<Notification> list = (since == null)
                ? notificationRepository.findTop200ByUserKeyOrderByCreatedAtDesc(userKey)
                : notificationRepository.findByUserKeyAndCreatedAtAfterOrderByCreatedAtDesc(userKey, since);

        return list.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getUserKey(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getLevel().name(),
                        n.isReadFlag(),
                        n.getCreatedAt()
                ))
                .toList();
    }

    public void markNotificationRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> n.setReadFlag(true));
    }
}
