package daniel.nuud.newsservice.service;

import daniel.nuud.newsservice.dto.ApiArticle;
import daniel.nuud.newsservice.dto.ApiResponse;
import daniel.nuud.newsservice.exception.ResourceNotFoundException;
import daniel.nuud.newsservice.model.Article;
import daniel.nuud.newsservice.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final WebClient webClient;

    private final NewsRepository newsRepository;

    @Value("${polygon.api.key}")
    private String apiKey;

    public List<Article> fetchAndSaveNews(String ticker) {
        ApiResponse response = getApiResponse(ticker);

        if (response == null || response.getResults() == null) {
            throw new ResourceNotFoundException("No news found for ticker " + ticker);
        }

        List<String> ids = response.getResults().stream()
                .map(ApiArticle::getId)
                .toList();

        Set<String> existingIds = newsRepository.findAllById(ids).stream()
                .map(Article::getId)
                .collect(Collectors.toSet());

        List<Article> articles = response.getResults().stream()
                .filter(apiArt -> !existingIds.contains(apiArt.getId()))
                .map(apiArt -> {
                    Article article = new Article();

                    article.setId(apiArt.getId());
                    article.setTitle(apiArt.getTitle());
                    article.setAuthor(apiArt.getAuthor());
                    article.setDescription(apiArt.getDescription());
                    article.setArticleUrl(apiArt.getArticleUrl());
                    article.setImageUrl(apiArt.getImageUrl());
                    article.setTickers(new ArrayList<>(apiArt.getTickers()));
                    article.setPublishedUtc(apiArt.getPublishedUtc());
                    article.setPublisherName(apiArt.getPublisher().getName());
                    article.setPublisherLogoUrl(apiArt.getPublisher().getLogoUrl());
                    article.setPublisherHomepageUrl(apiArt.getPublisher().getHomepageUrl());
                    article.setPublisherFaviconUrl(apiArt.getPublisher().getFavicon());

                    return article;
                })
                .toList();


        if (!articles.isEmpty()) {
            return newsRepository.saveAll(articles);
        }

        return List.of();
    }

    public ApiResponse getApiResponse(String ticker) {
        ApiResponse response = webClient.get()
                .uri("/v2/reference/news?ticker={ticker}&order=asc&limit=10&sort=published_utc&apiKey={apiKey}",
                        ticker, apiKey)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();
        return response;
    }

    public List<Article> getNewsByTicker (String ticker) {
        return newsRepository.findAllByTickersContaining(ticker)
                .orElseThrow(() -> new ResourceNotFoundException("No news found for ticker " + ticker));
    }

    public List<Article> getAllNews() {
        return newsRepository.findAll();
    }

}
