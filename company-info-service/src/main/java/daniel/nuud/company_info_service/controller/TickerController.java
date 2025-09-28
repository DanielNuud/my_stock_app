package daniel.nuud.company_info_service.controller;

import daniel.nuud.company_info_service.model.TickerEntity;
import daniel.nuud.company_info_service.service.TickerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Tickers", description = "Ticker search and metadata")
@RestController
@RequestMapping("/api/tickers")
@RequiredArgsConstructor
@Slf4j
public class TickerController {

    private final TickerService tickerService;

    @Operation(
            summary = "Search tickers",
            description = "Find tickers by symbol prefix or company name. " +
                    "Also refreshes local cache from the upstream provider when needed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results returned"
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<TickerEntity>> searchTickers(
            @Parameter(
            description = "Search term (ticker prefix or company name).",
            example = "AA",
            required = true
        )
            @RequestParam("query") String query) {

        log.info("Searching tickers with query: {}", query);

        boolean refreshed = tickerService.fetchAndSaveTickers(query);

        var data = tickerService.getFromDB(query);

        return ResponseEntity.ok()
                .header("X-Data-Freshness", refreshed ? "fresh" : "stale")
                .body(data);
    }
}
