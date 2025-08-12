package daniel.nuud.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CreateNotificationRequest(
        @NotBlank String userKey,
        @NotBlank String title,
        @NotBlank String message,
        String level,
        @NotBlank String dedupeKey,
        Map<String, Object> payload
) {
}
