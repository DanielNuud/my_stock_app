package daniel.nuud.historicalanalyticsservice.controller;

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

    @Autowired
    private HistoricalService historicalService;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<StockBar>> getStockBar(@RequestParam String period,
                                                     @RequestParam Integer multiplier, @PathVariable String ticker,
                                                     @RequestParam String timespan) {

        return ResponseEntity.ok(historicalService.getHistoricalStockBar(ticker, period, multiplier, timespan));
    }
}
