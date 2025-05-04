package daniel.nuud.newsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Article {

    @Id
    private String id;

    private String title;
    private String author;
    private String description;
    private String articleUrl;
    private String imageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "ticker")
    private List<String> tickers = new ArrayList<>();
}
