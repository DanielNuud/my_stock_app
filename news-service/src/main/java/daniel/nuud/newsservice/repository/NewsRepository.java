package daniel.nuud.newsservice.repository;

import daniel.nuud.newsservice.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<Article, String> {
}
