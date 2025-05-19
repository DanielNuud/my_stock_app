package daniel.nuud.historicalanalyticsservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_bars")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StockBar {

    @Id
    private String ticker;

    private Double closePrice;

    private Double lowPrice;

    private Double highPrice;

    private Double openPrice;

    private Integer volume;

    private Integer numberOfTransactions;

    private Integer timestamp;
}
