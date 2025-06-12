package daniel.nuud.newsservice.service;

import daniel.nuud.newsservice.dto.ApiArticle;
import daniel.nuud.newsservice.dto.ApiResponse;
import daniel.nuud.newsservice.dto.Publisher;
import daniel.nuud.newsservice.exception.ResourceNotFoundException;
import daniel.nuud.newsservice.model.Article;
import daniel.nuud.newsservice.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NewsServiceTest {

    @InjectMocks
    private NewsService newsService;

    @Mock
    private NewsRepository newsRepository;

    private NewsService spyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        spyService = spy(newsService);
    }

    @Test
    @DisplayName("fetchNews: if API returns null")
    void fetchNews_ifReturnsNull() {

        String ticker = "MSFT";

        when(newsRepository.findAllByTickersContaining(ticker)).thenReturn(Optional.empty());

        ApiResponse respNull = new ApiResponse();
        respNull.setResults(null);

        var spyService = Mockito.spy(newsService);
        doReturn(respNull).when(spyService).getApiResponse(ticker);

        assertThatThrownBy(() -> spyService.fetchAndSaveNews(ticker))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MSFT");

        verify(newsRepository, never()).save(any());

    }

    @Test
    @DisplayName("fetchNews:success case")
    void fetchNews_successCase() {
        String ticker = "AAPL";

        ApiArticle article = getApiArticle();

        ApiResponse resp = new ApiResponse();
        resp.setResults(List.of(article));

        when(newsRepository.findAllById(List.of("article-123")))
                .thenReturn(Collections.emptyList());

        when(newsRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var spyService = spy(newsService);
        doReturn(resp).when(spyService).getApiResponse(ticker);

        var result = spyService.fetchAndSaveNews(ticker);

        assertNotNull(result);
        assertEquals(1, result.size());

        Article saved = result.get(0);
        assertEquals("Apple Stock Rises After Strong Earnings", saved.getTitle());
        assertEquals("Bloomberg", saved.getPublisherName());
        assertEquals("AAPL", saved.getTickers().get(0));

        verify(newsRepository, times(1)).saveAll(anyList());

    }

    private static ApiArticle getApiArticle() {
        ApiArticle article = new ApiArticle();
        article.setArticleUrl("https://example.com/apple-stock-rises");
        article.setTitle("Apple Stock Rises After Strong Earnings");
        article.setDescription("Apple reports record revenue in Q2 2025.");
        article.setId("article-123");
        article.setImageUrl("https://example.com/images/apple.png");
        article.setAuthor("John Doe");
        article.setTickers(List.of("AAPL"));
        article.setPublishedUtc("2025-06-09T10:15:30Z");

        Publisher publisher = new Publisher();
        publisher.setName("Bloomberg");
        publisher.setFavicon("https://example.com/favicon.ico");
        publisher.setHomepageUrl("https://bloomberg.com");
        publisher.setLogoUrl("https://example.com/logo.png");

        article.setPublisher(publisher);
        return article;
    }

    @Test
    @DisplayName("fetchNews: if one article is already in DB")
    void fetchNews_ifOneArticleIsAlreadyInDB() {
        String ticker = "AAPL";

        ApiArticle article = getApiArticle();

        ApiResponse resp = new ApiResponse();
        resp.setResults(List.of(article));

        Article existingArticle = new Article();
        existingArticle.setId("article-123");
        existingArticle.setTitle("Apple Stock Rises After Strong Earnings");

        when(newsRepository.findAllById(List.of("article-123"))).thenReturn(List.of(existingArticle));

        var spyService = spy(newsService);
        doReturn(resp).when(spyService).getApiResponse(ticker);

        var result = spyService.fetchAndSaveNews(ticker);
        assertTrue(result.isEmpty());
        verify(newsRepository, never()).saveAll(anyList());

    }

    @Test
    @DisplayName("fetchNews: saves only new article when one already exists in DB")
    void fetchNews_savesOnlyNewArticleWhenOneAlreadyExistsInDB() {

        ApiArticle article1 = getApiArticle();
        ApiArticle article2 = new ApiArticle();
        article2.setId("article-456");
        article2.setTitle("Another Apple Stock Rises After Strong Earnings");
        article2.setDescription("Apple reports record revenue in Q3 2025.");
        article2.setTickers(List.of("AAPL"));

        Publisher publisher = new Publisher();
        publisher.setName("Washington");
        publisher.setFavicon("https://example.com/favicon.ico");
        publisher.setHomepageUrl("https://washington.com");
        publisher.setLogoUrl("https://example.com/logo.png");

        article2.setPublisher(publisher);

        ApiResponse resp = new ApiResponse();
        resp.setResults(List.of(article1, article2));

        Article existingArticle = new Article();
        existingArticle.setId("article-123");

        when(newsRepository.findAllById(List.of("article-123", "article-456"))).thenReturn(List.of(existingArticle));
        when(newsRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var spyService = spy(newsService);
        doReturn(resp).when(spyService).getApiResponse("AAPL");

        var result = spyService.fetchAndSaveNews("AAPL");
        assertEquals(1, result.size());
    }
}
