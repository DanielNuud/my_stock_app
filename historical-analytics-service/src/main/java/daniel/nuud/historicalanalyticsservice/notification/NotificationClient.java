package daniel.nuud.historicalanalyticsservice.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    private final RestClient notificationRestClient;

    public void sendNotification(
            String userKey,
            String title,
            String message,
            String level,
            String dedupeSuffix
    ) {
        String dedupeKey = buildDedupeKey(userKey, dedupeSuffix);

        var body = Map.of(
                "userKey", userKey,
                "title", title,
                "message", message,
                "level", level,
                "dedupeKey", dedupeKey
        );

        try {
            notificationRestClient.post().uri("/api/notifications")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (req, res) -> { throw new RuntimeException("Notify failed: " + res.getStatusCode()); })
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Notification post failed: {}", e.getMessage());
        }
    }

    private String buildDedupeKey(String userKey, String suffix) {
        long epochMinute = Instant.now().getEpochSecond() / 60;
        return userKey + ":" + suffix + ":" + epochMinute;
    }
}
