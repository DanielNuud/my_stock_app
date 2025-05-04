package daniel.nuud.newsservice.controller;

import daniel.nuud.newsservice.model.Article;
import daniel.nuud.newsservice.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<Article>> getNews(@PathVariable String ticker) {
        var articles = newsService.fetchAndSaveNews(ticker);
        return ResponseEntity.ok(articles);
    }
}
