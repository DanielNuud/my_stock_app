package daniel.nuud.company_info_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Company",
        description = "Company profile entity identified by stock ticker. "
)
public class Company implements Serializable {

    @Id
    @Column(name = "ticker", nullable = false, unique = true)
    private String ticker;

    @PrePersist @PreUpdate
    void normalize() {
        if (ticker != null) ticker = ticker.trim().toUpperCase();
    }

    @Column(columnDefinition = "TEXT")
    private String description;
    private String name;
    private String homepageUrl;
    private String primaryExchange;
    private String marketCap;
    private String city;
    private String address1;
    private String iconUrl;
    private String logoUrl;

    private String status;

}
