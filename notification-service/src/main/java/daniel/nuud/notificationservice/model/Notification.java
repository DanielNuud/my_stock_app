package daniel.nuud.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "uk_notifications_dedupe", columnNames = "dedupeKey")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userKey;
    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private Level level = Level.INFO;

    private boolean readFlag = false;
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    @Column(name = "dedupeKey", nullable = false, unique = true)
    private String dedupeKey;
    private String payloadJson;
}
