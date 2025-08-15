package daniel.nuud.newsservice.service;

import daniel.nuud.newsservice.model.Article;
import daniel.nuud.newsservice.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleWriter {

    private final NewsRepository repository;

    @Transactional(timeout = 5)
    public void saveArticles(List<Article> articles) {
        repository.saveAll(articles);
    }
}
