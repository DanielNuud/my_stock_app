package daniel.nuud.stocksservice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockPrice {

    private String ticker;
    private double price;
    private long timestamp;

}
