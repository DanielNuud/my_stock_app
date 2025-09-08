package daniel.nuud.newsservice.dto;

import daniel.nuud.newsservice.model.Article;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "Article",
        description = "News article item associated with one or more tickers."
)
public class ArticleDto {
    @Schema(
            description = "Article identifier."
    )
    private String id;

    @Schema(
            description = "Headline/title.",
            example = "Apple unveils new Mac lineup"
    )
    private String title;

    @Schema(
            description = "Article author.",
            example = "John Doe"
    )
    private String author;

    @Schema(
            description = "Short summary/abstract.",
            example = "Apple announced a refresh of its Mac lineup featuring ..."
    )
    private String description;

    @Schema(
            description = "Canonical article URL.",
            format = "uri",
            example = "https://example.com/news/apple-unveils-new-mac"
    )
    private String articleUrl;

    @Schema(
            description = "Preview image URL.",
            format = "uri",
            example = "https://cdn.example.com/images/aapl_news_001.jpg"
    )
    private String imageUrl;

    @Schema(
            description = "Publication timestamp (UTC, ISO-8601).",
            format = "date-time",
            example = "2025-09-07T12:34:56Z"
    )
    private String publishedUtc;

    @Schema(description = "Publisher name.", example = "Bloomberg")
    private String publisherName;

    @Schema(
            description = "Publisher logo URL.",
            format = "uri",
            example = "https://cdn.example.com/publishers/bloomberg-logo.png"
    )
    private String publisherLogoUrl;

    @Schema(
            description = "Publisher homepage URL.",
            format = "uri",
            example = "https://www.bloomberg.com"
    )
    private String publisherHomepageUrl;

    @Schema(
            description = "Publisher favicon URL.",
            format = "uri",
            example = "https://www.bloomberg.com/favicon.ico"
    )
    private String publisherFaviconUrl;

    @ArraySchema(
            arraySchema = @Schema(description = "Associated tickers."),
            schema = @Schema(
                    description = "Ticker symbol",
                    example = "AAPL",
                    minLength = 1,
                    maxLength = 12,
                    pattern = "^[A-Z0-9.\\-]{1,12}$"
            )
    )
    private List<String> tickers = new ArrayList<>();

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
                .tickers(new ArrayList<>(a.getTickers()))
                .build();
    }
}
