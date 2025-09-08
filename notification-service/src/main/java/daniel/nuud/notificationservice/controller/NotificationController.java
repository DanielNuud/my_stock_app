package daniel.nuud.notificationservice.controller;

import daniel.nuud.notificationservice.dto.CreateNotificationRequest;
import daniel.nuud.notificationservice.dto.NotificationResponse;
import daniel.nuud.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Create a notification",
            description = "Creates a notification and returns its server-generated id."
    )
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Notification payload"
            )
            @RequestBody @Valid CreateNotificationRequest request) {

        Long id = notificationService.createNotification(request);
        return ResponseEntity.accepted().body(Map.of("id", id));
    }

    @Operation(
            summary = "List notifications",
            description = "Returns notifications for a given user. If `since` is provided, returns items created after that timestamp."
    )
    @GetMapping
    public List<NotificationResponse> getNotifications(
            @Parameter(
                    description = "User key to filter notifications.",
                    example = "u123",
                    required = true
            )
            @RequestParam String userKey,

            @Parameter(
                    description = "Return only notifications created strictly after this UTC timestamp (ISO-8601).",
                    example = "2025-09-07T12:30:00Z"
            )
            @RequestParam(required = false) Instant since
    ) {
        return notificationService.listNotifications(userKey, since);
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Sets the read flag to true for the given notification id."
    )
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @Parameter(description = "Notification id", example = "123")
            @PathVariable Long id
    ) {
        notificationService.markNotificationRead(id);
        return ResponseEntity.noContent().build();
    }
}
