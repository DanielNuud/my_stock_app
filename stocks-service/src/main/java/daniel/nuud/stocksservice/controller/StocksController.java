package daniel.nuud.stocksservice.controller;

import daniel.nuud.stocksservice.model.StockPrice;
import daniel.nuud.stocksservice.service.StocksPriceService;
import daniel.nuud.stocksservice.service.WebSocketClient;
import daniel.nuud.stocksservice.subscriptions.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StocksController {

    private final StocksPriceService stockPriceService;
    private final WebSocketClient wsClient;
    private final Subscription subscriptions;

    @GetMapping("/{ticker}")
    public List<StockPrice> getPrices(@PathVariable String ticker) {
        return stockPriceService.getPrices(ticker.toUpperCase());
    }

    @PostMapping("/subscribe/{ticker}")
    public String subscribe(@PathVariable String ticker,
                            @RequestHeader(value = "X-User-Key", required = false) String userKey) {
        if (userKey == null || userKey.isBlank()) userKey = "guest";

        subscriptions.subscribe(userKey, ticker);

        wsClient.subscribeTo(ticker.toUpperCase());

        return "Subscribed to ticker: " + ticker.toUpperCase();
    }

    @DeleteMapping("/subscribe/{ticker}")
    public String unsubscribe(@PathVariable String ticker,
                              @RequestHeader(value = "X-User-Key", required = false) String userKey) {
        if (userKey == null || userKey.isBlank()) userKey = "guest";

        subscriptions.unsubscribe(userKey, ticker);

//        if (subscriptions.listeners(ticker).isEmpty()) {
//            wsClient.unsubscribe(ticker);
//        }

        return "Unsubscribed from ticker: " + ticker.toUpperCase();
    }
}
