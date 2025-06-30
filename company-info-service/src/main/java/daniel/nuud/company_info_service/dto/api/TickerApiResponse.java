package daniel.nuud.company_info_service.dto.api;

import lombok.Data;

import java.util.List;

@Data
public class TickerApiResponse {
    private String status;
    private Integer count;
    private String next_url;
    private List<Ticker> results;
}
