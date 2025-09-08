package daniel.nuud.newsservice.controller;

import daniel.nuud.newsservice.dto.ArticleDto;
import daniel.nuud.newsservice.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "News", description = "Company news by ticker")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(
            summary = "Get top 5 news for a ticker",
            description = "Fetches and returns the latest top 5 news for the given ticker. " +
                    "If a refresh from the upstream provider was performed during this call, " +
                    "the response includes header **X-Data-Freshness: fresh** (otherwise `stale`)."
    )
    @GetMapping("/{ticker}")
    public ResponseEntity<List<ArticleDto>> fetchNews(
            @Parameter(
                    description = "Stock ticker symbol (uppercase), e.g. AAPL",
                    example = "AAPL",
                    required = true,
                    schema = @Schema(minLength = 1, maxLength = 12, pattern = "^[A-Z0-9.\\-]{1,12}$")
            )
            @PathVariable String ticker) {

        boolean refreshed = newsService.fetchAndSaveNews(ticker);
        List<ArticleDto> data = newsService.getTop5NewsByTicker(ticker);

        return ResponseEntity.ok()
                .header("X-Data-Freshness", refreshed ? "fresh" : "stale")
                .body(data);
    }
}
