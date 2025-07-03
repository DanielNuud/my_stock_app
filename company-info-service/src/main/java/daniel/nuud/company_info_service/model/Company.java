package daniel.nuud.company_info_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company implements Serializable {

    @Id
    @Column(name = "ticker", nullable = false, unique = true)
    private String ticker;

    @Column(columnDefinition = "TEXT")
    private String description;
    private String name;
    private String homepageUrl;
    private String primaryExchange;
    private Long marketCap;
    private String city;
    private String address1;
    private String iconUrl;
    private String logoUrl;

    private String status;

}
