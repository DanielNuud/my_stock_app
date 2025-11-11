package daniel.nuud.historicalanalyticsservice.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class StockBar {

    @EmbeddedId
    private StockBarId id;

    private Double closePrice;

    private Double lowPrice;

    private Double highPrice;

    private Double openPrice;

    private Integer volume;

    private Integer numberOfTransactions;

    private Long timestamp;
}
