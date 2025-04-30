package daniel.nuud.company_info_service.dto;

import lombok.Data;

@Data
public class Ticket {
    private String ticker;
    private Branding branding;
    private Address address;
    private String description;
    private String homepageUrl;
    private String name;
    private String primaryExchange;
    private Long marketCap;
}
