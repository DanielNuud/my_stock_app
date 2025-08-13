package daniel.nuud.historicalanalyticsservice.controller;

import daniel.nuud.historicalanalyticsservice.dto.StockPrice;
import daniel.nuud.historicalanalyticsservice.model.StockBar;
import daniel.nuud.historicalanalyticsservice.service.HistoricalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historical")
@RequiredArgsConstructor
public class HistoricalController {

    private final HistoricalService historicalService;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<StockBar>> getStockBar(@RequestParam String period, @PathVariable String ticker,
                                                      @RequestHeader(value = "X-User-Key", defaultValue = "guest") String userKey) {
        return ResponseEntity.ok(historicalService.getHistoricalStockBar(ticker, period, userKey));
    }

    @PostMapping("/realtime")
    public ResponseEntity<Void> receiveRealtimePrice(@RequestBody StockPrice stockPrice) {
        historicalService.saveRealtimePrice(stockPrice);
        return ResponseEntity.ok().build();
    }
}
