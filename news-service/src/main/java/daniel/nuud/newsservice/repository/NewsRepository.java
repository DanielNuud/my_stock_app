package daniel.nuud.newsservice.repository;

import daniel.nuud.newsservice.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<Article, String> {
    List<Article> findTop5ByTickersOrderByPublishedUtcDesc(String ticker);
}
