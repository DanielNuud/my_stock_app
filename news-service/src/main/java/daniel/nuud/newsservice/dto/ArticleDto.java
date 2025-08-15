package daniel.nuud.newsservice.dto;

import daniel.nuud.newsservice.model.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private String id;
    private String title;
    private String author;
    private String description;
    private String articleUrl;
    private String imageUrl;
    private String publishedUtc;
    private String publisherName;
    private String publisherLogoUrl;
    private String publisherHomepageUrl;
    private String publisherFaviconUrl;
    private List<String> tickers;

    public static ArticleDto from(Article a) {
        return ArticleDto.builder()
                .id(a.getId())
                .title(a.getTitle())
                .author(a.getAuthor())
                .description(a.getDescription())
                .articleUrl(a.getArticleUrl())
                .imageUrl(a.getImageUrl())
                .publishedUtc(a.getPublishedUtc())
                .publisherName(a.getPublisherName())
                .publisherLogoUrl(a.getPublisherLogoUrl())
                .publisherHomepageUrl(a.getPublisherHomepageUrl())
                .publisherFaviconUrl(a.getPublisherFaviconUrl())
                .tickers(new ArrayList<>(a.getTickers())) // ← здесь коллекция инициализируется
                .build();
    }
}
