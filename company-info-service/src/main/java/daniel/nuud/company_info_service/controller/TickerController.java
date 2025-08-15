package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.dto.api.Ticker;
import daniel.nuud.company_info_service.model.TickerEntity;
import daniel.nuud.company_info_service.service.TickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickers")
@RequiredArgsConstructor
@Slf4j
public class TickerController {

    private final TickerService tickerService;

    @GetMapping("/search")
    public ResponseEntity<List<TickerEntity>> searchTickers(@RequestParam("query") String query) {
        log.info("Searching tickers with query: {}", query);

        boolean refreshed = tickerService.fetchAndSaveTickers(query);

        var data = tickerService.getFromDB(query);

        return ResponseEntity.ok()
                .header("X-Data-Freshness", refreshed ? "fresh" : "stale")
                .body(data);
    }
}
