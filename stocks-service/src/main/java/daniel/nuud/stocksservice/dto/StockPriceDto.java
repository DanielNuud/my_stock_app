package daniel.nuud.stocksservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceDto {
    private Double closePrice;
    private Long timestamp;
    private String ticker;
}
