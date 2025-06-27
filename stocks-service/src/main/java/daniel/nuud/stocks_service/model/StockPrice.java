package daniel.nuud.stocks_service.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockPrice {

    private String symbol;
    private double price;
    private long timestamp;

}