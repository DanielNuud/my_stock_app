package daniel.nuud.newsservice.controller;

import daniel.nuud.newsservice.dto.ArticleDto;
import daniel.nuud.newsservice.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<ArticleDto>> fetchNews(@PathVariable String ticker) {
        newsService.fetchAndSaveNews(ticker);
        return ResponseEntity.ok(newsService.getTop5NewsByTicker(ticker));
    }

    @PostMapping("/fetch/{ticker}")
    public ResponseEntity<Void> fetch(@PathVariable String ticker) {
        newsService.fetchAndSaveNews(ticker);
        return ResponseEntity.accepted().build();
    }
}
