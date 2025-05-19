package daniel.nuud.historicalanalyticsservice.controller;

import daniel.nuud.historicalanalyticsservice.model.StockBar;
import daniel.nuud.historicalanalyticsservice.model.Timespan;
import daniel.nuud.historicalanalyticsservice.service.HistoricalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/historical")
@RequiredArgsConstructor
public class HistoricalController {

    @Autowired
    private HistoricalService historicalService;

    @GetMapping("/{ticker}")
    public ResponseEntity<StockBar> getStockBar(@RequestParam LocalDate from, @RequestParam LocalDate to,
                                                @RequestParam Integer multiplier, @PathVariable String ticker,
                                                @RequestParam String timespan) {

        return ResponseEntity.ok(historicalService.fetchStockBar(ticker, from, to, multiplier, timespan));
    }
}
