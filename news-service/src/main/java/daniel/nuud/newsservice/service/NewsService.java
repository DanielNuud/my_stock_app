package daniel.nuud.newsservice.service;

import daniel.nuud.newsservice.dto.ApiArticle;
import daniel.nuud.newsservice.dto.ApiResponse;
import daniel.nuud.newsservice.exception.ResourceNotFoundException;
import daniel.nuud.newsservice.model.Article;
import daniel.nuud.newsservice.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService {

    private final WebClient webClient;

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(WebClient webClient, NewsRepository newsRepository) {
        this.webClient = webClient;
        this.newsRepository = newsRepository;
    }

    @Value("${polygon.api.key}")
    private String apiKey;

    public List<Article> fetchAndSaveNews(String ticker) {
        ApiResponse response = webClient.get()
                .uri("/v2/reference/news?ticker={ticker}&order=asc&limit=10&sort=published_utc&apiKey={apiKey}",
                        ticker, apiKey)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();

        if (response == null || response.getResults() == null) {
            throw new ResourceNotFoundException("No news found for ticker " + ticker);
        }

        List<Article> articles = new ArrayList<>();

        for (ApiArticle apiArt : response.getResults()) {
            Article article = new Article();

            article.setId(apiArt.getId());
            article.setTitle(apiArt.getTitle());
            article.setAuthor(apiArt.getAuthor());
            article.setDescription(apiArt.getDescription());
            article.setArticleUrl(apiArt.getArticleUrl());
            article.setImageUrl(apiArt.getImageUrl());
            article.setTickers(new ArrayList<>(apiArt.getTickers()));

            articles.add(article);
        }

        newsRepository.saveAll(articles);

        return articles;
    }

}
