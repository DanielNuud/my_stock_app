package daniel.nuud.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import daniel.nuud.notificationservice.dto.CreateNotificationRequest;
import daniel.nuud.notificationservice.dto.NotificationResponse;
import daniel.nuud.notificationservice.model.Level;
import daniel.nuud.notificationservice.model.Notification;
import daniel.nuud.notificationservice.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Long createNotification(CreateNotificationRequest createNotificationRequest) {
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

    @Transactional
    public List<NotificationResponse> listNotifications(String userKey, Instant since) {
        return notificationRepository.findForUserSince(userKey, since)
                .stream()
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

    @Transactional
    public void markNotificationRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> n.setReadFlag(true));
    }
}
