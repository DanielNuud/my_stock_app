package daniel.nuud.newsservice.controller;

import daniel.nuud.newsservice.dto.ArticleDto;
import daniel.nuud.newsservice.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<ArticleDto>> fetchNews(@PathVariable String ticker) {
        boolean refreshed = newsService.fetchAndSaveNews(ticker);
        List<ArticleDto> data = newsService.getTop5NewsByTicker(ticker);
        return ResponseEntity.ok()
                .header("X-Data-Freshness", refreshed ? "fresh" : "stale")
                .body(data);
    }

    @PostMapping("/fetch/{ticker}")
    public ResponseEntity<Void> fetch(@PathVariable String ticker) {
        newsService.fetchAndSaveNews(ticker);
        return ResponseEntity.accepted().build();
    }
}
