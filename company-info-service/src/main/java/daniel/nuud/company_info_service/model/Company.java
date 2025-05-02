package daniel.nuud.company_info_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "companies")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Company {

    @Id
    private String ticker;
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
