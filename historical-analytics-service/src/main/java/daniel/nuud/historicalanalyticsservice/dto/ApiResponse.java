package daniel.nuud.historicalanalyticsservice.dto;

import lombok.Data;

@Data
public class ApiResponse {
    private String status;
    private StockBarApi results;
}
