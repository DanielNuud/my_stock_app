package daniel.nuud.stocksservice.controller;

import daniel.nuud.stocksservice.model.StockPrice;
import daniel.nuud.stocksservice.service.StocksPriceService;
import daniel.nuud.stocksservice.service.WebSocketClient;
import daniel.nuud.stocksservice.subscriptions.Subscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stocks", description = "Historical prices and WebSocket subscription control")
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

    @Operation(
            summary = "Subscribe to real-time price stream",
            description = "Adds the current user to a WebSocket subscription for the given ticker."
    )
    @PostMapping("/subscribe/{ticker}")
    public String subscribe(
            @Parameter(
                    description = "Stock ticker symbol (uppercase), e.g. AAPL",
                    example = "AAPL",
                    required = true,
                    schema = @Schema(minLength = 1, maxLength = 12, pattern = "^[A-Z0-9.\\-]{1,12}$")
            )
            @PathVariable String ticker,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-User-Key",
                    description = "Optional user key for tracking/rate limits. Defaults to 'guest'.",
                    example = "u123",
                    required = false
            )
            @RequestHeader(value = "X-User-Key", defaultValue = "guest", required = false) String userKey
    ) {
        boolean first = subscriptions.subscribe(userKey, ticker);

        if (first) {
            wsClient.subscribeTo(ticker.toUpperCase());
        }

        return "Subscribed to ticker: " + ticker.toUpperCase();
    }

    @Operation(
            summary = "Unsubscribe from real-time price stream",
            description = "Removes the current user from a WebSocket subscription for the given ticker."
    )
    @DeleteMapping("/subscribe/{ticker}")
    public String unsubscribe(
            @Parameter(
                    description = "Stock ticker symbol (uppercase), e.g. AAPL",
                    example = "AAPL",
                    required = true,
                    schema = @Schema(minLength = 1, maxLength = 12, pattern = "^[A-Z0-9.\\-]{1,12}$")
            )
            @PathVariable String ticker,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-User-Key",
                    description = "Optional user key for tracking/rate limits. Defaults to 'guest'.",
                    example = "u123",
                    required = false
            )
            @RequestHeader(value = "X-User-Key", defaultValue = "guest", required = false) String userKey
    ) {
        boolean last = subscriptions.unsubscribe(userKey, ticker);

        if (last) {
            wsClient.unsubscribeFrom(ticker.toUpperCase());
        }

        return "Unsubscribed from ticker: " + ticker.toUpperCase();
    }
}
