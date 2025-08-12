package daniel.nuud.notificationservice.controller;

import daniel.nuud.notificationservice.dto.CreateNotificationRequest;
import daniel.nuud.notificationservice.dto.NotificationResponse;
import daniel.nuud.notificationservice.service.NotificationService;
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

    @PostMapping
    public ResponseEntity<Map<String,Object>> createNotification(@RequestBody @Valid CreateNotificationRequest request) {
        Long id =  notificationService.createNotification(request);
        return ResponseEntity.accepted().body(Map.of("id",id));
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestParam String userKey,
            @RequestParam(required = false)Instant since
            ) {
        return notificationService.listNotifications(userKey, since);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markNotificationRead(id);
        return ResponseEntity.noContent().build();
    }
}
