package daniel.nuud.stocksservice.controller;

import daniel.nuud.stocksservice.model.StockPrice;
import daniel.nuud.stocksservice.service.StocksPriceService;
import daniel.nuud.stocksservice.service.WebSocketClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StocksController {

    private final StocksPriceService stockPriceService;
    private final WebSocketClient wsClient;

    public StocksController(StocksPriceService stockPriceService, WebSocketClient wsClient) {
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
