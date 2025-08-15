package daniel.nuud.newsservice.service;

import daniel.nuud.newsservice.dto.ApiArticle;
import daniel.nuud.newsservice.dto.ApiResponse;
import daniel.nuud.newsservice.dto.ArticleDto;
import daniel.nuud.newsservice.exception.ResourceNotFoundException;
import daniel.nuud.newsservice.model.Article;
import daniel.nuud.newsservice.repository.NewsRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final PolygonClient polygonClient;
    private final NewsRepository newsRepository;
    private final ArticleWriter articleWriter;

    @Value("${polygon.api.key}")
    private String apiKey;

    @Bulkhead(name = "newsFetch", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "skipRefresh")
    public boolean fetchAndSaveNews(String ticker) {

        ApiResponse response = polygonClient.getApiResponse(ticker, apiKey);

        log.info("Fetching news for ticker: {}", ticker);

        if (response == null || response.getResults() == null) {
            throw new ResourceNotFoundException("No news found for ticker " + ticker);
        }

        List<String> ids = response.getResults().stream()
                .map(ApiArticle::getId)
                .toList();

        List<Article> articles = getArticles(ids, response);

        if (!articles.isEmpty()) {
            articleWriter.saveArticles(articles);
            return true;
        }

        return false;
    }

    private boolean skipRefresh(String ticker, Throwable ex) {
        log.warn("Skip refresh for {}: {}", ticker, ex.toString());
        return false;
    }

    private List<Article> getArticles(List<String> ids, ApiResponse response) {
        Set<String> existingIds = newsRepository.findAllById(ids).stream()
                .map(Article::getId)
                .collect(Collectors.toSet());

        return response.getResults().stream()
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
    }

    @Bulkhead(name = "newsRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true, timeout = 2)
    public List<ArticleDto> getTop5NewsByTicker(String ticker) {
        return newsRepository.findTop5ByTickersOrderByPublishedUtcDesc(ticker)
                .stream()
                .map(ArticleDto::from)
                .toList();
    }

}
