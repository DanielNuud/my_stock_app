package daniel.nuud.company_info_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company implements Serializable {

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
