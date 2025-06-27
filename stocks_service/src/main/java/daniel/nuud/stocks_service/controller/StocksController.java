package daniel.nuud.stocks_service.controller;

import daniel.nuud.stocks_service.model.StockPrice;
import daniel.nuud.stocks_service.service.PolygonWebSocketClient;
import daniel.nuud.stocks_service.service.StocksPriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StocksController {

    private final StocksPriceService stockPriceService;
    private final PolygonWebSocketClient wsClient;

    public StocksController(StocksPriceService stockPriceService, PolygonWebSocketClient wsClient) {
        this.stockPriceService = stockPriceService;
        this.wsClient = wsClient;
    }

    @GetMapping("/{ticker}")
    public List<StockPrice> getPrices(@PathVariable String ticker) {
        return stockPriceService.getPrices(ticker.toUpperCase());
    }

    @PostMapping("/subscribe/{ticker}")
    public String subscribe(@PathVariable String ticker) {
        wsClient.subscribeTo(ticker.toUpperCase());
        return "Subscribed to ticker: " + ticker.toUpperCase();
    }

}
