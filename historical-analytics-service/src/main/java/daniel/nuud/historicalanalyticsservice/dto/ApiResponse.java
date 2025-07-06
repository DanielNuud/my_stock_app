package daniel.nuud.historicalanalyticsservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponse {
    private String status;
    private List<StockBarApi> results;
}
